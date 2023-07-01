package me.fzzyhmstrs.amethyst_core.scepter.augments.base

import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter.ScepterTier
import me.fzzyhmstrs.amethyst_core.scepter.augments.AugmentDatapoint
import me.fzzyhmstrs.amethyst_core.scepter.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.scepter.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.scepter.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.scepter.augments.paired.ProcessContext
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

/**
 * Augment typically set up to provide an effect to the player or a single target.
 *
 * typical implementation will check if there is a target and apply the effect there, otherwise apply the effect to the user. Any number of alternative implementations are available of course.
 *
 * Implementation need not be limited to support effects. This template could also be used for an "instant damage" effect or a Life Drain spell, for a couple examples.
 */

abstract class EntityAoeAugment(
    tier: ScepterTier,
    maxLvl: Int,
    augmentData: AugmentDatapoint,
    augmentType: AugmentType = AugmentType.AOE_POSITIVE)
    :
    ScepterAugment(
        tier,
        maxLvl,
        augmentData,
        augmentType)
{

    constructor(tier: ScepterTier,
                maxLvl: Int,
                augmentData: AugmentDatapoint,
                positive: Boolean = true): this(tier, maxLvl, augmentData, if(positive) AugmentType.AOE_POSITIVE else AugmentType.AOE_NEGATIVE)

    override fun applyTasks(world: World,user: LivingEntity,hand: Hand,level: Int,effects: AugmentEffect,spells: PairedAugments): TypedActionResult<List<Identifier>> {
        val entityList = RaycasterUtil.raycastEntityArea(effects.range(level), user)
        if (entityList.isEmpty()) return FAIL
        val list = spells.processMultipleEntityHits(entityList.stream().map { EntityHitResult(it) }.toList(),world,null,user, hand, level, effects)
        list.addAll(spells.processOnCast(world,null,user, hand, level, effects))
        return if (list.isEmpty()) FAIL else actionResult(ActionResult.SUCCESS,list)
    }

    override fun onEntityHit(
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
    ): TypedActionResult<List<Identifier>> {
        val result = entityEffects(entityHitResult,context, world, source, user, hand, level, effects, othersType, spells)
        if (result.result.isAccepted)
            castSoundEvent(world,user.blockPos)
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
    ): TypedActionResult<List<Identifier>> {
        return actionResult(ActionResult.PASS)
    }
}