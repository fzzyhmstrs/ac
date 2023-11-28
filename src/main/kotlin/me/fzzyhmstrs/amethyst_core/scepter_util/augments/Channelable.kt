package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterTier
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World

/**
* interface for spells that are "channeled" in some way. Either with a charge and then a final effect onChannelEnd, or over time as the efect is channeled.
* 
* Channeling is applied every other tick. WIth a mana cost of 1, that is 10 mana per second
*/

interface Channelable {

    fun getMaxChannelTime(effects: AugmentEffect, level: Int): Int
    fun onChannelEnd(world: World, user: LivingEntity, hand: Hand, level: Int, modifiers: List<AugmentModifier> = listOf(), modifierData: AugmentModifier? = null): Boolean
}
