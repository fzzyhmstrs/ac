package me.fzzyhmstrs.amethyst_core.compat.spell_power

import me.fzzyhmstrs.amethyst_core.registry.RegisterTag
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.TagKey
import net.spell_power.api.MagicSchool
import net.spell_power.api.SpellPower
import kotlin.math.log10

object SpCompat {

    fun getHaste(user: LivingEntity, stack: ItemStack): Double{
        return SpellPower.getHaste(user,stack)
    }

    fun getModFromSpell(user: LivingEntity, spell: ScepterAugment): Double{
        val schools = getSchoolsForSpell(spell)
        var value = 0.0
        for (school in schools) {
            val power = SpellPower.getSpellPower(school, user)
            value += power.randomValue()
        }
        //spell power is like 10, 30, 50, 100 depending on the power scaling of that mod.
        // add 10 stops negative numbers. SP 0 -> multiplier 0
        // divide 10 will park "vanilla" numbers into a smaller boost range
        // Want small powers to give 10, 20, 30% boost. Big multipliers 50, 70, 100% boost.
        // log10 puts the multiplier from 0.2 to 1.0 SP 5 -> 100
        return  log10((12.0 + value) / 12.0) * 100.0
    }

    fun getModFromTags(user: LivingEntity, vararg tagKeys: TagKey<Enchantment>): Double{
        var value = 0.0
        for (tagKey in tagKeys) {
            val school = getSchoolFromTag(tagKey) ?: continue
            val power = SpellPower.getSpellPower(school, user)
            value += power.randomValue()
        }
        //spell power is like 10, 30, 50, 100 depending on the power scaling of that mod.
        // add 10 stops negative numbers. SP 0 -> multiplier 0
        // divide 10 will park "vanilla" numbers into a smaller boost range
        // Want small powers to give 10, 20, 30% boost. Big multipliers 50, 70, 100% boost.
        // log10 puts the multiplier from 0.2 to 1.0 SP 5 -> 100
        return  log10((12.0 + value) / 12.0) * 100.0
    }

    private fun getSchoolsForSpell(spell: ScepterAugment): Set<MagicSchool>{
        val set = mutableSetOf<MagicSchool>()
        if (spell.isIn(RegisterTag.FIRE_AUGMENTS)){
            set.add(MagicSchool.FIRE)
        }
        if (spell.isIn(RegisterTag.LIGHTNING_AUGMENTS)){
            set.add(MagicSchool.LIGHTNING)
        }
        if (spell.isIn(RegisterTag.ICE_AUGMENTS)){
            set.add(MagicSchool.FROST)
        }
        if (spell.isIn(RegisterTag.SOUL_AUGMENTS)){
            set.add(MagicSchool.SOUL)
        }
        if (spell.isIn(RegisterTag.HEALER_AUGMENTS)){
            set.add(MagicSchool.HEALING)
        }
        if (spell.isIn(RegisterTag.ARCANE_AUGMENTS)){
            set.add(MagicSchool.ARCANE)
        }
        return set
    }

    private fun getSchoolFromTag(tagKey: TagKey<Enchantment>): MagicSchool?{
        return when (tagKey) {
            RegisterTag.FIRE_AUGMENTS -> MagicSchool.FIRE
            RegisterTag.LIGHTNING_AUGMENTS -> MagicSchool.LIGHTNING
            RegisterTag.ICE_AUGMENTS -> MagicSchool.FROST
            RegisterTag.SOUL_AUGMENTS -> MagicSchool.SOUL
            RegisterTag.HEALER_AUGMENTS -> MagicSchool.HEALING
            RegisterTag.ARCANE_AUGMENTS -> MagicSchool.ARCANE
            else -> null
        }
    }

}