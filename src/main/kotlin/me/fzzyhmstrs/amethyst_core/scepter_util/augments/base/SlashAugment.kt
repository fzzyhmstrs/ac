package me.fzzyhmstrs.amethyst_core.scepter_util.augments.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterTier
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.AugmentDatapoint
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.ProcessContext
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.particle.ParticleEffect
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.math.Vec3d
import net.minecraft.world.World
import java.util.*

/**
 * Augment template for a spell that casts a blade of energy in front of the caster. See Amethyst Imbuements Spectral Slash for a basic implementation.
 */

@Suppress("SameParameterValue")
abstract class SlashAugment(
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


    override val baseEffect: AugmentEffect
        get() = super.baseEffect.withRange(2.5,0.25,0.0)

    override fun applyTasks(
        world: World,
        user: LivingEntity,
        hand: Hand,
        level: Int,
        effects: AugmentEffect,
        spells: PairedAugments
    ): TypedActionResult<List<Identifier>> {
        if (world !is ServerWorld) return FAIL
        if (user !is PlayerEntity) return FAIL
        val rotation = user.getRotationVec(1.0F)
        val perpendicularVector = RaycasterUtil.perpendicularVector(rotation, RaycasterUtil.InPlane.XZ)
        val raycasterPos = user.pos.add(rotation.multiply(effects.range(level * 2)/2)).add(Vec3d(0.0,user.height/2.0,0.0))
        val entityList: MutableList<Entity> =
            RaycasterUtil.raycastEntityRotatedArea(
                world.iterateEntities(),
                user,
                raycasterPos,
                rotation,
                perpendicularVector,
                effects.range(level * 2),
                effects.range(level),
                1.2)

        val hostileEntityList = filter(entityList,user)
        val entityDistance: SortedMap<Double, EntityHitResult> = mutableMapOf<Double, EntityHitResult>().toSortedMap()
        for (entity in hostileEntityList){
            val dist = entity.squaredDistanceTo(user)
            entityDistance[dist] = entity
        }
        val closest = entityDistance[entityDistance.firstKey()] ?:return FAIL
        val context = SlashContext(closest.entity)
        val list = if (hostileEntityList.isNotEmpty()) {
            spells.processMultipleEntityHits(hostileEntityList,context, world, null, user, hand, level, effects)
        } else {
            listOf(AugmentHelper.DRY_FIRED)
        }
        val buf = ScepterHelper.prepareParticlePacket(adderId)
        buf.writeInt(level)
        ScepterHelper.sendSpellParticlesFromServer(world,user.pos,buf)
        return if (list.isEmpty()) FAIL else actionResult(ActionResult.SUCCESS,list)
    }
    
    open fun filter(list: List<Entity>, user: LivingEntity): MutableList<EntityHitResult>{
        val hostileEntityList: MutableList<EntityHitResult> = mutableListOf()
        if (list.isNotEmpty()) {
            for (entity in list) {
                if (entity !== user) {
                    if (entity is SpellCastingEntity && !getPvpMode()) continue
                    if (entity is SpellCastingEntity && getPvpMode() && entity.isTeammate(user)) continue
                    hostileEntityList.add(EntityHitResult(entity))
                }
            }
        }
        return hostileEntityList
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
            val closestEntity = if (context is SlashContext) context.closestEntity else null
            val baseDamage = effects.damage(level)
            val splashDamage = effects.damage(level - 2)
            val inputDamage = if(closestEntity == entityHitResult.entity) baseDamage else splashDamage
            val damage = spells.provideDealtDamage(inputDamage,this,entityHitResult, user, world, hand, level, effects)
            val damageSource = spells.provideDamageSource(damageSourceBuilder(source, user),this,entityHitResult, source, user, world, hand, level, effects)
            val bl  = entityHitResult.entity.damage(damageSource, damage)

            return if(bl) {
                user.applyDamageEffects(user,entityHitResult.entity)
                hitSoundEvent(world, entityHitResult.entity.blockPos)
                if (entityHitResult.entity.isAlive) {
                    actionResult(ActionResult.SUCCESS, AugmentHelper.DAMAGED_MOB, AugmentHelper.SLASHED)
                } else {
                    spells.processOnKill(entityHitResult, world, source, user, hand, level, effects)
                    actionResult(ActionResult.SUCCESS, AugmentHelper.DAMAGED_MOB, AugmentHelper.SLASHED, AugmentHelper.KILLED_MOB)
                }
            } else {
                FAIL
            }
        }

        return super.onEntityHit(entityHitResult,context, world, source, user, hand, level, effects, othersType, spells)
    }

    private fun addParticles(world: World, user: LivingEntity, level: Int) {
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
                addParticles(world,castParticleType(),particlePos,particleVelocity)
                val particlePos2 =
                    userPos.add(perpendicularVector.multiply(p.first * -1 * scale)).add(rotation.multiply(p.second + p2))
                val particleVelocity2 = rotation.multiply(particleSpeed+ level * 0.25).add(user.velocity)
                addParticles(world,castParticleType(),particlePos2,particleVelocity2)
            }

        }
    }
    private fun addParticles(world: World,particleEffect: ParticleEffect,pos: Vec3d,velocity: Vec3d){
        world.addParticle(particleEffect,true,pos.x,pos.y,pos.z,velocity.x,velocity.y,velocity.z)
    }

    open fun particleSpeed(): Double{
        return 2.5
    }

    companion object{
        protected val contextId = Identifier(AC.MOD_ID,"slash_context")
    }
    protected val particleSpeed by lazy { particleSpeed() }
    protected val particles: Array<Pair<Double,Double>> = arrayOf(
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
    protected val particleOffsets: Array<Double> = arrayOf(
        0.0,
        0.05,
        0.15,
        0.3)
    private val adderId = Identifier(AC.MOD_ID,"slash_adder")

    init{
        ScepterHelper.registerParticleAdder(adderId){ client, buf ->
            val user = client.player ?: return@registerParticleAdder
            val world = client.world ?: return@registerParticleAdder
            val level = buf.readInt()
            client.execute {
                addParticles(world, user, level)
            }
        }
    }

    protected class SlashContext(val closestEntity: Entity): ProcessContext{
        override fun getType(): Identifier {
            return contextId
        }
    }

}
