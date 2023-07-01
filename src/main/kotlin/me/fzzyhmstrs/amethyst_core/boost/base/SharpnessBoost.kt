package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import net.minecraft.enchantment.Enchantments
import net.minecraft.util.Identifier

class SharpnessBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"sharpness_boost"), Enchantments.SHARPNESS, 5) {
    override val boostEffect: AugmentEffect
        get() = super.boostEffect.withDamage(0f,0f,10f)
}