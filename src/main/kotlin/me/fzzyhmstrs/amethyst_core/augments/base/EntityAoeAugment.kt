package me.fzzyhmstrs.amethyst_core.augments.base

import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.augments.SpellActionResult
import me.fzzyhmstrs.amethyst_core.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
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

abstract class EntityAoeAugment(
    tier: ScepterTier,
    augmentType: AugmentType = AugmentType.AOE_POSITIVE)
    :
    ScepterAugment(tier, augmentType)
{

    constructor(tier: ScepterTier,
                positive: Boolean = true): this(tier, if(positive) AugmentType.AOE_POSITIVE else AugmentType.AOE_NEGATIVE)

    override fun applyTasks(world: World,user: LivingEntity,hand: Hand,level: Int,effects: AugmentEffect,spells: PairedAugments): SpellActionResult {
        val entityList = RaycasterUtil.raycastEntityArea(effects.range(level), user)
        if (entityList.isEmpty()) return FAIL
        val list = spells.processMultipleEntityHits(entityList.stream().map { EntityHitResult(it) }.toList(),world,null,user, hand, level, effects)
        list.addAll(spells.processOnCast(world,null,user, hand, level, effects))
        return if (list.isEmpty()) FAIL else SpellActionResult.success(list)
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
    ): SpellActionResult {
        val result = entityEffects(entityHitResult,context, world, source, user, hand, level, effects, othersType, spells)
        if (result.success())
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
    ): SpellActionResult {
        return SUCCESSFUL_PASS
    }
}