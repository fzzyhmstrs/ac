package me.fzzyhmstrs.amethyst_core.scepter_util

import me.fzzyhmstrs.amethyst_core.AC
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

data class ScepterTier(val tag: TagKey<Item>, val tier: Int){

    companion object{
        val ONE = ScepterTier(TagKey.of(RegistryKeys.ITEM, Identifier(AC.MOD_ID,"tier_one_spell_scepters")),1)
        val TWO = ScepterTier(TagKey.of(RegistryKeys.ITEM, Identifier(AC.MOD_ID,"tier_two_spell_scepters")),2)
        val THREE = ScepterTier(TagKey.of(RegistryKeys.ITEM, Identifier(AC.MOD_ID,"tier_three_spell_scepters")),3)
    }
}