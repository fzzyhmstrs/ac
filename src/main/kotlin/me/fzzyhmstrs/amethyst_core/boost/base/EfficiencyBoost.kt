package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.enchantment.Enchantments
import net.minecraft.text.Text

class EfficiencyBoost: EnchantmentAugmentBoost(Enchantments.EFFICIENCY,5) {

    override val cooldownModifier: PerLvlI
        get() = PerLvlI(0,0,-10)

    override fun appendDescription(description: MutableList<Text>) {
        description.add(AcText.translatable("boost.amethyst_core.efficiency"))
    }
}