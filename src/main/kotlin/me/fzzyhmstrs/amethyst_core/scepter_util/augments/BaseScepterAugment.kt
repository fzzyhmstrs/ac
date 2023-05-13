package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.UniqueAugmentModifier
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

/**
 * the base scepter augment. Any Augment-type scepter will be able to successfully cast an augment made with this class or one of the templates.
 */

abstract class BaseScepterAugment()
    :
    Enchantment(Rarity.VERY_RARE,EnchantmentTarget.WEAPON, arrayOf(EquipmentSlot.MAINHAND))
{

    val id: Identifier? by lazy {
        Registries.ENCHANTMENT.getId(this)
    }
    val augmentSpecificModifier: AugmentModifier by lazy {
        generateUniqueModifier()
    }

    open fun generateUniqueModifier(): AugmentModifier{
        val augId = id?: return AugmentModifier(Identifier(AC.MOD_ID,"spell_boost"),2,-25.0,-15.0,false)
        return UniqueAugmentModifier(augId,2,-25.0,-15.0)
    }

    override fun getMinPower(level: Int): Int {
        return 30
    }

    override fun getMaxPower(level: Int): Int {
        return 50
    }

    override fun getMaxLevel(): Int {
        return 1
    }

    override fun isTreasure(): Boolean {
        return true
    }

    override fun isAvailableForEnchantedBookOffer(): Boolean {
        return false
    }

    override fun isAvailableForRandomSelection(): Boolean {
        return false
    }
}
