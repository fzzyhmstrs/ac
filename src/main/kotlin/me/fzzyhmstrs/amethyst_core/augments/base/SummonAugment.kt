package me.fzzyhmstrs.amethyst_core.augments.base

import me.fzzyhmstrs.amethyst_core.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter.ScepterTier
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class SummonAugment(
    tier: ScepterTier,
    augmentType: AugmentType = AugmentType.SUMMON)
    :
    ScepterAugment(tier, augmentType)
{

    override val baseEffect: AugmentEffect
        get() = AugmentEffect().withRange(3.0)

    override fun applyTasks(world: World,user: LivingEntity,hand: Hand,level: Int,effects: AugmentEffect,spells: PairedAugments): TypedActionResult<List<Identifier>> {
        val hit = RaycasterUtil.raycastHit(
            distance = effects.range(level),
            user,
            includeFluids = true
        ) ?: BlockHitResult(user.pos,Direction.UP,user.blockPos,false)
        val list = if (hit is BlockHitResult) {
            spells.processSingleBlockHit(hit, world, null, user, hand, level, effects)
        } else {
            val entityHitResult = EntityHitResult(user)
            spells.processSingleEntityHit(entityHitResult,world,null,user, hand, level, effects)
        }
        return if (list.isEmpty()) FAIL else actionResult(ActionResult.SUCCESS,*list.toTypedArray())
    }

    override fun onBlockHit(
        blockHitResult: BlockHitResult,
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
        if (othersType.empty){
            val list = spells.provideSummons(entitiesToSpawn(world,user,blockHitResult,level,effects),this,user, world, hand, level, effects)
            var successes = 0
            for (entity in list){
                if (world.spawnEntity(entity)) successes++
            }
            return if (successes > 0) {
                castSoundEvent(world,user.blockPos)
                actionResult(ActionResult.SUCCESS, AugmentHelper.SUMMONED_MOB)
            } else {
                FAIL
            }
        }
        return actionResult(ActionResult.PASS)
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
        if (othersType.empty){
            val list = spells.provideSummons(entitiesToSpawn(world,user,entityHitResult,level,effects),this,user, world, hand, level, effects)
            var successes = 0
            for (entity in list){
                if (world.spawnEntity(entity)) successes++
            }
            return if (successes > 0) {
                castSoundEvent(world,user.blockPos)
                actionResult(ActionResult.SUCCESS, AugmentHelper.SUMMONED_MOB)
            } else {
                FAIL
            }
        }
        return actionResult(ActionResult.PASS)
    }

    open fun <T> entitiesToSpawn(world: World, user: LivingEntity, hit: HitResult, level: Int, effects: AugmentEffect): List<T> {
        return listOf()
    }
}
