package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import net.minecraft.enchantment.Enchantments
import net.minecraft.text.Text

class SharpnessBoost: EnchantmentAugmentBoost(Enchantments.SHARPNESS,5) {
    override val boostEffect: AugmentEffect
        get() = super.boostEffect.withDamage(0f,0f,10f)

    override fun appendDescription(description: MutableList<Text>) {
        description.add(AcText.translatable("boost.amethyst_core.sharpness"))
    }
}