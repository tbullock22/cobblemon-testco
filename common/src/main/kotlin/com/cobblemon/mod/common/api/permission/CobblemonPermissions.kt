/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.api.permission

object CobblemonPermissions {

    private const val COMMAND_PREFIX = "command."
    private val permissions = arrayListOf<Permission>()

    val CHANGE_SCALE_AND_SIZE = this.create("${COMMAND_PREFIX}changescaleandsize", PermissionLevel.ALL_COMMANDS)

    val CHECKSPAWNS = this.create("${COMMAND_PREFIX}checkspawns", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    val GET_NBT = this.create("${COMMAND_PREFIX}getnbt", PermissionLevel.ALL_COMMANDS)

    private const val GIVE_POKEMON_BASE = "${COMMAND_PREFIX}givepokemon"
    val GIVE_POKEMON_SELF = this.create("${GIVE_POKEMON_BASE}.self", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    val GIVE_POKEMON_OTHER = this.create("${GIVE_POKEMON_BASE}.other", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    private const val HEAL_POKEMON_BASE = "${COMMAND_PREFIX}healpokemon"
    val HEAL_POKEMON_SELF = this.create("$HEAL_POKEMON_BASE.self", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    val HEAL_POKEMON_OTHER = this.create("$HEAL_POKEMON_BASE.other", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    private const val LEVEL_UP_BASE = "${COMMAND_PREFIX}levelup"
    val LEVEL_UP_SELF = this.create("$LEVEL_UP_BASE.self", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    val LEVEL_UP_OTHER = this.create("$LEVEL_UP_BASE.other", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    val OPEN_STARTER_SCREEN = this.create("${COMMAND_PREFIX}openstarterscreen", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    private const val POKEMON_EDIT_BASE = "${COMMAND_PREFIX}pokemonedit"
    val POKEMON_EDIT_SELF = this.create("$POKEMON_EDIT_BASE.self", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)
    val POKEMON_EDIT_OTHER = this.create("$POKEMON_EDIT_BASE.other", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    val SPAWN_ALL_POKEMON = this.create("${COMMAND_PREFIX}spawnallpokemon", PermissionLevel.ALL_COMMANDS)
    val GIVE_ALL_POKEMON = this.create("${COMMAND_PREFIX}giveallpokemon", PermissionLevel.ALL_COMMANDS)

    val SPAWN_POKEMON = this.create("${COMMAND_PREFIX}spawnpokemon", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    val STOP_BATTLE = this.create("${COMMAND_PREFIX}stopbattle", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    val TAKE_POKEMON = this.create("${COMMAND_PREFIX}takepokemon", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    val TEACH = this.create("${COMMAND_PREFIX}teach", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    val FRIENDSHIP = this.create("${COMMAND_PREFIX}friendship", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    val HELD_ITEM = this.create("${COMMAND_PREFIX}helditem", PermissionLevel.CHEAT_COMMANDS_AND_COMMAND_BLOCKS)

    fun all(): Iterable<Permission> = this.permissions

    private fun create(node: String, level: PermissionLevel): Permission {
        val permission = CobblemonPermission(node, level)
        this.permissions += permission
        return permission
    }

}