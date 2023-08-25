package me.fzzyhmstrs.amethyst_core.compat.spell_power

import me.fzzyhmstrs.amethyst_core.registry.RegisterTag
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.spell_power.api.MagicSchool
import net.spell_power.api.SpellPower
import kotlin.math.log10

object SpCompat {

    fun getHaste(user: LivingEntity, stack: ItemStack): Double{
        return SpellPower.getHaste(user,stack)
    }

    fun getModFromPower(user: LivingEntity, spell: ScepterAugment): Double{
        val school = getSchoolForSpell(spell) ?: return 0.0
        val power = SpellPower.getSpellPower(school,user)
        val value = power.randomValue()
        //spell power is like 10, 30, 50, 100 depending on the power scaling of that mod.
        // add 10 stops negative numbers. SP 0 -> multiplier 0
        // divide 10 will park "vanilla" numbers into a smaller boost range
        // Want small powers to give 10, 20, 30% boost. Big multipliers 50, 70, 100% boost.
        // log10 puts the multiplier from 0.2 to 1.0 SP 5 -> 100
        return  log10((10.0 + value) / 10.0) * 100.0
    }

    private fun getSchoolForSpell(spell: ScepterAugment): MagicSchool?{
        return if (spell.isIn(RegisterTag.FIRE_AUGMENTS)){
            MagicSchool.FIRE
        } else if (spell.isIn(RegisterTag.LIGHTNING_AUGMENTS)){
            MagicSchool.LIGHTNING
        } else if (spell.isIn(RegisterTag.ICE_AUGMENTS)){
            MagicSchool.FROST
        } else if (spell.isIn(RegisterTag.SOUL_AUGMENTS)){
            MagicSchool.SOUL
        } else if (spell.isIn(RegisterTag.HEALER_AUGMENTS)){
            MagicSchool.HEALING
        }else if (spell.isIn(RegisterTag.ARCANE_AUGMENTS)){
            MagicSchool.ARCANE
        } else {
            null
        }
    }

}