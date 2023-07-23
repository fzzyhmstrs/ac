package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.util.Identifier

class LootingBoost: EnchantmentAugmentBoost(AC.identity("looting_boost"), Enchantments.SHARPNESS, 3) {

    override fun provideLevel(enchantment: Enchantment): Int {
        return if (enchantment == Enchantments.LOOTING) 3 else 0
    }
}