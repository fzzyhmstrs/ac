package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.Registries
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier

class UniqueAugmentModifier(
    private val augmentId: Identifier,
    levelModifier: Int = 0,
    cooldownModifier: Double = 0.0,
    manaCostModifier: Double = 0.0)
    :
    AugmentModifier(
        Identifier("$augmentId/modifier"),
        levelModifier,
        cooldownModifier,
        manaCostModifier,
        false)
{

    private val enchant: Enchantment? by lazy {
        Registries.ENCHANTMENT.get(augmentId)
    }

    override fun getTranslation(): MutableText {
        val name = enchant?.getName(1)?:AcText.literal("Unknown")
        return AcText.translatable("scepter.modifier.unique",name)
    }

    override fun getDescTranslation(): MutableText {
        val name = enchant?.getName(1)?:AcText.literal("Unknown")
        return AcText.translatable("scepter.modifier.unique.desc",name)
    }
}