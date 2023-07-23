package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import net.minecraft.enchantment.Enchantments
import net.minecraft.util.Identifier

class PowerBoost: EnchantmentAugmentBoost(AC.identity("power_boost"), Enchantments.POWER, Enchantments.POWER.maxLevel) {
    override val boostEffect: AugmentEffect
        get() = super.boostEffect.withAmplifier(1)
}