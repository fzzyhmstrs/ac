package me.fzzyhmstrs.amethyst_core.compat.spell_power

import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.tag.TagKey

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

    fun addSpellPowerAttribute(power: String, uuid: String, amount: Double, operation: EntityAttributeModifier.Operation, map: Multimap<EntityAttribute, EntityAttributeModifier>){
        if (spellPowerLoaded){
            val attribute = Registries.ATTRIBUTE.get(Identifier(power)) ?: return
            val uUID =  UUID.fromString(uuid)
            map.put(attribute, EntityAttributeModifier(uUID, power, amount, operation))
        }
    }
}
