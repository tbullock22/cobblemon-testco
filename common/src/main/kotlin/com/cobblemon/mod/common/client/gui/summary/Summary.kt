/*
 * Copyright (C) 2023 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.gui.summary

import com.cobblemon.mod.common.CobblemonSounds
import com.cobblemon.mod.common.api.gui.blitk
import com.cobblemon.mod.common.api.moves.Move
import com.cobblemon.mod.common.api.moves.MoveSet
import com.cobblemon.mod.common.api.reactive.Observable.Companion.emitWhile
import com.cobblemon.mod.common.api.reactive.ObservableSubscription
import com.cobblemon.mod.common.api.scheduling.after
import com.cobblemon.mod.common.api.storage.party.PartyPosition
import com.cobblemon.mod.common.api.text.bold
import com.cobblemon.mod.common.api.text.text
import com.cobblemon.mod.common.client.CobblemonClient
import com.cobblemon.mod.common.client.CobblemonResources
import com.cobblemon.mod.common.client.gui.ExitButton
import com.cobblemon.mod.common.client.gui.TypeIcon
import com.cobblemon.mod.common.client.gui.summary.widgets.EvolutionSelectScreen
import com.cobblemon.mod.common.client.gui.summary.widgets.ModelWidget
import com.cobblemon.mod.common.client.gui.summary.widgets.PartyWidget
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.SummaryTab
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.info.InfoWidget
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves.MoveSwapScreen
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.moves.MovesWidget
import com.cobblemon.mod.common.client.gui.summary.widgets.screens.stats.StatWidget
import com.cobblemon.mod.common.client.render.drawScaledText
import com.cobblemon.mod.common.net.messages.server.storage.party.MovePartyPokemonPacket
import com.cobblemon.mod.common.net.messages.server.storage.party.SwapPartyPokemonPacket
import com.cobblemon.mod.common.pokemon.Gender
import com.cobblemon.mod.common.pokemon.Pokemon
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.lang
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.Drawable
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.Selectable
import net.minecraft.client.gui.screen.Screen
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.client.sound.PositionedSoundInstance
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.sound.SoundEvent
import net.minecraft.text.Text

/**
 * The screen responsible for displaying various information regarding a Pokémon team.
 *
 * @property party The party that will be displayed.
 * @property editable Whether you shall be able to edit Pokémon with operations such as reordering their move set.
 *
 * @param selection The index the [party] will have as the base [selectedPokemon].
 */
class Summary private constructor(party: Collection<Pokemon>, private val editable: Boolean, selection: Int): Screen(Text.translatable("cobblemon.ui.summary.title")) {

    companion object {
        const val BASE_WIDTH = 331
        const val BASE_HEIGHT = 161
        private const val PORTRAIT_SIZE = 66
        private const val SCALE = 0.5F

        // Main Screen Index
        private const val INFO = 0
        private const val MOVES = 1
        private const val STATS = 2

        // Side Screen Index
        const val PARTY = 0
        const val MOVE_SWAP = 1
        const val EVOLVE = 2

        // Resources
        private val baseResource = cobblemonResource("ui/summary/summary_base.png")
        private val portraitBackgroundResource = cobblemonResource("ui/summary/portrait_background.png")
        private val typeSpacerResource = cobblemonResource("ui/summary/type_spacer.png")
        private val typeSpacerDoubleResource = cobblemonResource("ui/summary/type_spacer_double.png")
        private val sideSpacerResource = cobblemonResource("ui/summary/summary_side_spacer.png")
        private val evolveButtonResource = cobblemonResource("ui/summary/summary_evolve_button.png")

        val iconShinyResource = cobblemonResource("ui/summary/icon_shiny.png")

        /**
         * Attempts to open this screen for a client.
         * If an exception is thrown this screen will not open.
         *
         * @param party The party to be displayed.
         * @param editable Whether you shall be able to edit Pokémon with operations such as reordering their move set.
         * @param selection The index to start as the selected party member, based on the [party].
         *
         * @throws IllegalArgumentException If the [party] is empty or contains more than 6 members.
         * @throws IndexOutOfBoundsException If the [selection] is not a possible index of [party].
         */
        fun open(party: Collection<Pokemon>, editable: Boolean = true, selection: Int = 0) {
            val mc = MinecraftClient.getInstance()
            val screen = Summary(party, editable, selection)
            mc.setScreen(screen)
        }
    }

    internal var selectedPokemon: Pokemon
    private lateinit var mainScreen: ClickableWidget
    lateinit var sideScreen: Element
    lateinit var modelWidget: ModelWidget
    private val summaryTabs = mutableListOf<SummaryTab>()
    private var mainScreenIndex = INFO
    var sideScreenIndex = PARTY
    private val party = ArrayList(party)

    init {
        if (this.party.isEmpty()) {
            throw IllegalArgumentException("Summary UI cannot display zero Pokemon")
        }
        if (this.party.size > 6) {
            throw IllegalArgumentException("Summary UI cannot display more than six Pokemon")
        }
        this.selectedPokemon = this.party[selection]
        this.listenToMoveSet()
    }

    /**
     * Initializes the Summary Screen
     */
    public override fun init() {
        clearChildren()
        super.init()

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        displayMainScreen(mainScreenIndex)
        displaySideScreen(PARTY)

        // Evolve Button
        addDrawableChild(
            SummaryButton(
                buttonX = x + 12F,
                buttonY = y + 145F,
                buttonWidth = 54,
                buttonHeight = 15,
                clickAction = {
                    after(ticks = 0, serverThread = false) {
                        displaySideScreen(if (sideScreenIndex == EVOLVE) PARTY else EVOLVE)
                    }
                },
                text = lang("ui.evolve"),
                resource = evolveButtonResource,
                renderRequirement = { selectedPokemon.evolutionProxy.client().isNotEmpty() },
                clickRequirement = { selectedPokemon.evolutionProxy.client().isNotEmpty() }
            )
        )

        // Init Tabs
        summaryTabs.clear()
        summaryTabs.add(
            SummaryTab(
                pX = x + 78,
                pY = y - 1,
                label = lang("ui.info")
            ) {
                if (mainScreenIndex != INFO) {
                    displayMainScreen(INFO)
                    playSound(CobblemonSounds.GUI_CLICK.get())
                }
            }
        )

        summaryTabs.add(
            SummaryTab(
                pX = x + 119,
                pY = y - 1,
                label = lang("ui.moves")
            ) {
                if (mainScreenIndex != MOVES) {
                    displayMainScreen(MOVES)
                    playSound(CobblemonSounds.GUI_CLICK.get())
                }
            }
        )

        summaryTabs.add(
            SummaryTab(
                pX = x + 160,
                pY = y - 1,
                label = lang("ui.stats")
            ) {
                if (mainScreenIndex != STATS) {
                    displayMainScreen(STATS)
                    playSound(CobblemonSounds.GUI_CLICK.get())
                }
            }
        )

        summaryTabs[mainScreenIndex].toggleTab()
        summaryTabs.forEach { addDrawableChild(it) }

        // Add Exit Button
        addDrawableChild(
            ExitButton(
                pX = x + 302,
                pY = y + 145
            ) {
                playSound(CobblemonSounds.GUI_CLICK.get())
                MinecraftClient.getInstance().setScreen(null)
            }
        )

        // Add Model Preview
        modelWidget = ModelWidget(
            pX = x + 6,
            pY = y + 32,
            pWidth = PORTRAIT_SIZE,
            pHeight = PORTRAIT_SIZE,
            pokemon = selectedPokemon.asRenderablePokemon(),
            baseScale = 2F,
            rotationY = 325F,
            offsetY = -10.0
        )
    }

    fun swapPartySlot(sourceIndex: Int, targetIndex: Int) {
        val sourcePokemon = this.party.getOrNull(sourceIndex)

        if (sourcePokemon != null) {
            val targetPokemon = this.party.getOrNull(targetIndex)
            val sourcePosition = PartyPosition(sourceIndex)
            val targetPosition = PartyPosition(targetIndex)

            val packet = targetPokemon?.let { SwapPartyPokemonPacket(it.uuid, targetPosition, sourcePokemon.uuid, sourcePosition) }
                ?: MovePartyPokemonPacket(sourcePokemon.uuid, sourcePosition, targetPosition)
            packet.sendToServer()

            // Update change in UI
            this.party[targetIndex] = sourcePokemon
            this.party[sourceIndex] = targetPokemon
            displaySideScreen(PARTY)
            (sideScreen as PartyWidget).enableSwap()
        }
    }

    /**
     * Switches the selected PKM
     */
    fun switchSelection(newSelection: Int) {
        this.party.getOrNull(newSelection)?.let { this.selectedPokemon = it }
        moveSetSubscription?.unsubscribe()
        listenToMoveSet()
        displayMainScreen(mainScreenIndex)
        children().find { it is EvolutionSelectScreen }?.let(this::remove)
        modelWidget.pokemon = selectedPokemon.asRenderablePokemon()
    }

    private var moveSetSubscription: ObservableSubscription<MoveSet>? = null

    /**
     * Start observing the MoveSet of the current PKM for changes
     */
    private fun listenToMoveSet() {
        moveSetSubscription = selectedPokemon.moveSet.observable
            .pipe(emitWhile { isOpen() })
            .subscribe {
                if (mainScreen is MovesWidget)
                    displayMainScreen(MOVES)
            }
    }

    /**
     * Returns if this Screen is open or not
     */
    private fun isOpen() = MinecraftClient.getInstance().currentScreen == this

    /**
     * Switch center screen
     */
    private fun displayMainScreen(screen: Int) {
        // Get stat tab index if currently displaying stat screen
        var subIndex = if (mainScreenIndex == STATS && mainScreen is StatWidget) (mainScreen as StatWidget).statTabIndex else 0

        mainScreenIndex = screen
        if (::mainScreen.isInitialized) remove(mainScreen)
        if (sideScreenIndex == MOVE_SWAP) displaySideScreen(PARTY)

        summaryTabs.forEachIndexed {index, item ->
            if (index == screen) item.toggleTab() else item.toggleTab(false)
        }

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        when (screen) {
            INFO -> {
                mainScreen = InfoWidget(
                    pX = x + 77,
                    pY = y + 12,
                    pokemon = this.selectedPokemon
                )
            }
            MOVES -> {
                mainScreen = MovesWidget(
                    pX = x + 77,
                    pY = y + 12,
                    summary = this
                )
            }
            STATS -> {
                mainScreen = StatWidget(
                    pX = x + 77,
                    pY = y + 12,
                    pokemon = this.selectedPokemon,
                    tabIndex = subIndex
                )
            }
        }
        addDrawableChild(mainScreen)
    }

    /**
     * Switch right screen
     */
    fun displaySideScreen(screen: Int, move: Move? = null) {
        sideScreenIndex = screen
        if (::sideScreen.isInitialized) remove(sideScreen)

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        when (screen) {
            PARTY -> {
                sideScreen = PartyWidget(
                    pX = x + 216,
                    pY = y + 24,
                    isParty = selectedPokemon in CobblemonClient.storage.myParty,
                    summary = this,
                    partyList = this.party
                )
            }
            MOVE_SWAP -> {
                val movesWidget = mainScreen
                if (movesWidget is MovesWidget && move != null) {
                    sideScreen = MoveSwapScreen(
                        x + 216,
                        y + 22,
                        movesWidget = movesWidget,
                        replacedMove = move
                    ).also { switchPane ->
                        val pokemon = selectedPokemon
                        pokemon.allAccessibleMoves
                            .filter { template -> pokemon.moveSet.none { it.template == template } }
                            .map { template ->
                                val benched = pokemon.benchedMoves.find { it.moveTemplate == template }
                                MoveSwapScreen.MoveSlot(switchPane, template, benched?.ppRaisedStages ?: 0)
                            }
                            .forEach { switchPane.addEntry(it) }
                    }
                }
            }
            EVOLVE -> {
                sideScreen = EvolutionSelectScreen(
                    x + 216,
                    y + 22,
                    pokemon = selectedPokemon
                )
            }
        }
        val element = sideScreen
        if (element is Drawable && element is Selectable) {
            addDrawableChild(element)
        }
    }

    override fun render(pMatrixStack: MatrixStack, pMouseX: Int, pMouseY: Int, pPartialTicks: Float) {
        renderBackground(pMatrixStack)

        val x = (width - BASE_WIDTH) / 2
        val y = (height - BASE_HEIGHT) / 2

        // Render Portrait Background
        blitk(
            matrixStack = pMatrixStack,
            texture = portraitBackgroundResource,
            x = x + 6,
            y = y + 32,
            width = PORTRAIT_SIZE,
            height = PORTRAIT_SIZE
        )

        modelWidget.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks)

        // Render Base Resource
        blitk(
            matrixStack = pMatrixStack,
            texture = baseResource,
            x = x,
            y = y,
            width = BASE_WIDTH,
            height = BASE_HEIGHT
        )

        // Status
        val status = selectedPokemon.status?.status
        if (selectedPokemon.isFainted() || status != null) {
            val statusName = if (selectedPokemon.isFainted()) "fnt" else status?.showdownName
            blitk(
                matrixStack = pMatrixStack,
                texture = cobblemonResource("ui/battle/battle_status_$statusName.png"),
                x = x + 34,
                y = y + 4,
                height = 7,
                width = 39,
                uOffset = 35,
                textureWidth = 74
            )

            blitk(
                matrixStack = pMatrixStack,
                texture = cobblemonResource("ui/summary/status_trim.png"),
                x = x + 34,
                y = y + 5,
                height = 6,
                width = 3
            )

            drawScaledText(
                matrixStack = pMatrixStack,
                font = CobblemonResources.DEFAULT_LARGE,
                text = lang("ui.status.$statusName").bold(),
                x = x + 39,
                y = y + 3
            )
        }

        // Poké Ball
        val ballResource = cobblemonResource("textures/items/poke_balls/" + selectedPokemon.caughtBall.name.path + ".png")
        blitk(
            matrixStack = pMatrixStack,
            texture = ballResource,
            x = (x + 3.5) / SCALE,
            y = (y + 15) / SCALE,
            width = 16,
            height = 16,
            scale = SCALE
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = selectedPokemon.displayName.bold(),
            x = x + 12,
            y = y + 14.5,
            shadow = true
        )

        if (selectedPokemon.gender != Gender.GENDERLESS) {
            val isMale = selectedPokemon.gender == Gender.MALE
            val textSymbol = if (isMale) "♂".text().bold() else "♀".text().bold()
            drawScaledText(
                matrixStack = pMatrixStack,
                font = CobblemonResources.DEFAULT_LARGE,
                text = textSymbol,
                x = x + 69, // 64 when tag icon is implemented
                y = y + 14.5,
                colour = if (isMale) 0x32CBFF else 0xFC5454,
                shadow = true
            )
        }

        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = lang("ui.lv").bold(),
            x = x + 6,
            y = y + 4.5,
            shadow = true
        )

        drawScaledText(
            matrixStack = pMatrixStack,
            font = CobblemonResources.DEFAULT_LARGE,
            text = selectedPokemon.level.toString().text().bold(),
            x = x + 19,
            y = y + 4.5,
            shadow = true
        )

        // Shiny Icon
        if (selectedPokemon.shiny) {
            blitk(
                matrixStack = pMatrixStack,
                texture = iconShinyResource,
                x = (x + 63.5) / SCALE,
                y = (y + 33.5) / SCALE,
                width = 14,
                height = 14,
                scale = SCALE
            )
        }

        // Type Icon(s)
        blitk(
            matrixStack = pMatrixStack,
            texture = if (selectedPokemon.secondaryType != null) typeSpacerDoubleResource else typeSpacerResource,
            x = (x + 5.5) / SCALE,
            y = (y + 126) / SCALE,
            width = 134,
            height = 24,
            scale = SCALE
        )

        // Held Item
        val heldItem = selectedPokemon.heldItemNoCopy()
        val itemX = x + 3
        val itemY = y + 104
        if (!heldItem.isEmpty) {
            MinecraftClient.getInstance().itemRenderer.renderGuiItemIcon(heldItem, itemX, itemY)
            MinecraftClient.getInstance().itemRenderer.renderGuiItemOverlay(MinecraftClient.getInstance().textRenderer, heldItem, itemX, itemY)
        }

        drawScaledText(
            matrixStack = pMatrixStack,
            text = lang("held_item"),
            x = x + 27,
            y = y + 114.5,
            scale = SCALE
        )

        TypeIcon(
            x = x + 39,
            y = y + 123,
            type = selectedPokemon.primaryType,
            secondaryType = selectedPokemon.secondaryType,
            centeredX = true
        ).render(pMatrixStack)

        blitk(
            matrixStack = pMatrixStack,
            texture = sideSpacerResource,
            x = (x + 217) / SCALE,
            y = (y + 141) / SCALE,
            width = 144,
            height = 14,
            scale = SCALE
        )

        // Render all added Widgets
        super.render(pMatrixStack, pMouseX, pMouseY, pPartialTicks)

        // Render Item Tooltip
        if (!heldItem.isEmpty) {
            val itemHovered = pMouseX.toFloat() in (itemX.toFloat()..(itemX.toFloat() + 16)) && pMouseY.toFloat() in (itemY.toFloat()..(itemY.toFloat() + 16))
            if (itemHovered) renderTooltip(pMatrixStack, heldItem, pMouseX, pMouseY)
        }
    }

    /**
     * Whether this Screen should pause the Game in SinglePlayer
     */
    override fun shouldPause(): Boolean {
        return false
    }

    override fun mouseScrolled(d: Double, e: Double, f: Double): Boolean {
        return children().any { it.mouseScrolled(d, e, f) }
    }

    override fun mouseClicked(d: Double, e: Double, i: Int): Boolean {
        return children().any { it.mouseClicked(d, e, i) }
    }

    fun playSound(soundEvent: SoundEvent) {
        MinecraftClient.getInstance().soundManager.play(PositionedSoundInstance.master(soundEvent, 1.0F))
    }
}