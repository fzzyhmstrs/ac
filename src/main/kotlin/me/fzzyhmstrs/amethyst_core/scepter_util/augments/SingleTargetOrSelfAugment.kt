package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterTier
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
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

abstract class SingleTargetOrSelfAugment(
    tier: ScepterTier,
    maxLvl: Int,
    augmentData: AugmentDatapoint,
    augmentType: AugmentType = AugmentType.SINGLE_TARGET_OR_SELF)
    :
    ScepterAugment(
        tier,
        maxLvl,
        augmentData,
        augmentType)
{

    override val baseEffect: AugmentEffect
        get() = super.baseEffect.withRange(6.0,0.0, 0.0)

    override fun applyTasks(world: World,user: LivingEntity,hand: Hand,level: Int,effects: AugmentEffect,spells: PairedAugments): TypedActionResult<List<Identifier>> {
        val target = RaycasterUtil.raycastHit(distance = effects.range(level),user)
        val hit = if (target is EntityHitResult) target else EntityHitResult(user)
        val list = spells.processSingleEntityHit(hit,world,null,user, hand, level, effects)
        return if (list.isEmpty()) FAIL else actionResult(ActionResult.SUCCESS,*list.toTypedArray())
    }

    override fun onEntityHit(
        entityHitResult: EntityHitResult,
        world: World,
        source: Entity?,
        user: LivingEntity,
        hand: Hand,
        level: Int,
        effects: AugmentEffect,
        othersType: AugmentType,
        spells: PairedAugments
    ): TypedActionResult<List<Identifier>> {
        val result = entityEffects(entityHitResult, world, source, user, hand, level, effects, othersType, spells)
        castSoundEvent(world,user.blockPos)
        return result
    }

    open fun entityEffects(
        entityHitResult: EntityHitResult,
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