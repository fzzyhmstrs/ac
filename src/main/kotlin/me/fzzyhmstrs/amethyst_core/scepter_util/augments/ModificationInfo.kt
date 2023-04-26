package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI

class ModificationInfo private constructor(
    val cooldownType: ModificationType, val cooldownModifier: PerLvlI,
    val pairingCostMultiplier: Float,
    val damageModificationType: ModificationType,
    val amplifierModificationType: ModificationType,
    val durabilityModificationType: ModificationType,
    val rangeModificationType: ModificationType
) {

    companion object{
        fun empty(): ModificationInfo{
            return ModificationInfo(ModificationType.DEFER,PerLvlI(),1f)
        }

    }

}
