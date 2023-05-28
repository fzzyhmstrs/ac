package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.enchantment.Enchantments
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class QuickChargeBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"quick_charge_boost"), Enchantments.QUICK_CHARGE, 3) {

    override val cooldownModifier: PerLvlI
        get() = PerLvlI(-2,0,0)
}