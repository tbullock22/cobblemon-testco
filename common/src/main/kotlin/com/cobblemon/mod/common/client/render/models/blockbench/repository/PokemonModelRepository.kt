/*
 * Copyright (C) 2022 Cobblemon Contributors
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */

package com.cobblemon.mod.common.client.render.models.blockbench.repository

import com.cobblemon.mod.common.Cobblemon
import com.cobblemon.mod.common.client.render.models.blockbench.TexturedModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.JsonPokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.PokemonPoseableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.AbraModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.AerodactylModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.AlakazamModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ArbokModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ArcanineModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ArticunoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.BeedrillModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.BellsproutModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.BlastoiseModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.BulbasaurModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ButterfreeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.CaterpieModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ChanseyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.CharizardModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.CharmanderModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.CharmeleonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ClefableModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ClefairyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.CloysterModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.CuboneModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DewgongModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DiglettModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DittoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DodrioModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DoduoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DragonairModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DragoniteModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DratiniModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DrowzeeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.DugtrioModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.EeveeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.EkansModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ElectabuzzModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ElectrodeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ExeggcuteModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ExeggutorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.FarfetchdModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.FearowModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.FlareonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GastlyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GengarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GeodudeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GloomModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GolbatModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GoldeenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GolduckModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GolemModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GravelerModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GrimerModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GrowlitheModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.GyaradosModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.HaunterModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.HitmonchanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.HitmonleeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.HorseaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.HypnoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.IvysaurModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.JigglypuffModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.JolteonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.JynxModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KabutoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KabutopsModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KadabraModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KakunaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KangaskhanModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KinglerModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KoffingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.KrabbyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.LaprasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.LickitungModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MachampModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MachokeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MachopModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MagikarpModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MagmarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MagnemiteModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MagnetonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MankeyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MarowakModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MeowthModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MetapodModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MewModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MewtwoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MoltresModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MrmimeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.MukModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NidokingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NidoqueenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NidoranfModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NidoranmModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NidorinaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NidorinoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.NinetalesModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.OddishModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.OmanyteModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.OmastarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.OnixModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ParasModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ParasectModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PersianModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PidgeotModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PidgeottoModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PidgeyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PikachuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PinsirModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PoliwagModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PoliwhirlModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PoliwrathModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PonytaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PorygonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PrimeapeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.PsyduckModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RaichuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RapidashModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RaticateModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RattataModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RhydonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.RhyhornModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SandshrewModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SandslashModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ScytherModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SeadraModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SeakingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SeelModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ShellderModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SlowbroModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SlowpokeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SnorlaxModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SpearowModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.SquirtleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.StarmieModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.StaryuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.TangelaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.TaurosModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.TentacoolModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.TentacruelModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VaporeonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VenomothModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VenonatModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VenusaurModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VictreebelModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VileplumeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VoltorbModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.VulpixModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.WartortleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.WeedleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.WeepinbellModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.WeezingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.WigglytuffModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ZapdosModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen1.ZubatModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.BellossomModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.BlisseyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.CleffaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.CrobatModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.ElekidModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.EspeonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.HitmontopModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.IgglybuffModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.KingdraModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.MagbyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.MurkrowModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.NatuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.PichuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.PiloswineModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.PolitoedModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.Porygon2Model
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.QuagsireModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.ScizorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SlowkingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SmoochumModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SteelixModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.SwinubModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.TyrogueModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.UmbreonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.WooperModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.XatuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen2.YanmaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.BlazikenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.CombuskenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.MarshtompModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.MinunModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.MudkipModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.PlusleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.RayquazaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.SableyeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.SwampertModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.TorchicModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.WailmerModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen3.WailordModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.BibarelModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.BidoofModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.BunearyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.ElectivireModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.EmpoleonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.GlaceonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.HappinyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.HonchkrowModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.LeafeonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.LickilickyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.LopunnyModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.MagmortarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.MagnezoneModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.MamoswineModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.MimejrModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.MunchlaxModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.PachirisuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.PiplupModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.PorygonzModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.PrinplupModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.RhyperiorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.TangrowthModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen4.YanmegaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.BasculinModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.CrustleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.DeerlingModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.DwebbleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.EmolgaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.MaractusModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen5.SawsbuckModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen6.SylveonModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.BounsweetModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.DartrixModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.DecidueyeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.IncineroarModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.LittenModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.MimikyuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.NaganadelModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.PoipoleModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.PyukumukuModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.RowletModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.SteeneeModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.TorracatModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen7.TsareenaModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.CentiskorchModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.KleavorModel
import com.cobblemon.mod.common.client.render.models.blockbench.pokemon.gen8.SizzlipedeModel
import com.cobblemon.mod.common.client.render.pokemon.ModelLayer
import com.cobblemon.mod.common.client.render.pokemon.RegisteredSpeciesRendering
import com.cobblemon.mod.common.client.render.pokemon.SpeciesVariationSet
import com.cobblemon.mod.common.client.util.exists
import com.cobblemon.mod.common.entity.pokemon.PokemonEntity
import com.cobblemon.mod.common.pokemon.Species
import com.cobblemon.mod.common.pokemon.aspects.SHINY_ASPECT
import com.cobblemon.mod.common.util.cobblemonResource
import com.cobblemon.mod.common.util.endsWith
import com.cobblemon.mod.common.util.fromJson
import java.io.File
import java.nio.charset.StandardCharsets
import net.minecraft.client.model.ModelPart
import net.minecraft.resource.ResourceManager
import net.minecraft.util.Identifier

object PokemonModelRepository : ModelRepository<PokemonEntity>() {
    val posers = mutableMapOf<Identifier, (ModelPart) -> PokemonPoseableModel>()
    val renders = mutableMapOf<Identifier, RegisteredSpeciesRendering>()

    fun registerPosers(resourceManager: ResourceManager) {
        posers.clear()
        registerInBuiltPosers()
        registerJsonPosers(resourceManager)
    }

    fun registerInBuiltPosers() {
        inbuilt("bulbasaur", ::BulbasaurModel)
        inbuilt("ivysaur", ::IvysaurModel)
        inbuilt("venusaur", ::VenusaurModel)
        inbuilt("charmander", ::CharmanderModel)
        inbuilt("charmeleon", ::CharmeleonModel)
        inbuilt("charizard", ::CharizardModel)
        inbuilt("squirtle", ::SquirtleModel)
        inbuilt("wartortle", ::WartortleModel)
        inbuilt("blastoise", ::BlastoiseModel)
        inbuilt("caterpie", ::CaterpieModel)
        inbuilt("metapod", ::MetapodModel)
        inbuilt("butterfree", ::ButterfreeModel)
        inbuilt("weedle", ::WeedleModel)
        inbuilt("kakuna", ::KakunaModel)
        inbuilt("beedrill", ::BeedrillModel)
        inbuilt("rattata", ::RattataModel)
        inbuilt("raticate", ::RaticateModel)
        inbuilt("eevee", ::EeveeModel)
        inbuilt("magikarp", ::MagikarpModel)
        inbuilt("gyarados", ::GyaradosModel)
        inbuilt("pidgey", ::PidgeyModel)
        inbuilt("pidgeotto", ::PidgeottoModel)
        inbuilt("pidgeot", ::PidgeotModel)
        inbuilt("diglett", ::DiglettModel)
        inbuilt("dugtrio", ::DugtrioModel)
        inbuilt("zubat", ::ZubatModel)
        inbuilt("cleffa", ::CleffaModel)
        inbuilt("clefable", ::ClefableModel)
        inbuilt("clefairy", ::ClefairyModel)
        inbuilt("krabby", ::KrabbyModel)
        inbuilt("paras", ::ParasModel)
        inbuilt("parasect", ::ParasectModel)
        inbuilt("mankey", ::MankeyModel)
        inbuilt("primeape", ::PrimeapeModel)
        inbuilt("oddish", ::OddishModel)
        inbuilt("gloom", ::GloomModel)
        inbuilt("vileplume", ::VileplumeModel)
        inbuilt("bellossom", ::BellossomModel)
        inbuilt("voltorb", ::VoltorbModel)
        inbuilt("electrode", ::ElectrodeModel)
        inbuilt("lapras", ::LaprasModel)
        inbuilt("ekans", ::EkansModel)
        inbuilt("machop", ::MachopModel)
        inbuilt("machoke", ::MachokeModel)
        inbuilt("machamp", ::MachampModel)
        inbuilt("abra", ::AbraModel)
        inbuilt("aerodactyl", ::AerodactylModel)
        inbuilt("alakazam", ::AlakazamModel)
        inbuilt("arbok", ::ArbokModel)
        inbuilt("arcanine", ::ArcanineModel)
        inbuilt("articuno", ::ArticunoModel)
        inbuilt("bellsprout", ::BellsproutModel)
        inbuilt("chansey", ::ChanseyModel)
        inbuilt("cloyster", ::CloysterModel)
        inbuilt("crobat", ::CrobatModel)
        inbuilt("cubone", ::CuboneModel)
        inbuilt("dewgong", ::DewgongModel)
        inbuilt("ditto", ::DittoModel)
        inbuilt("dodrio", ::DodrioModel)
        inbuilt("doduo", ::DoduoModel)
        inbuilt("dragonair", ::DragonairModel)
        inbuilt("dragonite", ::DragoniteModel)
        inbuilt("dratini", ::DratiniModel)
        inbuilt("drowzee", ::DrowzeeModel)
        inbuilt("electabuzz", ::ElectabuzzModel)
        inbuilt("exeggcute", ::ExeggcuteModel)
        inbuilt("exeggutor", ::ExeggutorModel)
        inbuilt("farfetchd", ::FarfetchdModel)
        inbuilt("fearow", ::FearowModel)
        inbuilt("flareon", ::FlareonModel)
        inbuilt("gastly", ::GastlyModel)
        inbuilt("gengar", ::GengarModel)
        inbuilt("geodude", ::GeodudeModel)
        inbuilt("golbat", ::GolbatModel)
        inbuilt("goldeen", ::GoldeenModel)
        inbuilt("golduck", ::GolduckModel)
        inbuilt("golem", ::GolemModel)
        inbuilt("graveler", ::GravelerModel)
        inbuilt("grimer", ::GrimerModel)
        inbuilt("growlithe", ::GrowlitheModel)
        inbuilt("haunter", ::HaunterModel)
        inbuilt("hitmonchan", ::HitmonchanModel)
        inbuilt("hitmonlee", ::HitmonleeModel)
        inbuilt("horsea", ::HorseaModel)
        inbuilt("hypno", ::HypnoModel)
        inbuilt("jigglypuff", ::JigglypuffModel)
        inbuilt("jolteon", ::JolteonModel)
        inbuilt("jynx", ::JynxModel)
        inbuilt("kabuto", ::KabutoModel)
        inbuilt("kabutops", ::KabutopsModel)
        inbuilt("kadabra", ::KadabraModel)
        inbuilt("kangaskhan", ::KangaskhanModel)
        inbuilt("kingler", ::KinglerModel)
        inbuilt("koffing", ::KoffingModel)
        inbuilt("krabby", ::KrabbyModel)
        inbuilt("lickitung", ::LickitungModel)
        inbuilt("magmar", ::MagmarModel)
        inbuilt("magnemite", ::MagnemiteModel)
        inbuilt("magneton", ::MagnetonModel)
        inbuilt("marowak", ::MarowakModel)
        inbuilt("meowth", ::MeowthModel)
        inbuilt("mew", ::MewModel)
        inbuilt("mewtwo", ::MewtwoModel)
        inbuilt("moltres", ::MoltresModel)
        inbuilt("mrmime", ::MrmimeModel)
        inbuilt("muk", ::MukModel)
        inbuilt("nidoking", ::NidokingModel)
        inbuilt("nidoqueen", ::NidoqueenModel)
        inbuilt("nidoranf", ::NidoranfModel)
        inbuilt("nidoranm", ::NidoranmModel)
        inbuilt("nidorina", ::NidorinaModel)
        inbuilt("nidorino", ::NidorinoModel)
        inbuilt("ninetales", ::NinetalesModel)
        inbuilt("omanyte", ::OmanyteModel)
        inbuilt("omastar", ::OmastarModel)
        inbuilt("onix", ::OnixModel)
        inbuilt("persian", ::PersianModel)
        inbuilt("pikachu", ::PikachuModel)
        inbuilt("pinsir", ::PinsirModel)
        inbuilt("poliwag", ::PoliwagModel)
        inbuilt("poliwhirl", ::PoliwhirlModel)
        inbuilt("poliwrath", ::PoliwrathModel)
        inbuilt("politoed", ::PolitoedModel)
        inbuilt("ponyta", ::PonytaModel)
        inbuilt("porygon", ::PorygonModel)
        inbuilt("psyduck", ::PsyduckModel)
        inbuilt("raichu", ::RaichuModel)
        inbuilt("rapidash", ::RapidashModel)
        inbuilt("rhydon", ::RhydonModel)
        inbuilt("rhyhorn", ::RhyhornModel)
        inbuilt("sandshrew", ::SandshrewModel)
        inbuilt("sandslash", ::SandslashModel)
        inbuilt("scyther", ::ScytherModel)
        inbuilt("seadra", ::SeadraModel)
        inbuilt("seaking", ::SeakingModel)
        inbuilt("seel", ::SeelModel)
        inbuilt("shellder", ::ShellderModel)
        inbuilt("slowbro", ::SlowbroModel)
        inbuilt("slowpoke", ::SlowpokeModel)
        inbuilt("snorlax", ::SnorlaxModel)
        inbuilt("spearow", ::SpearowModel)
        inbuilt("starmie", ::StarmieModel)
        inbuilt("staryu", ::StaryuModel)
        inbuilt("steelix", ::SteelixModel)
        inbuilt("tangela", ::TangelaModel)
        inbuilt("tauros", ::TaurosModel)
        inbuilt("tentacool", ::TentacoolModel)
        inbuilt("tentacruel", ::TentacruelModel)
        inbuilt("vaporeon", ::VaporeonModel)
        inbuilt("venomoth", ::VenomothModel)
        inbuilt("venonat", ::VenonatModel)
        inbuilt("victreebel", ::VictreebelModel)
        inbuilt("vulpix", ::VulpixModel)
        inbuilt("weepinbell", ::WeepinbellModel)
        inbuilt("weezing", ::WeezingModel)
        inbuilt("wigglytuff", ::WigglytuffModel)
        inbuilt("zapdos", ::ZapdosModel)
        inbuilt("elekid", ::ElekidModel)
        inbuilt("igglybuff", ::IgglybuffModel)
        inbuilt("magby", ::MagbyModel)
        inbuilt("pichu", ::PichuModel)
        inbuilt("smoochum", ::SmoochumModel)
        inbuilt("tyrogue", ::TyrogueModel)
        inbuilt("hitmontop", ::HitmontopModel)
        inbuilt("electivire", ::ElectivireModel)
        inbuilt("glaceon", ::GlaceonModel)
        inbuilt("happiny", ::HappinyModel)
        inbuilt("leafeon", ::LeafeonModel)
        inbuilt("lickilicky", ::LickilickyModel)
        inbuilt("magmortar", ::MagmortarModel)
        inbuilt("magnezone", ::MagnezoneModel)
        inbuilt("mimejr", ::MimejrModel)
        inbuilt("munchlax", ::MunchlaxModel)
        inbuilt("porygon2", ::Porygon2Model)
        inbuilt("porygonz", ::PorygonzModel)
        inbuilt("rhyperior", ::RhyperiorModel)
        inbuilt("scizor", ::ScizorModel)
        inbuilt("tangrowth", ::TangrowthModel)
        inbuilt("sylveon", ::SylveonModel)
        inbuilt("umbreon", ::UmbreonModel)
        inbuilt("espeon", ::EspeonModel)
        inbuilt("blissey", ::BlisseyModel)
        inbuilt("kingdra", ::KingdraModel)
        inbuilt("piloswine", ::PiloswineModel)
        inbuilt("quagsire", ::QuagsireModel)
        inbuilt("slowking", ::SlowkingModel)
        inbuilt("swinub", ::SwinubModel)
        inbuilt("wooper", ::WooperModel)
        inbuilt("yanma", ::YanmaModel)
        inbuilt("blaziken", ::BlazikenModel)
        inbuilt("combusken", ::CombuskenModel)
        inbuilt("marshtomp", ::MarshtompModel)
        inbuilt("minun", ::MinunModel)
        inbuilt("mudkip", ::MudkipModel)
        inbuilt("plusle", ::PlusleModel)
        inbuilt("rayquaza", ::RayquazaModel)
        inbuilt("swampert", ::SwampertModel)
        inbuilt("torchic", ::TorchicModel)
        inbuilt("bibarel", ::BibarelModel)
        inbuilt("bidoof", ::BidoofModel)
        inbuilt("buneary", ::BunearyModel)
        inbuilt("empoleon", ::EmpoleonModel)
        inbuilt("lopunny", ::LopunnyModel)
        inbuilt("mamoswine", ::MamoswineModel)
        inbuilt("pachirisu", ::PachirisuModel)
        inbuilt("piplup", ::PiplupModel)
        inbuilt("prinplup", ::PrinplupModel)
        inbuilt("yanmega", ::YanmegaModel)
        inbuilt("basculin", ::BasculinModel)
        inbuilt("crustle", ::CrustleModel)
        inbuilt("dwebble", ::DwebbleModel)
        inbuilt("emolga", ::EmolgaModel)
        inbuilt("maractus", ::MaractusModel)
        inbuilt("bounsweet", ::BounsweetModel)
        inbuilt("dartrix", ::DartrixModel)
        inbuilt("decidueye", ::DecidueyeModel)
        inbuilt("incineroar", ::IncineroarModel)
        inbuilt("litten", ::LittenModel)
        inbuilt("mimikyu", ::MimikyuModel)
        inbuilt("naganadel", ::NaganadelModel)
        inbuilt("poipole", ::PoipoleModel)
        inbuilt("rowlet", ::RowletModel)
        inbuilt("steenee", ::SteeneeModel)
        inbuilt("torracat", ::TorracatModel)
        inbuilt("tsareena", ::TsareenaModel)
        inbuilt("centiskorch", ::CentiskorchModel)
        inbuilt("sizzlipede", ::SizzlipedeModel)
        inbuilt("kleavor", ::KleavorModel)
        inbuilt("pyukumuku", ::PyukumukuModel)
        inbuilt("deerling", ::DeerlingModel)
        inbuilt("sawsbuck", ::SawsbuckModel)
        inbuilt("sableye", ::SableyeModel)
        inbuilt("natu", ::NatuModel)
        inbuilt("xatu", ::XatuModel)
        inbuilt("wailmer", ::WailmerModel)
        inbuilt("wailord", ::WailordModel)
        inbuilt("murkrow", ::MurkrowModel)
        inbuilt("honchkrow", ::HonchkrowModel)
    }

    fun inbuilt(name: String, model: (ModelPart) -> PokemonPoseableModel) {
        posers[cobblemonResource(name)] = model
    }



    fun registerJsonPosers(resourceManager: ResourceManager) {
        resourceManager.findResources("bedrock/posers") { path -> path.endsWith(".json") }.forEach { identifier, resource ->
            resource.inputStream.use { stream ->
                val json = String(stream.readAllBytes(), StandardCharsets.UTF_8)
                val resolvedIdentifier = Identifier(identifier.namespace, File(identifier.path).nameWithoutExtension)
                posers[resolvedIdentifier] = {
                    JsonPokemonPoseableModel.JsonPokemonPoseableModelAdapter.modelPart = it
                    JsonPokemonPoseableModel.gson.fromJson(json, JsonPokemonPoseableModel::class.java)
                }
            }
        }
    }

    fun registerSpeciesAssetResolvers(resourceManager: ResourceManager) {
        val speciesToSpeciesVariationSets = mutableMapOf<Identifier, MutableList<SpeciesVariationSet>>()
        resourceManager.findResources("bedrock/species") { path -> path.endsWith(".json") }.forEach { identifier, resource ->
            resource.inputStream.use { stream ->
                val json = String(stream.readAllBytes(), StandardCharsets.UTF_8)
                val speciesVariationSet = RegisteredSpeciesRendering.GSON.fromJson<SpeciesVariationSet>(json)
                speciesToSpeciesVariationSets.getOrPut(speciesVariationSet.species) { mutableListOf() }.add(speciesVariationSet)
            }
        }

        for ((species, speciesVariationSets) in speciesToSpeciesVariationSets) {
            val variations = speciesVariationSets.sortedBy { it.order }.flatMap { it.variations }.toMutableList()
            renders[species] = RegisteredSpeciesRendering(species, variations)
        }

        renders.values.forEach(RegisteredSpeciesRendering::initialize)
    }

    override fun registerAll() {
    }

    val texturedModels = mutableMapOf<Identifier, TexturedModel>()
    fun registerModels(resourceManager: ResourceManager) {
        var models = 0
        resourceManager.findResources("bedrock/models") { path -> path.endsWith(".geo.json") }.forEach { identifier, resource ->
            resource.inputStream.use { stream ->
                val json = String(stream.readAllBytes(), StandardCharsets.UTF_8)
                val resolvedIdentifier = Identifier(identifier.namespace, File(identifier.path).nameWithoutExtension)
                texturedModels[resolvedIdentifier] = TexturedModel.from(json)
                models++
            }
        }
        Cobblemon.LOGGER.info("Loaded $models Pokémon models.")
    }

    override fun reload(resourceManager: ResourceManager) {
        Cobblemon.LOGGER.info("Loading Pokémon models...")
        this.renders.clear()
        this.posers.clear()
        registerPosers(resourceManager)
        registerModels(resourceManager)
        registerSpeciesAssetResolvers(resourceManager)
        initializeModelLayers()
    }

    fun getPoser(species: Species, aspects: Set<String>): PokemonPoseableModel {
        try {
            val poser = this.renders[species.resourceIdentifier]?.getPoser(aspects)
            if (poser != null) {
                return poser
            }
        } catch(e: IllegalStateException) {
//            e.printStackTrace()
        }
        return this.renders[cobblemonResource("substitute")]!!.getPoser(aspects)
    }

    fun getTexture(species: Species, aspects: Set<String>): Identifier {
        try {
            val texture = this.renders[species.resourceIdentifier]?.getTexture(aspects)
            if (texture != null) {
                if (texture.exists()) {
                    return texture
                } else if (SHINY_ASPECT.aspect in aspects) {
                    // If the shiny texture doesn't exist, try parsing again but without the shiny - it doesn't seem to be implemented.
                    return getTexture(species, aspects - SHINY_ASPECT.aspect)
                }
            }
        } catch(_: IllegalStateException) { }
        return this.renders[cobblemonResource("substitute")]!!.getTexture(aspects)
    }

    fun getLayers(species: Species, aspects: Set<String>): Iterable<ModelLayer> {
        try {
            val layers = this.renders[species.resourceIdentifier]?.getLayers(aspects)
            if (layers != null) {
                return layers
            }
        } catch(_: IllegalStateException) { }
        return this.renders[cobblemonResource("substitute")]!!.getLayers(aspects)
    }
}