package me.fzzyhmstrs.amethyst_core.augments.data

import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.scepter.LoreTier
import me.fzzyhmstrs.amethyst_core.scepter.SpellType
import me.fzzyhmstrs.fzzy_config.config_util.SyncedConfigHelperV1
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.util.Identifier
import java.util.function.Consumer

/**
 * Data class defines the characteristics of a [ScepterAugment].
 *
 * [type]: The [SpellType] for the augment. This both organizes the spell flavor-wise, and also tells an Augment Scepter to increment the correct level stat. See [SpellType] for recommendations of using the three types.
 *
 * [cooldown]: time in ticks between each spell cast.
 *
 * [manaCost]: the mana damage inflicted on the Augment Scepter with each successful cast
 *
 * [minLvl]: the minimum [SpellType] level the scepter must be before the augment can be added to the scepter
 *
 * [imbueLevel]: The cost in Experience Levels to imbue this augment on the Imbuing Table. Typically defined in the Imbuing Recipe json
 *
 * [bookOfLoreTier]: Defines which Knowledge Book the augment can show up in. see [LoreTier] for more info. "NO_TIER" will not add the augment to any book. Typically used for augments that have pre-available crafting recipes the player can use without a Knowledge Book.
 *
 * [keyItem]: If the recipe follows a templated pattern, setting this will give the player a hint as to how to craft this augment without REI or similar.
 *
 * [enabled]: (new in 0.3.1) Determines if the augment is enabled by the configuration settings. If not enabled, a registry can opt to skip it or otherwise exclude it from showing up during play.
 *
 * [pvpMode]: (new in 1.1.0)
 */

class AugmentDatapoint(
    val id: Identifier,
    val type: SpellType,
    cooldown: PerLvlI = PerLvlI(20,0,0),
    manaCost: Int = 20,
    minLvl: Int = 1,
    maxLvl: Int = 1,
    var imbueLevel: Int = 1,
    castXp: Int = 1,
    val bookOfLoreTier: LoreTier = LoreTier.NO_TIER,
    val keyItem: Item = Items.AIR,
    version: UInt = 0u,
    updater: Consumer<AugmentDataConfig> = Consumer{_ -> })
{

    constructor(id: Identifier,
        type: SpellType = SpellType.NULL,
        cooldown: Int = 20,
        manaCost: Int = 20,
        minLvl: Int = 1,
        maxLvl: Int = 1,
        imbueLevel: Int = 1,
        castXp: Int = 1,
        bookOfLoreTier: LoreTier = LoreTier.NO_TIER,
        keyItem: Item = Items.AIR,
        version: UInt = 0u,
        updater: Consumer<AugmentDataConfig> = Consumer{_ -> })
        :
        this(id,type,PerLvlI(cooldown),manaCost, minLvl,maxLvl, imbueLevel, castXp, bookOfLoreTier, keyItem, version, updater)

    private var config = AugmentDataConfig(id.toString(),updater,cooldown,manaCost,minLvl,maxLvl,castXp)
    
    init{
        config = if (version == 0u){
            SyncedConfigHelperV1.readOrCreateAndValidate("${id.path + "_v" + version.toString()}.json","spells",id.namespace) {config}
        } else {
            SyncedConfigHelperV1.readOrCreateUpdatedAndValidate("${id.path + "_v" + version.toString()}.json","${id.path + "_v" + (version - 1u).toString()}.json","spells",id.namespace,{config}) {config}
        }
    }

    val cooldown: PerLvlI
        get() = config.cooldown.get()

    val manaCost: Int
        get() = config.manaCost.get()

    val minLvl: Int
        get() = config.minLvl.get()

    val maxLvl: Int
        get() = config.maxLvl.get()

    val castXp: Int
        get() = config.castXP.get()

    val enabled: Boolean
        get() = config.enabled.get()

    val pvpMode: Boolean
        get() = config.pvpMode.get()
}