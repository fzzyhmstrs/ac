package me.fzzyhmstrs.amethyst_core.boost

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

open class ItemAugmentBoost(id: Identifier,private val item: Item): AugmentBoost(id) {

    private val stacks: Set<ItemStack> by lazy{
        setOf(ItemStack(item))
    }

    override fun matches(stack: ItemStack): Boolean {
        return stack.isOf(item)
    }

    override fun asStacks(): Collection<ItemStack> {
        return stacks
    }

}