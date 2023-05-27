package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.text.Text

class SilkTouchBoost: EnchantmentAugmentBoost(Enchantments.SILK_TOUCH,1) {

    override fun modifyStack(stack: ItemStack): ItemStack {
        val stack2 = stack.copy()
        stack2.addEnchantment(Enchantments.SILK_TOUCH,1)
        return stack2
    }

    override fun appendDescription(description: MutableList<Text>) {
        description.add(AcText.translatable("boost.amethyst_core.sharpness"))
    }
}