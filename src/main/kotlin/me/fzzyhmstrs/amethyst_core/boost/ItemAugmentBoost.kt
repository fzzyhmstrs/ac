package me.fzzyhmstrs.amethyst_core.boost

import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

abstract class ItemAugmentBoost(id: Identifier,private val item: Item): AugmentBoost(id) {

    private val stack: ItemStack by lazy{
        ItemStack(item)
    }

    override fun matches(stack: ItemStack, augment: ScepterAugment): Boolean {
        return stack.isOf(item)
    }

    override fun asStack(): ItemStack {
        return stack
    }

}