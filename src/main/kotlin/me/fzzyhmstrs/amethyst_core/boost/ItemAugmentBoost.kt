package me.fzzyhmstrs.amethyst_core.boost

import net.minecraft.item.Item
import net.minecraft.item.ItemStack

abstract class ItemAugmentBoost(private val item: Item): AugmentBoost() {

    private val stack: ItemStack by lazy{
        ItemStack(item)
    }

    override fun matches(stack: ItemStack): Boolean {
        return stack.isOf(item)
    }

    override fun asStack(): ItemStack {
        return stack
    }

}