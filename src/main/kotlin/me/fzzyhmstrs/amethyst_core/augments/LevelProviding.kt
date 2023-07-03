package me.fzzyhmstrs.amethyst_core.augments

import net.minecraft.enchantment.Enchantment

interface LevelProviding {
   fun provideLevel(enchantment: Enchantment): Int{
        return 0
    }
}