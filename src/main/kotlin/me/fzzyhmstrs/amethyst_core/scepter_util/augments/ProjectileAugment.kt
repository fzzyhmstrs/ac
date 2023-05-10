package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.entity_util.MissileEntity
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterTier
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.ProjectileEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.ActionResult
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.World
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Vec3d

/**
 * template for summoning a projectile entity. Used for basic "bolt"/"blast"/"missile" spells like Amethyst Imbuements base spell Magic Missile
 *
 * the only method you need to override to succesfully extend this class is [entityClass], providing the projectile entity you would like to spawn into the world.
 *
 * see [MissileEntity] for an open class you can use to develop your own projectiles.
 */
abstract class ProjectileAugment(
    tier: ScepterTier,
    maxLvl: Int,
    augmentData: AugmentDatapoint,
    augmentType: AugmentType = AugmentType.BOLT)
    :
    ScepterAugment(
        tier,
        maxLvl,
        augmentData,
        augmentType
    )
{

    override fun applyTasks(world: World,user: LivingEntity,hand: Hand,level: Int,effects: AugmentEffect,spells: PairedAugments): TypedActionResult<List<Identifier>> {
        return spawnProjectileEntity(world, user, entityClass(world, user, level, effects, spells))
    }

    open fun entityClass(world: World, user: LivingEntity, level: Int = 1, effects: AugmentEffect, spells: PairedAugments): ProjectileEntity {
        return MissileEntity(world, user, spells)
    }

    open fun spawnProjectileEntity(world: World, entity: LivingEntity, projectile: ProjectileEntity): TypedActionResult<List<Identifier>>{
        val bl = world.spawnEntity(projectile)
        if(bl) {
            castSoundEvent(world, entity.blockPos)
        }
        return if(bl) TypedActionResult.success(listOf(AugmentHelper.PROJECTILE_FIRED)) else TypedActionResult.fail(listOf())
    }
    
    override fun onEntityHit(entityHitResult: EntityHitResult, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments): TypedActionResult<List<Identifier>>{
        if (othersType == AugmentType.EMPTY){
            val amount = spells.modifyDamage(effects.damage(level), entityHitResult, user, world, hand, level, effects)
            val damageSource = spells.provideDamageSource(entityHitResult, source, user, world, hand, level, effects)
            val bl  = entityHitResult.entity.damage(damageSource, amount)
            
            return if(bl) {
                val pos = source?.pos?:entityHitResult.entity.pos
                splashParticles(entityHitResult,world,pos.x,pos.y,pos.z,spells)
                user.applyDamageEffects(user,entityHitResult.entity)
                hitSoundEvent(world, entityHitResult.entity.blockPos)
                actionResult(ActionResult.SUCCESS, AugmentHelper.PROJECTILE_HIT)
            } else {
                FAIL
            }
        }
        return actionResult(ActionResult.PASS)
    }
    
    override fun onBlockHit(blockHitResult: BlockHitResult, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments): TypedActionResult<List<Identifier>>{
        val pos = source?.pos?: blockHitResult.pos
        blockHitResult.pos
        splashParticles(blockHitResult,world,pos.x,pos.y,pos.z,spells)
        if (othersType == AugmentType.EMPTY){
            hitSoundEvent(world, blockHitResult.blockPos)
            return actionResult(ActionResult.PASS,AugmentHelper.BLOCK_HIT)
        }
        return actionResult(ActionResult.PASS)
    }
}