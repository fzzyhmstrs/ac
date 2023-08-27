package me.fzzyhmstrs.amethyst_core.scepter_util.data

import me.fzzyhmstrs.fzzy_config.interfaces.OldClass
import me.fzzyhmstrs.fzzy_config.interfaces.SyncedConfig
import me.fzzyhmstrs.fzzy_config.registry.SyncedConfigRegistry
import me.fzzyhmstrs.fzzy_config.validated_field.ValidatedBoolean
import me.fzzyhmstrs.fzzy_config.validated_field.ValidatedInt
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import java.util.function.Consumer

open class AugmentDataConfig(
    private val configName: String,
    private val updater: Consumer<AugmentDataConfig>,
    cooldown: PerLvlI,
    manaCost: Int,
    minLvl: Int,
    maxLvl: Int,
    castXP: Int)
    :
    SyncedConfig, OldClass<AugmentDataConfig>
{
    override fun initConfig() {
        SyncedConfigRegistry.registerConfig(configName,this)
    }

    var enabled: ValidatedBoolean = ValidatedBoolean(true)
    var pvpMode: ValidatedBoolean = ValidatedBoolean(false)
    var cooldown: ValidatedPerLvlI = ValidatedPerLvlI(cooldown)
    var manaCost: ValidatedInt = ValidatedInt(manaCost, Int.MAX_VALUE,1)
    var minLvl: ValidatedInt = ValidatedInt(minLvl, Int.MAX_VALUE,1)
    var maxLvl: ValidatedInt = ValidatedInt(maxLvl, Int.MAX_VALUE,1)
    var castXP: ValidatedInt = ValidatedInt(castXP, Int.MAX_VALUE,0)
    override fun generateNewClass(): AugmentDataConfig {
        val new = this
        updater.accept(new)
        return new
    }

}