package me.fzzyhmstrs.amethyst_core.augments.base

import me.fzzyhmstrs.amethyst_core.augments.AugmentHelper
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
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Direction
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World

/**
 * Augment typically set up to provide an effect to the player or a single target.
 *
 * typical implementation will check if there is a target and apply the effect there, otherwise apply the effect to the user. Any number of alternative implementations are available of course.
 *
 * Implementation need not be limited to support effects. This template could also be used for an "instant damage" effect or a Life Drain spell, for a couple examples.
 */

abstract class BeamAugment(
    tier: ScepterTier,
    augmentType: AugmentType = AugmentType.DIRECTED_ENERGY)
    :
    ScepterAugment(tier, augmentType)
{

    override val baseEffect: AugmentEffect
        get() = super.baseEffect.withRange(7.0,0.0, 0.0)

    override fun <T> applyTasks(world: World,context: ProcessContext,user: T, hand: Hand,level: Int,effects: AugmentEffect,spells: PairedAugments)
    : 
    SpellActionResult
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val onCastResults = spells.processOnCast(context,world,null,user, hand, level, effects)
        if (!onCastResults.success()) return  FAIL
        if (onCastResults.overwrite()) return onCastResults
        if (world !is ServerWorld) return FAIL
        val rotation = user.getRotationVec(1.0F)
        val perpendicularVector = RaycasterUtil.perpendicularVector(rotation, RaycasterUtil.InPlane.XZ)
        val raycastPos = user.pos.add(rotation.multiply(effects.range(level)/2)).add(beamOffset(user))
        val entityList: MutableList<Entity> =
            RaycasterUtil.raycastEntityRotatedArea(
                world.iterateEntities(),
                user,
                raycastPos,
                rotation,
                perpendicularVector,
                effects.range(level),
                0.8,
                0.8)
        val filteredList = filter(entityList,user)
        if (filteredList.isEmpty()) return FAIL
        val list = spells.processMultipleEntityHits(entityList.stream().map { EntityHitResult(it) }.toList(),context,world,null,user, hand, level, effects)
        list.addAll(onCastResults.results())
        var range = effects.range(level)
        val blockList: MutableList<BlockHitResult> = mutableListOf()
        do {
            val pos = user.pos.add(beamOffset(user)).add(user.rotationVector.multiply(range))
            val blockPos = BlockPos.ofFloored(pos)
            blockList.add(BlockHitResult(pos, Direction.UP,blockPos,true))
            range -= 1.0
        }while (range > 0.0)
        val list2 = spells.processMultipleBlockHits(blockList, context, world, null, user, hand, level, effects)
        list.addAll(list2)
        spells.castSoundEvents(world,user.blockPos,context)
        return if (list.isEmpty()) FAIL else SpellActionResult.success(list)
    }

    open fun filter(list: List<Entity>, user: LivingEntity): MutableList<EntityHitResult>{
        val hostileEntityList: MutableList<EntityHitResult> = mutableListOf()
        if (list.isNotEmpty()) {
            for (entity in list) {
                if (entity !== user) {
                    if (entity is PlayerEntity && !getPvpMode()) continue
                    if (entity is SpellCastingEntity && getPvpMode() && entity.isTeammate(user)) continue
                    hostileEntityList.add(EntityHitResult(entity))
                }
            }
        }
        return hostileEntityList
    }

    open fun beamOffset(user: LivingEntity): Vec3d{
        return Vec3d(0.0,user.height/2.0,0.0)
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
        return entityEffects(entityHitResult,context, world, source, user, hand, level, effects, othersType, spells)
    }

    open fun <T> entityEffects(
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
        if (othersType.empty){
            val amount = spells.provideDealtDamage(effects.damage(level), context, entityHitResult, user, world, hand, level, effects)
            val damageSource = spells.provideDamageSource(context, entityHitResult, source, user, world, hand, level, effects)
            val bl  = entityHitResult.entity.damage(damageSource, amount)

            return if(bl) {
                val pos = source?.pos?:entityHitResult.entity.pos
                splashParticles(entityHitResult,world,pos.x,pos.y,pos.z,spells)
                user.applyDamageEffects(user,entityHitResult.entity)
                spells.hitSoundEvents(world, entityHitResult.entity.blockPos, context)
                val list: MutableList<Identifier> = mutableListOf()
                if (entityHitResult.entity.isAlive) {
                    list.add(AugmentHelper.DAMAGED_MOB)
                    SpellActionResult.success(list)
                } else {
                    list.add(AugmentHelper.DAMAGED_MOB)
                    list.add(AugmentHelper.KILLED_MOB)
                    spells.processOnKill(entityHitResult,context, world, source, user, hand, level, effects)
                    SpellActionResult.success(list)
                }
            } else {
                FAIL
            }
        }
        return SUCCESSFUL_PASS
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
    T : LivingEntity,
    T : SpellCastingEntity
    {
        return blockEffects(blockHitResult, context, world, source, user, hand, level, effects, othersType, spells)
    }

    open fun <T> blockEffects(
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
        val pos = blockHitResult.pos
        if(!world.getBlockState(blockHitResult.blockPos).isSolidBlock(world,blockHitResult.blockPos))
            splashParticles(blockHitResult,world,pos.x,pos.y,pos.z,spells)
        return SUCCESSFUL_PASS
    }
}
