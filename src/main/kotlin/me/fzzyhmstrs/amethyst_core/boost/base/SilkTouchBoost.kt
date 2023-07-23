package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier

class SilkTouchBoost: EnchantmentAugmentBoost(AC.identity("silk_touch_boost"), Enchantments.SILK_TOUCH, 1) {

    override fun modifyStack(stack: ItemStack): ItemStack {
        val stack2 = stack.copy()
        stack2.addEnchantment(Enchantments.SILK_TOUCH,1)
        return stack2
    }
}