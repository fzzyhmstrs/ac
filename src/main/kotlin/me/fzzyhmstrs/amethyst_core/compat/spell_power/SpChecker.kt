package me.fzzyhmstrs.amethyst_core.compat.spell_power

import com.google.common.collect.Multimap
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.math.MathHelper
import net.spell_power.api.attributes.EntityAttributes_SpellPower
import java.util.UUID

object SpChecker {

    val spellPowerLoaded: Boolean by lazy{
        FabricLoader.getInstance().isModLoaded("spell_power")
    }

    fun getHaste(user: LivingEntity, stack: ItemStack): Double{
        if (spellPowerLoaded)
            return SpCompat.getHaste(user,stack)
        return 1.0
    }

    fun getModFromSpell(user: LivingEntity, spell: ScepterAugment): Double{
        if (spellPowerLoaded)
            return SpCompat.getModFromSpell(user, spell)
        return 0.0
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

    fun getSpellCooldownModifier(percent: Int, uuid: UUID, name: String): Pair<EntityAttribute,EntityAttributeModifier>{
        if (percent > 99 || percent < -1000) throw IllegalStateException("Percentage $percent out of bounds for creating a spell cooldown multiplier")
        if (spellPowerLoaded) {
            val multiplier = MathHelper.lerp(if(percent < 0) 0.0 else percent/99.0,0.0,9.0)
            return Pair(EntityAttributes_SpellPower.HASTE, EntityAttributeModifier(uuid, name, multiplier,EntityAttributeModifier.Operation.MULTIPLY_TOTAL))
        }
        return Pair(RegisterAttribute.SPELL_COOLDOWN, EntityAttributeModifier(uuid, name, percent/100.0, EntityAttributeModifier.Operation.MULTIPLY_TOTAL))
    }
}
