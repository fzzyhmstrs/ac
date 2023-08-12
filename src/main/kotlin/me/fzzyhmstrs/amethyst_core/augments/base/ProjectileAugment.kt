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
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ExplosiveProjectileEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import kotlin.math.max
import kotlin.streams.toList

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

    override fun <T> applyTasks(world: World,context: ProcessContext, user: T, hand: Hand, level: Int, effects: AugmentEffect, spells: PairedAugments)
    : 
    SpellActionResult 
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val onCastResults = spells.processOnCast(context,world,null,user, hand, level, effects)
        if (!onCastResults.success()) return  FAIL
        if (onCastResults.overwrite()) return onCastResults
        val type = AugmentType.EMPTY
        val startCount = startCount(user,effects,level,type,spells)
        val count = max(1, spells.provideCount(startCount,context, user, world, hand, level, effects, type, spells))
        val projectiles = createProjectileEntities(world, context, user, level, effects, spells, count)
        val projectiles2 = projectiles.stream().map { if (it is ModifiableEffectEntity) spells.provideProjectile(it,context,user,world, hand, level, effects) else it }.toList()
        val result = spawnProjectileEntities(world,context, user, projectiles2, mutableListOf(), spells)
        return if (result.success()) {
            result.withResults(onCastResults.results())
        } else {
            FAIL
        }

    }

    open fun <T> createProjectileEntities(world: World, context: ProcessContext, user: T, level: Int = 1, effects: AugmentEffect, spells: PairedAugments, count: Int)
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

    open fun <T> spawnProjectileEntities(world: World, context: ProcessContext, user: T, projectiles: List<ProjectileEntity>, list: MutableList<Identifier>, spells: PairedAugments)
    : 
    SpellActionResult
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        var success = 0
        var angle = (15f * (projectiles.size - 1)) / 2f


        for (projectile in projectiles){
            if (projectile is ModifiableEffectEntity){
                if (projectile.processContext.get(ProcessContext.SPELL) != Identifier("")){
                    projectileContext(projectile.processContext)
                } else {
                    val context1 = context.copy()
                    projectileContext(context1)
                    projectile.passContext(context1)
                }
            }
            if (projectile is ExplosiveProjectileEntity){
                val powVec = Vec3d(projectile.powerX, projectile.powerY,projectile.powerZ)
                val newVel = powVec.rotateY(angle)
                projectile.velocity = newVel
            } else {
                val newVel = projectile.velocity.rotateY(angle)
                projectile.velocity = newVel
            }
            if(world.spawnEntity(projectile)) success++
            angle -= 15f
        }
        if(success > 0) {
            spells.castSoundEvents(world, user.blockPos, context)
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
            if (!canTarget(entityHitResult, context, world, user, hand, spells)) return FAIL
            val amount = spells.provideDealtDamage(effects.damage(level), context, entityHitResult, user, world, hand, level, effects)
            val damageSource = spells.provideDamageSource(context,entityHitResult, source, user, world, hand, level, effects)
            val bl  = entityHitResult.entity.damage(damageSource, amount)
            
            return if(bl) {
                val pos = source?.pos?:entityHitResult.entity.pos
                splashParticles(entityHitResult,world,pos.x,pos.y,pos.z,spells)
                user.applyDamageEffects(user,entityHitResult.entity)
                spells.hitSoundEvents(world, entityHitResult.entity.blockPos,context)
                if (entityHitResult.entity.isAlive) {
                    SpellActionResult.success(AugmentHelper.DAMAGED_MOB, AugmentHelper.PROJECTILE_HIT)
                } else {
                    spells.processOnKill(entityHitResult, context, world, source, user, hand, level, effects)
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
            spells.hitSoundEvents(world, blockHitResult.blockPos,context)
            return SpellActionResult.success(AugmentHelper.BLOCK_HIT)
        }
        return SUCCESSFUL_PASS
    }

    companion object{
        fun projectileContext(context: ProcessContext): ProcessContext{
            return context.set(ProcessContext.FROM_ENTITY,true)
        }
    }
}
