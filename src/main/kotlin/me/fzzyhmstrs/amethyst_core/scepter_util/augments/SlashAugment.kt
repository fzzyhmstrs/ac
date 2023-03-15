package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentConsumer
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterTier
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.mob.MobEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.DefaultParticleType
import net.minecraft.particle.ParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*

/**
 * Augment template for a spell that casts a blade of energy in front of the caster. See Amethyst Imbuements Spectral Slash for a basic implementation.
 *
 * For an implementation consistent with Spectral Slash, leave the effect and clientEffect methods alone, and add your flair to the [addStatusInstance], [secondaryEffect], [particleType], and [soundEvent] methods.
 */

@Suppress("SameParameterValue")
abstract class SlashAugment(tier: ScepterTier, maxLvl: Int): MiscAugment(tier, maxLvl){

    override val baseEffect: AugmentEffect
        get() = super.baseEffect.withRange(2.5,0.25,0.0)


    override fun effect(
        world: World,
        target: Entity?,
        user: LivingEntity,
        level: Int,
        hit: HitResult?,
        effect: AugmentEffect
    ): Boolean {
        if (world !is ServerWorld) return false
        if (user !is PlayerEntity) return false
        val rotation = user.getRotationVec(1.0F)
        val perpendicularVector = RaycasterUtil.perpendicularVector(rotation, RaycasterUtil.InPlane.XZ)
        val raycasterPos = user.pos.add(rotation.multiply(effect.range(level * 2)/2)).add(Vec3d(0.0,user.height/2.0,0.0))
        val entityList: MutableList<Entity> =
            RaycasterUtil.raycastEntityRotatedArea(
                world.iterateEntities(),
                user,
                raycasterPos,
                rotation,
                perpendicularVector,
                effect.range(level * 2),
                effect.range(level),
                1.2)
        val hostileEntityList = filter(entityList,user)
        if (!effect(world, user, hostileEntityList, level, effect)) return false
        world.playSound(null, user.blockPos, soundEvent(), SoundCategory.PLAYERS, 1.0F, 1.0F)
        return true
    }
    
    open fun filter(list: List<Entity>, user: LivingEntity): MutableList<Entity>{
        val hostileEntityList: MutableList<Entity> = mutableListOf()
        if (entityList.isNotEmpty()) {
            for (entity in entityList) {
                if (entity !== user) {
                    if (entity is SpellCastingEntity && !getPvpMode()) continue
                    if (entity is SpellCastingEntity && getPvpMode() && entity.isTeammate(user)) continue
                    hostileEntityList.add(entity)
                }
            }
        }
        return hostileEntityList
    }

    override fun effect(world: World, user: LivingEntity, entityList: MutableList<Entity>, level: Int, effect: AugmentEffect): Boolean {
        val entityDistance: SortedMap<Double, Entity> = mutableMapOf<Double, Entity>().toSortedMap()
        for (entity in entityList){
            if (entity is MobEntity){
                val dist = entity.squaredDistanceTo(user)
                entityDistance[dist] = entity
            }
        }
        var bl = false
        if (entityDistance.isNotEmpty()) {
            val baseDamage = effect.damage(level)
            val splashDamage = effect.damage(level - 1)
            var closestHit = false
            for (entry in entityDistance){
                val entity = entry.value
                if (!closestHit) {
                    bl = entity.damage(DamageSource.magic(entity, user), baseDamage)
                    closestHit = true
                } else {
                    bl = entity.damage(DamageSource.magic(entity, user), splashDamage)
                }
                secondaryEffect(world, user, entity, level, effect)
                val status = addStatusInstance(effect, level)
                if (status != null){
                    if (entity is LivingEntity){
                        entity.addStatusEffect(status)
                    }
                }
            }
            if (bl){
                effect.accept(user, AugmentConsumer.Type.BENEFICIAL)
            }
            effect.accept(toLivingEntityList(entityList), AugmentConsumer.Type.HARMFUL)
        }
        return true
    }

    override fun clientTask(world: World, user: LivingEntity,
                            hand: Hand, level: Int) {
        val rotation = user.getRotationVec(MinecraftClient.getInstance().tickDelta).normalize()
        val perpendicularToPosX = 1.0
        val perpendicularToPosZ = (rotation.x/rotation.z) * -1
        val perpendicularVector = Vec3d(perpendicularToPosX,0.0,perpendicularToPosZ).normalize()
        val userPos = user.eyePos.add(0.0,-0.3,0.0)
        val scale = baseEffect.range(level)/2
        for (p in particles){
            for (p2 in particleOffsets) {
                val particlePos =
                    userPos.add(perpendicularVector.multiply(p.first * scale)).add(rotation.multiply(p.second + p2))
                val particleVelocity = rotation.multiply(particleSpeed + level * 0.25).add(user.velocity)
                addParticles(world,particleType(),particlePos,particleVelocity)
                val particlePos2 =
                    userPos.add(perpendicularVector.multiply(p.first * -1 * scale)).add(rotation.multiply(p.second + p2))
                val particleVelocity2 = rotation.multiply(particleSpeed+ level * 0.25).add(user.velocity)
                addParticles(world,particleType(),particlePos2,particleVelocity2)
            }

        }
    }

    abstract fun particleType(): DefaultParticleType

    open fun addStatusInstance(effect: AugmentEffect, level: Int): StatusEffectInstance?{
        return null
    }

    open fun secondaryEffect(world: World, user: LivingEntity, target: Entity, level: Int, effect: AugmentEffect){
        return
    }

    override fun soundEvent(): SoundEvent {
        return SoundEvents.ENTITY_PLAYER_ATTACK_SWEEP
    }

    private fun addParticles(world: World,particleEffect: ParticleEffect,pos: Vec3d,velocity: Vec3d){
        world.addParticle(particleEffect,true,pos.x,pos.y,pos.z,velocity.x,velocity.y,velocity.z)
    }

    open fun particleSpeed(): Double{
        return 2.5
    }

    protected val particleSpeed by lazy { particleSpeed() }
    private val particles: Array<Pair<Double,Double>> = arrayOf(
        Pair(-1.0,0.0),
        Pair(-0.9,0.05),
        Pair(-0.8,0.1),
        Pair(-0.7,0.1),
        Pair(-0.6,0.15),
        Pair(-0.5,0.15),
        Pair(-0.4,0.15),
        Pair(-0.3,0.15),
        Pair(-0.2,0.2),
        Pair(-0.1,0.2),
        Pair(0.0,0.2))
    private val particleOffsets: Array<Double> = arrayOf(
        0.0,
        0.05,
        0.15,
        0.3)

}
