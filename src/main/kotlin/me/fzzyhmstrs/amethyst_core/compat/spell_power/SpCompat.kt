package me.fzzyhmstrs.amethyst_core.compat.spell_power

import me.fzzyhmstrs.amethyst_core.compat.spell_power.SpCompat.SpellPowerCast
import me.fzzyhmstrs.amethyst_core.registry.RegisterTag
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.TagKey
import net.minecraft.world.World
import net.spell_power.api.MagicSchool
import net.spell_power.api.SpellPower
import net.spell_power.api.attributes.EntityAttributes_SpellPower
import kotlin.math.log10

object SpCompat {

    val AFTER_CAST: Event<SpellPowerCast> = EventFactory.createArrayBacked(SpellPowerCast::class.java){ listeners ->
        SpellPowerCast { world, user, stack, spell, schools ->
            for (listener in listeners){
                listener.onSpellPowerCast(world, user, stack, spell, schools)
            }
        }
    }

    @FunctionalInterface
    fun interface SpellPowerCast{
        fun onSpellPowerCast(world: World, user: LivingEntity, stack: ItemStack, spell: ScepterAugment, schools: Set<MagicSchool>)
    }

    fun fireOnSpellPowerCast(world: World, user: LivingEntity, stack: ItemStack, spell: ScepterAugment){
        val schools = getSchoolsForSpell(spell)
        AFTER_CAST.invoker().onSpellPowerCast(world, user, stack, spell, schools)
        //println("Spell cast for SPA: $spell, with schools:$schools")
    }

    fun getHaste(user: LivingEntity, stack: ItemStack): Double{
        return SpellPower.getHaste(user,stack)
    }

    fun getModFromSpell(user: LivingEntity, spell: ScepterAugment): Pair<Double,Double>{
        val schools = getSchoolsForSpell(spell)
        var value = 0.0
        var valueCheck = 0.0
        for (school in schools) {
            val power = SpellPower.getSpellPower(school, user)
            value += power.randomValue()
            valueCheck += power.nonCriticalValue()
        }
        //spell power is like 10, 30, 50, 100 depending on the power scaling of that mod.
        // add 10 stops negative numbers. SP 0 -> multiplier 0
        // divide 10 will park "vanilla" numbers into a smaller boost range
        // Want small powers to give 10, 20, 30% boost. Big multipliers 50, 70, 100% boost.
        // log10 puts the multiplier from 0.2 to 1.0 SP 5 -> 100
        return if(valueCheck == value)
            Pair(1.0,log10((12.0 + valueCheck) / 12.0) * 100.0)
        else
            Pair(value/valueCheck,log10((12.0 + valueCheck) / 12.0) * 100.0)
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

    private fun getSchoolsForSpell(spell: ScepterAugment): Set<MagicSchool>{
        val set = mutableSetOf<MagicSchool>()
        if (FzzyPort.ENCHANTMENT.isInTag(spell,RegisterTag.FIRE_AUGMENTS)){
            set.add(MagicSchool.FIRE)
        }
        if ( FzzyPort.ENCHANTMENT.isInTag(spell,RegisterTag.LIGHTNING_AUGMENTS)){
            set.add(MagicSchool.LIGHTNING)
        }
        if ( FzzyPort.ENCHANTMENT.isInTag(spell,RegisterTag.ICE_AUGMENTS)){
            set.add(MagicSchool.FROST)
        }
        if ( FzzyPort.ENCHANTMENT.isInTag(spell,RegisterTag.SOUL_AUGMENTS)){
            set.add(MagicSchool.SOUL)
        }
        if ( FzzyPort.ENCHANTMENT.isInTag(spell,RegisterTag.HEALER_AUGMENTS)){
            set.add(MagicSchool.HEALING)
        }
        if ( FzzyPort.ENCHANTMENT.isInTag(spell,RegisterTag.ARCANE_AUGMENTS)){
            set.add(MagicSchool.ARCANE)
        }
        return set
    }

    fun getAttributeFromEnum(attr: SpChecker.Power): EntityAttribute?{
        return when(attr){
            SpChecker.Power.CRITICAL_CHANCE -> EntityAttributes_SpellPower.CRITICAL_CHANCE
            SpChecker.Power.CRITICAL_DAMAGE -> EntityAttributes_SpellPower.CRITICAL_DAMAGE
            SpChecker.Power.HASTE -> EntityAttributes_SpellPower.HASTE
            SpChecker.Power.ARCANE -> EntityAttributes_SpellPower.POWER[MagicSchool.ARCANE]
            SpChecker.Power.FIRE -> EntityAttributes_SpellPower.POWER[MagicSchool.FIRE]
            SpChecker.Power.FROST -> EntityAttributes_SpellPower.POWER[MagicSchool.FROST]
            SpChecker.Power.HEALING -> EntityAttributes_SpellPower.POWER[MagicSchool.HEALING]
            SpChecker.Power.LIGHTNING -> EntityAttributes_SpellPower.POWER[MagicSchool.LIGHTNING]
            SpChecker.Power.SOUL -> EntityAttributes_SpellPower.POWER[MagicSchool.SOUL]
        }
    }

}
