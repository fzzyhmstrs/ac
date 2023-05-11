package me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired

import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI

class ModificationInfo private constructor(
    val cooldownType: ModificationType, val cooldownModifier: PerLvlI,
    val manaCostType: ModificationType, val manaCostModifier: PerLvlI,
    val pairingCostMultiplier: Float,
    val damageModificationType: ModificationType,
    val amplifierModificationType: ModificationType,
    val durationModificationType: ModificationType,
    val rangeModificationType: ModificationType
) {

    companion object{
        fun empty(): ModificationInfo {
            return ModificationInfo(
                ModificationType.DEFER,PerLvlI(),
                                    ModificationType.DEFER,PerLvlI(),
                                    1f,
                                    ModificationType.DEFER,
                                    ModificationType.DEFER,
                                    ModificationType.DEFER,
                                    ModificationType.DEFER
            )
        }

        class Builder{
            var cooldownType: ModificationType = ModificationType.DEFER
            var cooldownModifier: PerLvlI = PerLvlI()
            var manaCostType: ModificationType = ModificationType.DEFER
            var manaCostModifier: PerLvlI = PerLvlI()
            var pairingCostMultiplier: Float = 1f
            var damageModificationType: ModificationType = ModificationType.DEFER
            var amplifierModificationType: ModificationType = ModificationType.DEFER
            var durationModificationType: ModificationType = ModificationType.DEFER
            var rangeModificationType: ModificationType = ModificationType.DEFER
        }

    }

}
