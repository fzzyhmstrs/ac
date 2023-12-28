package me.fzzyhmstrs.amethyst_core.compat.spell_power

import com.google.common.collect.Multimap
import me.fzzyhmstrs.amethyst_core.event.AfterSpellEvent
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributeModifier.Operation
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World
import net.spell_power.api.MagicSchool
import net.spell_power.api.attributes.EntityAttributes_SpellPower
import java.util.UUID

object SpChecker {

    val spellPowerLoaded: Boolean by lazy{
        FabricLoader.getInstance().isModLoaded("spell_power")
    }

    fun fireOnSpellPowerCast(world: World, user: LivingEntity, stack: ItemStack, spell: ScepterAugment){
        if (spellPowerLoaded)
            SpCompat.fireOnSpellPowerCast(world, user, stack, spell)
    }

    fun getHaste(user: LivingEntity, stack: ItemStack): Double{
        if (spellPowerLoaded)
            return SpCompat.getHaste(user,stack)
        return 1.0
    }

    fun getModFromSpell(user: LivingEntity, spell: ScepterAugment): Pair<Double,Double>{
        if (spellPowerLoaded)
            return SpCompat.getModFromSpell(user, spell)
        return Pair(1.0,0.0)
    }

    fun getModFromTags(user: LivingEntity, vararg tagKeys: TagKey<Enchantment>): Double{
        if (spellPowerLoaded)
            return SpCompat.getModFromTags(user, *tagKeys)
        return 0.0
    }

    fun addSpellPowerAttribute(power: Power, uuid: String, amount: Double, operation: EntityAttributeModifier.Operation, map: Multimap<EntityAttribute, EntityAttributeModifier>){
        if (spellPowerLoaded){
            val attribute = SpCompat.getAttributeFromEnum(power) ?: return
            val uUID =  UUID.fromString(uuid)
            map.put(attribute, EntityAttributeModifier(uUID, power.id, amount, operation))
        }
    }
    
    enum class Power(val id: String){
        CRITICAL_CHANCE("ac_crit_chance"),
        CRITICAL_DAMAGE("ac_crit_damage"),
        HASTE("ac_haste"),
        ARCANE("ac_arcane"),
        FIRE("ac_fire"),
        FROST("ac_frost"),
        HEALING("ac_healing"),
        LIGHTNING("ac_lightning"),
        SOUL("ac_soul")
    }

    /**
     * Attribute uses MULTIPLY_TOTAL operation
     */
    fun getSpellCooldownModifier(percent: Int, uuid: UUID, name: String): Pair<EntityAttribute,EntityAttributeModifier>{
        if (percent > 99 || percent < -1000) throw IllegalStateException("Percentage $percent out of bounds for creating a spell cooldown multiplier")
        if (spellPowerLoaded) {
            val multiplier = MathHelper.lerp(if(percent < 0) 0.0 else percent/99.0,0.0,9.0)
            return Pair(EntityAttributes_SpellPower.HASTE, EntityAttributeModifier(uuid, name, multiplier, Operation.MULTIPLY_TOTAL))
        }
        return Pair(RegisterAttribute.SPELL_COOLDOWN, EntityAttributeModifier(uuid, name, percent/100.0, Operation.MULTIPLY_TOTAL))
    }

    /**
     * Attribute uses ADDITION operation
     */
    fun getSpellCritChanceModifier(percent: Int, uuid: UUID, name: String): Pair<EntityAttribute,EntityAttributeModifier>{
        if (percent > 100 || percent < 0) throw IllegalStateException("Percentage $percent out of bounds for creating a spell crit chance")
        if (spellPowerLoaded) {
            return Pair(EntityAttributes_SpellPower.CRITICAL_CHANCE, EntityAttributeModifier(uuid, name, percent.toDouble(), Operation.ADDITION))
        }
        return Pair(RegisterAttribute.SPELL_CRITICAL_CHANCE, EntityAttributeModifier(uuid, name, percent/100.0,  Operation.ADDITION))
    }

    /**
     * Attribute uses any operation
     */
    fun getSpellCritDamageModifier(percent: Int, uuid: UUID, name: String, operation: Operation): Pair<EntityAttribute,EntityAttributeModifier>{
        if (percent > 1000 || if (operation == Operation.ADDITION) percent < 0 else percent < 100) throw IllegalStateException("Percentage $percent out of bounds for creating a spell crit damage multiplier")
        if (operation != Operation.ADDITION) {
            val multiplier = (percent - 100)/100.0
            if (spellPowerLoaded) {
                return Pair(EntityAttributes_SpellPower.CRITICAL_DAMAGE, EntityAttributeModifier(uuid, name, multiplier, operation))
            }
            return Pair(RegisterAttribute.SPELL_CRITICAL_MULTIPLIER, EntityAttributeModifier(uuid, name, multiplier, operation))
        } else {

            if (spellPowerLoaded) {
                return Pair(EntityAttributes_SpellPower.CRITICAL_DAMAGE, EntityAttributeModifier(uuid, name, percent.toDouble(), Operation.ADDITION))
            }
            return Pair(RegisterAttribute.SPELL_CRITICAL_MULTIPLIER, EntityAttributeModifier(uuid, name, percent / 100.0,  Operation.ADDITION))
        }
    }
}
