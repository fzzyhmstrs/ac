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

        class Builder(multiplier: Float = 1f){
            private var cooldownType: ModificationType = ModificationType.DEFER
            private var cooldownModifier: PerLvlI = PerLvlI()
            private var manaCostType: ModificationType = ModificationType.DEFER
            private var manaCostModifier: PerLvlI = PerLvlI()
            private var pairingCostMultiplier: Float = multiplier
            private var damageModificationType: ModificationType = ModificationType.DEFER
            private var amplifierModificationType: ModificationType = ModificationType.DEFER
            private var durationModificationType: ModificationType = ModificationType.DEFER
            private var rangeModificationType: ModificationType = ModificationType.DEFER
            
            fun setCooldownType(type: ModificationType): Builder{
                this.cooldownType = type
                return this
            }
            fun withCooldownMod(base: Int = 0, perLvl: Int = 0, percent: Int = 0):Builder{
                cooldownModifier = PerLvlI(base,perLvl,percent)
                return this
            }
            fun setManaCostType(type: ModificationType): Builder{
                this.manaCostType = type
                return this
            }
            fun withManaCostMod(base: Int = 0, perLvl: Int = 0, percent: Int = 0):Builder{
                manaCostModifier = PerLvlI(base,perLvl,percent)
                return this
            }
            fun setDamageType(type: ModificationType): Builder{
                this.damageModificationType = type
                return this
            }
            fun setAmplifierType(type: ModificationType): Builder{
                this.amplifierModificationType = type
                return this
            }
            fun setDurationType(type: ModificationType): Builder{
                this.durationModificationType = type
                return this
            }
            fun setRangeType(type: ModificationType): Builder{
                this.rangeModificationType = type
                return this
            }
            
            fun build(): ModificationInfo{
                return ModificationInfo(
                    cooldownType, 
                    cooldownModifier,
                    manaCostType, 
                    manaCostModifier,
                    pairingCostMultiplier,
                    damageModificationType,
                    amplifierModificationType,
                    durationModificationType,
                    rangeModificationType)
            }
        }

    }

}
