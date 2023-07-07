package me.fzzyhmstrs.amethyst_core.augments.base

import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.augments.SpellActionResult
import me.fzzyhmstrs.amethyst_core.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter.ScepterTier
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

/**
 * Augment typically set up to provide an effect to the player or a single target.
 *
 * typical implementation will check if there is a target and apply the effect there, otherwise apply the effect to the user. Any number of alternative implementations are available of course.
 *
 * Implementation need not be limited to support effects. This template could also be used for an "instant damage" effect or a Life Drain spell, for a couple examples.
 */

abstract class SingleTargetOrSelfAugment(
    tier: ScepterTier,
    augmentType: AugmentType = AugmentType.SINGLE_TARGET_OR_SELF)
    :
    ScepterAugment(tier, augmentType)
{

    override val baseEffect: AugmentEffect
        get() = super.baseEffect.withRange(6.0,0.0, 0.0)

    override fun <T> applyTasks(world: World,user: T,hand: Hand,level: Int,effects: AugmentEffect, spells: PairedAugments)
    :
    SpellActionResult
    where
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val target = RaycasterUtil.raycastHit(distance = effects.range(level),user)
        val hit = if (target is EntityHitResult) target else EntityHitResult(user)
        val list = spells.processSingleEntityHit(hit,world,null,user, hand, level, effects)
        list.addAll(spells.processOnCast(world,null,user, hand, level, effects))
        return if (list.isEmpty()) FAIL else SpellActionResult.success(list)
    }

    override fun <T> onEntityHit(
        entityHitResult: EntityHitResult,
        context: ProcessContext,
        world: World,
        source: Entity?,
        user: T,
        hand: Hand,
        level: Int,
        effects: AugmentEffect,
        othersType: AugmentType,
        spells: PairedAugments
    )
    :
    SpellActionResult
    where
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val result = entityEffects(entityHitResult,context, world, source, user, hand, level, effects, othersType, spells)
        if (result.success()) {
            castSoundEvent(world, user.blockPos)
        }
        return result
    }

    open fun entityEffects(
        entityHitResult: EntityHitResult,
        context: ProcessContext,
        world: World,
        source: Entity?,
        user: LivingEntity,
        hand: Hand,
        level: Int,
        effects: AugmentEffect,
        othersType: AugmentType,
        spells: PairedAugments
    ): SpellActionResult {
        return SUCCESSFUL_PASS
    }


}