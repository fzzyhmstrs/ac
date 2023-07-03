package me.fzzyhmstrs.amethyst_core.augments.base

import me.fzzyhmstrs.amethyst_core.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import me.fzzyhmstrs.amethyst_core.entity.MissileEntity
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter.ScepterTier
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

/**
 * template for summoning a projectile entity. Used for basic "bolt"/"blast"/"missile" spells like Amethyst Imbuements base spell Magic Missile
 *
 * the only method you need to override to succesfully extend this class is [entityClass], providing the projectile entity you would like to spawn into the world.
 *
 * see [MissileEntity] for an open class you can use to develop your own projectiles.
 */
abstract class ProjectileAugment(
    tier: ScepterTier,
    augmentType: AugmentType = AugmentType.BOLT)
    :
    ScepterAugment(tier, augmentType)
{

    override fun applyTasks(world: World,user: LivingEntity,hand: Hand,level: Int,effects: AugmentEffect,spells: PairedAugments): TypedActionResult<List<Identifier>> {
        val list = spells.processOnCast(world,null,user, hand, level, effects)
        return spawnProjectileEntity(world, user, entityClass(world, user, level, effects, spells), list)
    }

    open fun entityClass(world: World, user: LivingEntity, level: Int = 1, effects: AugmentEffect, spells: PairedAugments): ProjectileEntity {
        val me = MissileEntity(world, user)
        me.setVelocity(user,user.pitch,user.yaw,0.0f,
            2.0f,
            0.1f)
        me.passEffects(spells,effects,level)
        return me
    }

    open fun spawnProjectileEntity(world: World, entity: LivingEntity, projectile: ProjectileEntity, list: MutableList<Identifier>): TypedActionResult<List<Identifier>>{
        val bl = world.spawnEntity(projectile)
        if(bl) {
            castSoundEvent(world, entity.blockPos)
            list.add(AugmentHelper.PROJECTILE_FIRED)
        }
        return if(list.isNotEmpty()) actionResult(ActionResult.SUCCESS,list) else FAIL
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
            val amount = spells.provideDealtDamage(effects.damage(level),this, entityHitResult, user, world, hand, level, effects)
            val damageSource = spells.provideDamageSource(damageSourceBuilder(source, user),this,entityHitResult, source, user, world, hand, level, effects)
            val bl  = entityHitResult.entity.damage(damageSource, amount)
            
            return if(bl) {
                val pos = source?.pos?:entityHitResult.entity.pos
                splashParticles(entityHitResult,world,pos.x,pos.y,pos.z,spells)
                user.applyDamageEffects(user,entityHitResult.entity)
                hitSoundEvent(world, entityHitResult.entity.blockPos)
                if (entityHitResult.entity.isAlive) {
                    actionResult(ActionResult.SUCCESS, AugmentHelper.DAMAGED_MOB, AugmentHelper.PROJECTILE_HIT)
                } else {
                    spells.processOnKill(entityHitResult, world, source, user, hand, level, effects)
                    actionResult(ActionResult.SUCCESS, AugmentHelper.DAMAGED_MOB, AugmentHelper.PROJECTILE_HIT, AugmentHelper.KILLED_MOB)
                }
            } else {
                FAIL
            }
        }
        return super.onEntityHit(entityHitResult, context, world, source, user, hand, level, effects, othersType, spells)
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
        val pos = source?.pos?: blockHitResult.pos
        splashParticles(blockHitResult,world,pos.x,pos.y,pos.z,spells)
        if (othersType == AugmentType.EMPTY){
            hitSoundEvent(world, blockHitResult.blockPos)
            return actionResult(ActionResult.SUCCESS, AugmentHelper.BLOCK_HIT)
        }
        return actionResult(ActionResult.PASS)
    }
}
