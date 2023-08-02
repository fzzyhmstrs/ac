package me.fzzyhmstrs.amethyst_core.scepter

import me.fzzyhmstrs.fzzy_core.mana_util.ManaItemMaterial
import net.minecraft.item.ToolMaterial

/**
 * A tool material for defining a scepter. The Scepter tier is used to determine which augments can be applied to which scepters. See [LoreTier] for more info.
 */
open class ScepterToolMaterial protected constructor(
    private val tier: Int,
    attackSpeedDefault: ValidatedDouble,
    durabilityDefault: ValidatedInt,
    miningSpeedDefault: ValidatedFloat,
    attackDamageDefault: ValidatedFloat,
    miningLevelDefault: ValidatedInt,
    enchantabilityDefault: ValidatedInt,
    repairIngredientDefault: ValidatedIngredient)
: 
ValidatedToolMaterial(
    durabilityDefault,
    miningSpeedDefault,
    attackDamageDefault,
    miningLevelDefault,
    enchantabilityDefault,
    repairIngredientDefault)
,
ManaItemMaterial 
{
    var attackSpeed = attackSpeedDefault
    
    fun scepterTier(): Int{
        return tier
    }

    fun getAttackSpeed(): Double{
        return attackSpeed.get()
    }

    open class Builder(private val tier: Int): ValidatedToolMaterial.AbstractBuilder<ScepterToolMaterial, Builder>(){
        protected var aS = ValidatedFloat(-3f,0f,-4f)

        fun attackSpeed(default: Float): Builder{
            aS = ValidatedFloat(default,0f,-4f)
        }

        override fun builderClass(): Builder{
            return this
        }
        
        override fun build(): ScepterToolMaterial{
            return ScepterToolMaterial(tier, aS, d, mSM, aD, mL, e, rI)
        }
        
    }
  
}
