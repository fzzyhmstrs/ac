package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class SilkTouchBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"silk_touch_boost"), Enchantments.SILK_TOUCH, 1) {

    override fun modifyStack(stack: ItemStack): ItemStack {
        val stack2 = stack.copy()
        stack2.addEnchantment(Enchantments.SILK_TOUCH,1)
        return stack2
    }
}