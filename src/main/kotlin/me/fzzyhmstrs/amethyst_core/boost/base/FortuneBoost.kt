package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class FortuneBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"fortune_boost"), Enchantments.FORTUNE, 3) {

    override fun provideLevel(enchantment: Enchantment): Int {
        return if (enchantment == Enchantments.FORTUNE) 3 else 0
    }
}