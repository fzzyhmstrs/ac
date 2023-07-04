package me.fzzyhmstrs.amethyst_core.scepter

import me.fzzyhmstrs.amethyst_core.AC.TIER_1_SPELL_SCEPTERS
import me.fzzyhmstrs.amethyst_core.AC.TIER_2_SPELL_SCEPTERS
import me.fzzyhmstrs.amethyst_core.AC.TIER_3_SPELL_SCEPTERS
import net.minecraft.item.Item
import net.minecraft.registry.tag.TagKey

data class ScepterTier(val tag: TagKey<Item>, val tier: Int){

    companion object{
        val ONE = ScepterTier(TIER_1_SPELL_SCEPTERS,1)
        val TWO = ScepterTier(TIER_2_SPELL_SCEPTERS,2)
        val THREE = ScepterTier(TIER_3_SPELL_SCEPTERS,3)
    }
}