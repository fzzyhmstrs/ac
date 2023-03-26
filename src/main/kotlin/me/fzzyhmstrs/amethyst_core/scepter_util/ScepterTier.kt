package me.fzzyhmstrs.amethyst_core.scepter_util

import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierHelper
import net.minecraft.item.Item
import net.minecraft.registry.tag.TagKey

data class ScepterTier(val tag: TagKey<Item>, val tier: Int){

    companion object{
        val ONE = ScepterTier(ModifierHelper.TIER_1_SPELL_SCEPTERS,1)
        val TWO = ScepterTier(ModifierHelper.TIER_2_SPELL_SCEPTERS,2)
        val THREE = ScepterTier(ModifierHelper.TIER_3_SPELL_SCEPTERS,3)
    }
}