package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import net.minecraft.enchantment.Enchantments
import net.minecraft.util.Identifier

class SharpnessBoost: EnchantmentAugmentBoost(AC.identity("sharpness_boost"), Enchantments.SHARPNESS, Enchantments.SHARPNESS.maxLevel) {
    override val boostEffect: AugmentEffect
        get() = super.boostEffect.withDamage(0f,0f,10f)
}