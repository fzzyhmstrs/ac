package me.fzzyhmstrs.amethyst_core.boost

import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.EnchantmentLevelEntry
import net.minecraft.item.EnchantedBookItem
import net.minecraft.item.ItemStack
import net.minecraft.item.Items

abstract class EnchantmentAugmentBoost(private val enchantment: Enchantment, private val level: Int): AugmentBoost() {

    private val stack: ItemStack by lazy{
        val book = ItemStack(Items.ENCHANTED_BOOK)
        EnchantedBookItem.addEnchantment(book, EnchantmentLevelEntry(enchantment, level))
        book
    }

    open fun provideLevel(enchantment: Enchantment): Int{
        return 0
    }

    override fun matches(stack: ItemStack): Boolean {
        return EnchantmentHelper.getLevel(enchantment, stack) >= level
    }

    override fun asStack(): ItemStack {
        return stack
    }
}