package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.enchantment.Enchantments
import net.minecraft.text.Text

class MendingBoost: EnchantmentAugmentBoost(Enchantments.MENDING,1) {

    override val manaCostModifier: PerLvlI
        get() = PerLvlI(0,0,-10)

    override fun appendDescription(description: MutableList<Text>) {
        description.add(AcText.translatable("boost.amethyst_core.mending"))
    }
}