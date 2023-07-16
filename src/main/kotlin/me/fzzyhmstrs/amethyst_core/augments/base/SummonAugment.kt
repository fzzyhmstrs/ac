package me.fzzyhmstrs.amethyst_core.augments.base

import me.fzzyhmstrs.amethyst_core.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.augments.SpellActionResult
import me.fzzyhmstrs.amethyst_core.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import me.fzzyhmstrs.amethyst_core.entity.MissileEntity
import me.fzzyhmstrs.amethyst_core.entity.ModifiableEffectEntity
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter.ScepterTier
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Direction
import net.minecraft.world.World
import org.apache.logging.log4j.core.appender.rolling.action.IfAccumulatedFileCount

abstract class SummonAugment<E>(
    tier: ScepterTier,
    augmentType: AugmentType = AugmentType.SUMMON)
    :
    ScepterAugment(tier, augmentType)
    where
    E: Entity,
    E: ModifiableEffectEntity
{

    override val baseEffect: AugmentEffect
        get() = AugmentEffect().withRange(3.0)

    override fun <T> applyTasks(world: World, context: ProcessContext, user: T, hand: Hand, level: Int, effects: AugmentEffect, spells: PairedAugments)
    :
    SpellActionResult
    where
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val onCastResults = spells.processOnCast(context,world,null,user, hand, level, effects)
        if (!onCastResults.success()) return  FAIL
        if (onCastResults.overwrite()) return onCastResults
        val hit = RaycasterUtil.raycastHit(
            distance = effects.range(level),
            user,
            includeFluids = true
        ) ?: BlockHitResult(user.pos,Direction.UP,user.blockPos,false)
        val list = if (hit is BlockHitResult) {
            spells.processSingleBlockHit(hit, context, world, null, user, hand, level, effects)
        } else {
            val entityHitResult = EntityHitResult(user)
            spells.processSingleEntityHit(entityHitResult,context,world,null,user, hand, level, effects)
        }
        return if (list.isEmpty()) {
            FAIL
        } else {
            list.addAll(onCastResults.results())
            SpellActionResult.success(list)
        }
    }

    override fun <T> onBlockHit(
        blockHitResult: BlockHitResult,
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
        val result = if (context.get(SUMMONED_MOB)){
            afterSummonBlockEffects(blockHitResult, context, world, source, user, hand, level, effects, othersType, spells)
        } else {
            val temp = spawnEntities<E, T>(blockHitResult, context, world, source, user, hand, level, effects, othersType, spells)
            if (temp.success()){
                spells.castSoundEvents(world,user.blockPos,context)
            }
            temp
        }
        return result
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
        val result = if (context.get(SUMMONED_MOB)){
            afterSummonEntityEffects(entityHitResult, context, world, source, user, hand, level, effects, othersType, spells)
        } else {
            val temp = spawnEntities<E, T>(entityHitResult, context, world, source, user, hand, level, effects, othersType, spells)
            if (temp.success()){
                spells.castSoundEvents(world, user.blockPos, context)
            }
            temp
        }

        return result
    }

    open fun <T,U> spawnEntities(
        hit: HitResult,
        context: ProcessContext,
        world: World,
        source: Entity?,
        user: U,
        hand: Hand,
        level: Int,
        effects: AugmentEffect,
        othersType: AugmentType,
        spells: PairedAugments)
    :
    SpellActionResult
    where
    T: Entity,
    T: ModifiableEffectEntity,
    U: LivingEntity,
    U: SpellCastingEntity
    {
        if (othersType.empty){
            summonContext(context)
            val startCount = spawnCount(user,effects, othersType, spells)
            val count = spells.provideCount(startCount, context, user, world, hand, level, effects, othersType, spells)
            val startList: List<T> = entitiesToSpawn(world,user,hit,level,effects, count)
            val list = spells.provideSummons(startList,hit, context, user, world, hand, level, effects)
            var successes = 0
            for (entity in list){
                if (entity is ModifiableEffectEntity) {
                    entity.passContext(context)
                }
                if (world.spawnEntity(entity)) successes++
            }
            return if (successes > 0) {
                SpellActionResult.success(AugmentHelper.SUMMONED_MOB)
            } else {
                FAIL
            }
        }
        return SUCCESSFUL_PASS
    }

    open fun <T> entitiesToSpawn(world: World, user: LivingEntity, hit: HitResult, level: Int, effects: AugmentEffect, count: Int)
    :
    List<T>
    where
    T: Entity,
    T: ModifiableEffectEntity
    {
        return listOf()
    }

    open fun <T> spawnCount(user: T,effects: AugmentEffect,othersType: AugmentType, spells: PairedAugments)
    :
    Int
    where
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return 1
    }

    open fun <T> afterSummonBlockEffects(
        blockHitResult: BlockHitResult,
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
        return SUCCESSFUL_PASS
    }

    open fun <T> afterSummonEntityEffects(
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
        return SUCCESSFUL_PASS
    }

    fun summonContext(context: ProcessContext): ProcessContext{
        return context.set(SUMMONED_MOB, true).set(ProcessContext.FROM_ENTITY,true)
    }

    companion object{
        val SUMMONED_MOB = object : ProcessContext.Data<Boolean>("mob_is_summoned",ProcessContext.BooleanDataType){}
    }
}
