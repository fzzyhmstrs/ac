package me.fzzyhmstrs.amethyst_core.scepter.augments

import net.minecraft.enchantment.Enchantment

interface LevelProviding {
   fun provideLevel(enchantment: Enchantment): Int{
        return 0
    }
}