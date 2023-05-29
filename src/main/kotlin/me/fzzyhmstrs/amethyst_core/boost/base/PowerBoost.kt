package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import net.minecraft.enchantment.Enchantments
import net.minecraft.util.Identifier

class PowerBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"power_boost"), Enchantments.POWER, 5) {
    override val boostEffect: AugmentEffect
        get() = super.boostEffect.withAmplifier(1)
}