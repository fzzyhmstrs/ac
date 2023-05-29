package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.enchantment.Enchantments
import net.minecraft.util.Identifier

class MendingBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"mending_boost"), Enchantments.MENDING, 1) {

    override val manaCostModifier: PerLvlI
        get() = PerLvlI(0,0,-10)
}