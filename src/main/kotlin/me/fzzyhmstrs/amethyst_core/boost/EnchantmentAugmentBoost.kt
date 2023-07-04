package me.fzzyhmstrs.amethyst_core.boost

import me.fzzyhmstrs.amethyst_core.augments.LevelProviding
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.EnchantmentLevelEntry
import net.minecraft.item.EnchantedBookItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.util.Identifier

abstract class EnchantmentAugmentBoost(id: Identifier, private val enchantment: Enchantment, private val level: Int): AugmentBoost(id),
    LevelProviding {

    private val stack: ItemStack by lazy{
        val book = ItemStack(Items.ENCHANTED_BOOK)
        EnchantedBookItem.addEnchantment(book, EnchantmentLevelEntry(enchantment, level))
        book
    }

    override fun matches(stack: ItemStack): Boolean {
        return EnchantmentHelper.getLevel(enchantment, stack) >= level
    }

    override fun asStack(): ItemStack {
        return stack
    }
}