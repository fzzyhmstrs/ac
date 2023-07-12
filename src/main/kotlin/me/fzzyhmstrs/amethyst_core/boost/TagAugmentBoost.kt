package me.fzzyhmstrs.amethyst_core.boost

import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

open class TagAugmentBoost(id: Identifier, private val tag: TagKey<Item>): AugmentBoost(id) {

    private val stacks: List<ItemStack> by lazy{
        val rawList = Registries.ITEM.getEntryList(tag)
        if (rawList.isEmpty) return@lazy listOf(ItemStack.EMPTY)
        val stackList = rawList.get().stream().map { it -> ItemStack(it.value()) }.toList()
        stackList
    }

    override fun matches(stack: ItemStack): Boolean {
        return stack.isIn(tag)
    }

    override fun asStacks(): Collection<ItemStack> {
        return stacks
    }

}