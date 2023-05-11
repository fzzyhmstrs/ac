package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import eu.pb4.common.protection.api.CommonProtection
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterTier
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.DamageSourceBuilder
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.PairedAugments
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.block.ShapeContext
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
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
    maxLvl: Int,
    augmentData: AugmentDatapoint,
    augmentType: AugmentType = AugmentType.DIRECTED_ENERGY)
    :
    ScepterAugment(
        tier,
        maxLvl,
        augmentData,
        augmentType)
{

    override val baseEffect: AugmentEffect
        get() = super.baseEffect.withRange(7.0,0.0, 0.0)

    override fun applyTasks(world: World,user: LivingEntity,hand: Hand,level: Int,effects: AugmentEffect,spells: PairedAugments): TypedActionResult<List<Identifier>> {
        if (world !is ServerWorld) return FAIL
        if (user !is PlayerEntity) return FAIL
        val rotation = user.getRotationVec(1.0F)
        val perpendicularVector = RaycasterUtil.perpendicularVector(rotation, RaycasterUtil.InPlane.XZ)
        val raycasterPos = user.pos.add(rotation.multiply(effects.range(level)/2)).add(beamOffset(user))
        val entityList: MutableList<Entity> =
            RaycasterUtil.raycastEntityRotatedArea(
                world.iterateEntities(),
                user,
                raycasterPos,
                rotation,
                perpendicularVector,
                effects.range(level),
                0.8,
                0.8)
        if (entityList.isEmpty()) return FAIL
        val list = spells.processMultipleEntityHits(entityList.stream().map { EntityHitResult(it) }.toList(),world,null,user, hand, level, effects).toMutableList()
        var range = effects.range(level)
        val blockList: MutableList<BlockHitResult> = mutableListOf()
        do {
            val pos = user.pos.add(beamOffset(user)).add(user.rotationVector.multiply(range))
            val blockPos = BlockPos(pos)
            blockList.add(BlockHitResult(pos, Direction.UP,blockPos,true))
            range -= 1.0
        }while (range > 0.0)
        val list2 = spells.processMultipleBlockHits(blockList,world, null, user, hand, level, effects)
        list.addAll(list2)
        return if (list.isEmpty()) FAIL else actionResult(ActionResult.SUCCESS,list)
    }

    open fun beamOffset(user: LivingEntity): Vec3d{
        return Vec3d(0.0,user.height/2.0,0.0)
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
        if (result.result.isAccepted)
            hitSoundEvent(world,user.blockPos)
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
        if (othersType.empty){
            val amount = spells.provideDamage(effects.damage(level),this, entityHitResult, user, world, hand, level, effects)
            val damageSource = spells.provideDamageSource(DamageSourceBuilder(user,source),this,entityHitResult, source, user, world, hand, level, effects)
            val bl  = entityHitResult.entity.damage(damageSource, amount)

            return if(bl) {
                val pos = source?.pos?:entityHitResult.entity.pos
                splashParticles(entityHitResult,world,pos.x,pos.y,pos.z,spells)
                user.applyDamageEffects(user,entityHitResult.entity)
                hitSoundEvent(world, entityHitResult.entity.blockPos)
                actionResult(ActionResult.SUCCESS, AugmentHelper.DAMAGED_MOB)
            } else {
                FAIL
            }
        }
        return actionResult(ActionResult.PASS)
    }

    override fun onBlockHit(
        blockHitResult: BlockHitResult,
        world: World,
        source: Entity?,
        user: LivingEntity,
        hand: Hand,
        level: Int,
        effects: AugmentEffect,
        othersType: AugmentType,
        spells: PairedAugments
    ): TypedActionResult<List<Identifier>> {
        val result = blockEffects(blockHitResult, world, source, user, hand, level, effects, othersType, spells)
        if (result.result.isAccepted)
            hitSoundEvent(world,user.blockPos)
        return result
    }

    open fun blockEffects(
        blockHitResult: BlockHitResult,
        world: World,
        source: Entity?,
        user: LivingEntity,
        hand: Hand,
        level: Int,
        effects: AugmentEffect,
        othersType: AugmentType,
        spells: PairedAugments
    ): TypedActionResult<List<Identifier>> {
        val pos = blockHitResult.pos
        if(!world.getBlockState(blockHitResult.blockPos).isSolidBlock(world,blockHitResult.blockPos))
            splashParticles(blockHitResult,world,pos.x,pos.y,pos.z,spells)
        return actionResult(ActionResult.PASS)
    }
}