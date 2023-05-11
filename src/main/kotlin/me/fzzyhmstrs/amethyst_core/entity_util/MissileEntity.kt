package me.fzzyhmstrs.amethyst_core.entity_util

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.registry.RegisterBaseEntity
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.PairedAugments
import net.minecraft.entity.Entity
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.projectile.ExplosiveProjectileEntity
import net.minecraft.entity.projectile.ProjectileUtil
import net.minecraft.network.packet.s2c.play.EntitySpawnS2CPacket
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

/**
 * basic missile projectile for use with spells or any other projectile-lobbing object.
 *
 * Extend and modify as you might any other projectile, like arrows, with the added functionality of the Modifiability.
 *
 * See [ModifiableEffectEntity] for more info. In general, modifiable entities can pass modifications from the object that spawned it to the projectile damage/effect itself. See below for a basic implementation of the modifiable effect.
 *
 * The [entityEffects] bucket holds attributes like damage and range that can be used in the [onEntityHit] methods and others. This allows for dynamic damage rather than defining one static value. As seen below, 3.0 damage is set as a default value, but if passEffects is called and it's parameter has a different damage, the entity will deal that modified amount on hit.
 *
 * Similarly, if the effect is provided with a consumer, on hit the missile will apply any of those consumers marked as harmful. An example consumer would be one that applies 10 seconds of blindness to any affected entity. Basically a bucket for applying secondary effects on hit. See [AugmentEffect] for more info.
 */

open class MissileEntity(entityType: EntityType<out MissileEntity?>, world: World): ExplosiveProjectileEntity(entityType,world), ModifiableEffectEntity {

    constructor(world: World,owner: LivingEntity,augments: PairedAugments) : this(RegisterBaseEntity.MISSILE_ENTITY,world){
        this.augment = augments
        this.owner = owner
        this.setPosition(
            owner.x,
            owner.eyeY - 0.4,
            owner.z
        )
        this.setRotation(owner.yaw, owner.pitch)
    }

    var augment: PairedAugments = PairedAugments()

    override var entityEffects: AugmentEffect = AugmentEffect()
    override var level: Int = 0
    open val maxAge = 200
    open val colorData = ColorData()
    private val particle by lazy{
        augment.getCastParticleType()
    }

    override fun initDataTracker() {}

    override fun tick() {
        super.tick()
        if (age > maxAge){
            discard()
        }
        val vec3d = velocity
        val hitResult = ProjectileUtil.getCollision(
            this
        ) { entity: Entity ->
            canHit(
                entity
            )
        }
        onCollision(hitResult)
        val x2 = vec3d.x
        val y2 = vec3d.y
        val z2 = vec3d.z
        val d = this.x + x2
        val e = this.y + y2
        val f = this.z + z2
        this.updateRotation()
        val g = drag.toDouble()
        if (age % 4 == 0)
            addParticles(x2, y2, z2)
        val gg: Double = if (this.isTouchingWater) {
            0.95
        } else {
            g
        }
        velocity = vec3d.multiply(gg)
        if (!hasNoGravity()) {
            velocity = velocity.add(0.0, -0.0, 0.0)
        }
        this.setPosition(d, e, f)
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        onMissileEntityHit(entityHitResult)
        discard()
    }

    open fun onMissileEntityHit(entityHitResult: EntityHitResult){
        val entity = owner
        if (entity is LivingEntity) {
            augment.processSingleEntityHit(entityHitResult,world,this,entity,Hand.MAIN_HAND,level,entityEffects)
            if (!entityHitResult.entity.isAlive){
                augment.processOnKill(entityHitResult,world,this,entity,Hand.MAIN_HAND,level,entityEffects)
            }
        }
    }

    override fun onBlockHit(blockHitResult: BlockHitResult) {
        super.onBlockHit(blockHitResult)
        onMissileBlockHit(blockHitResult)
        discard()
    }

    open fun onMissileBlockHit(blockHitResult: BlockHitResult){
        val entity = owner
        if (entity is LivingEntity) {
            augment.processSingleBlockHit(blockHitResult,world,this,entity,Hand.MAIN_HAND,level,entityEffects)
        }
    }

    override fun damage(source: DamageSource, amount: Float): Boolean {
        return false
    }

    override fun isBurning(): Boolean {
        return false
    }

    override fun getDrag(): Float {
        return 0.999999f
    }

    override fun getParticleType(): ParticleEffect? {
        return particle
    }

    override fun onSpawnPacket(packet: EntitySpawnS2CPacket) {
        super.onSpawnPacket(packet)
        val d = packet.velocityX
        val e = packet.velocityY
        val f = packet.velocityZ
        this.setVelocity(d, e, f)
    }

    open fun addParticles(x2: Double, y2: Double, z2: Double){
        val particleWorld = world
        if (particleWorld !is ServerWorld) return
        if (this.isTouchingWater) {
                particleWorld.spawnParticles(ParticleTypes.BUBBLE,this.x,this.y,this.z,3,1.0,1.0,1.0,0.0)
        } else {
            particleWorld.spawnParticles(particleType,this.x,this.y,this.z,3,1.0,1.0,1.0,0.0)
        }
    }

    class ColorData(val r: Float = 1.0f, val g: Float = 1.0f, val b: Float = 1.0f, val downScale: Float = 0.5f, val upScale: Float = 1.5f)

}
