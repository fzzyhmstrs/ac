package me.fzzyhmstrs.amethyst_core.augments.base

import me.fzzyhmstrs.amethyst_core.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.augments.SpellActionResult
import me.fzzyhmstrs.amethyst_core.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import me.fzzyhmstrs.amethyst_core.entity.MissileEntity
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter.ScepterTier
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
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

    override fun <T> applyTasks(world: World, user: T, hand: Hand, level: Int, effects: AugmentEffect, spells: PairedAugments)
    : 
    SpellActionResult 
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val list = spells.processOnCast(world,null,user, hand, level, effects)
        val projectiles = createProjectileEntities(world, user, level, effects, spells)
        return spawnProjectileEntities(world, user, projectiles, list)
    }

    open fun <T> createProjectileEntities(world: World, user: T, level: Int = 1, effects: AugmentEffect, spells: PairedAugments)
    : 
    List<ProjectileEntity>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        //val me = MissileEntity(world, user)
        //val direction = user.rotationVec3d
        //me.setVelocity(direction.x,direction.y,direction.z, 2.0f, 0.1f)
        //me.passEffects(spells,effects,level)
        //val finalMe = spells.provideProjectile(me, user, world, Hand.MAIN_HAND, level, effects)
        return listOf()
    }

    open fun <T> spawnProjectileEntities(world: World, user: T, projectiles: List<ProjectileEntity>, list: MutableList<Identifier>)
    : 
    SpellActionResult
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        var success = 0
        for (projectile in projectiles){
            if(world.spawnEntity(projectile)) success++
        }
        if(success > 0) {
            castSoundEvent(world, user.blockPos)
            list.add(AugmentHelper.PROJECTILE_FIRED)
        }
        return if(list.isNotEmpty()) SpellActionResult.success(list) else FAIL
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
    ): 
    SpellActionResult
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        if (othersType.empty){
            val amount = spells.provideDealtDamage(effects.damage(level), spellContext(), entityHitResult, user, world, hand, level, effects)
            val damageSource = spells.provideDamageSource(damageSourceBuilder(world, source, user), spellContext(),entityHitResult, source, user, world, hand, level, effects)
            val bl  = entityHitResult.entity.damage(damageSource, amount)
            
            return if(bl) {
                val pos = source?.pos?:entityHitResult.entity.pos
                splashParticles(entityHitResult,world,pos.x,pos.y,pos.z,spells)
                user.applyDamageEffects(user,entityHitResult.entity)
                hitSoundEvent(world, entityHitResult.entity.blockPos)
                if (entityHitResult.entity.isAlive) {
                    SpellActionResult.success(AugmentHelper.DAMAGED_MOB, AugmentHelper.PROJECTILE_HIT)
                } else {
                    spells.processOnKill(entityHitResult, world, source, user, hand, level, effects)
                    SpellActionResult.success(AugmentHelper.DAMAGED_MOB, AugmentHelper.PROJECTILE_HIT, AugmentHelper.KILLED_MOB)
                }
            } else {
                FAIL
            }
        }
        return super.onEntityHit(entityHitResult, context, world, source, user, hand, level, effects, othersType, spells)
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
        val pos = source?.pos?: blockHitResult.pos
        splashParticles(blockHitResult,world,pos.x,pos.y,pos.z,spells)
        if (othersType == AugmentType.EMPTY){
            hitSoundEvent(world, blockHitResult.blockPos)
            return SpellActionResult.success(AugmentHelper.BLOCK_HIT)
        }
        return SUCCESSFUL_PASS
    }
}
