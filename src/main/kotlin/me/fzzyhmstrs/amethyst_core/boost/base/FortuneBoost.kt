package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.util.Identifier

class FortuneBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"fortune_boost"), Enchantments.FORTUNE, Enchantments.FORTUNE.maxLevel) {

    override fun provideLevel(enchantment: Enchantment): Int {
        return if (enchantment == Enchantments.FORTUNE) 3 else 0
    }
}