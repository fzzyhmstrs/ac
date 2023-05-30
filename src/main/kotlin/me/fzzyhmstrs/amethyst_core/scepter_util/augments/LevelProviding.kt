package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import net.minecraft.enchantment.Enchantment

interface LevelProviding {
   fun provideLevel(enchantment: Enchantment): Int{
        return 0
    }
}