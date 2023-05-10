package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.event.BlockHitActionEvent
import me.fzzyhmstrs.amethyst_core.event.EntityHitActionEvent
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentConsumer
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlD
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlF
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World

class PairedAugments private constructor (internal val augments: Array<ScepterAugment>){

    constructor(): this(arrayOf())

    constructor(main: ScepterAugment): this(arrayOf(main))

    constructor(main: ScepterAugment, pairedSpell: ScepterAugment?): this(if(pairedSpell != null) arrayOf(main, pairedSpell) else arrayOf(main))

    private val type: Type

    init{
        type = if (augments.isEmpty()){
            Type.EMPTY
        } else if (augments.size == 1){
            Type.SINGLE
        } else{
            Type.PAIRED
        }
    }

    private val castParticleEffect by lazy{
        when(type){
            Type.SINGLE -> augments[0].castParticleType()
            Type.PAIRED -> augments[1].castParticleType()
            Type.EMPTY -> ParticleTypes.CRIT
        }
    }

    fun getCastParticleType(): ParticleEffect {
        return castParticleEffect
    }

    fun getHitParticleType(hit: HitResult): ParticleEffect {
        return when(type){
            Type.SINGLE -> augments[0].hitParticleType(hit)
            Type.PAIRED -> augments[1].hitParticleType(hit)
            Type.EMPTY -> ParticleTypes.CRIT
        }
    }

    fun processAugmentEffects(user: LivingEntity,modifierData: AugmentModifier): AugmentEffect{
        val effectModifiers = AugmentEffect(
            PerLvlF(0f,0f,(user.getAttributeValue(RegisterAttribute.SPELL_DAMAGE).toFloat() - 1f) * 100f),
            PerLvlI(user.getAttributeValue(RegisterAttribute.SPELL_AMPLIFIER).toInt()),
            PerLvlI(0,0,(user.getAttributeValue(RegisterAttribute.SPELL_DURATION).toInt() - 1) * 100),
            PerLvlD(0.0,0.0,(user.getAttributeValue(RegisterAttribute.SPELL_RANGE) - 1.0) * 100.0)
        )
        effectModifiers.plus(modifierData.getEffectModifier())
        return when (type){
            Type.SINGLE ->
                effectModifiers.plus(augments[0].baseEffect)
            Type.PAIRED -> {
                when (augments[1].modificationInfo().damageModificationType){
                    ModificationType.DEFER ->
                        effectModifiers.addDamage(augments[0].baseEffect)
                    ModificationType.MODIFY ->
                        effectModifiers.addDamage(augments[0].baseEffect).addDamage(augments[1].modificationEffect)
                    ModificationType.REPLACE ->
                        when(augments[0].modificationInfo().damageModificationType){
                            ModificationType.DEFER ->
                                effectModifiers.addDamage(augments[1].baseEffect)
                            ModificationType.MODIFY ->
                                effectModifiers.addDamage(augments[1].baseEffect).addDamage(augments[0].modificationEffect)
                            ModificationType.REPLACE ->
                                effectModifiers.addDamage(augments[1].baseEffect)
                        }
                }
                when (augments[1].modificationInfo().amplifierModificationType){
                    ModificationType.DEFER ->
                        effectModifiers.addAmplifier(augments[0].baseEffect)
                    ModificationType.MODIFY ->
                        effectModifiers.addAmplifier(augments[0].baseEffect).addAmplifier(augments[1].modificationEffect)
                    ModificationType.REPLACE ->
                        when(augments[0].modificationInfo().amplifierModificationType){
                            ModificationType.DEFER ->
                                effectModifiers.addAmplifier(augments[1].baseEffect)
                            ModificationType.MODIFY ->
                                effectModifiers.addAmplifier(augments[1].baseEffect).addAmplifier(augments[0].modificationEffect)
                            ModificationType.REPLACE ->
                                effectModifiers.addAmplifier(augments[1].baseEffect)
                        }
                }
                when (augments[1].modificationInfo().durationModificationType){
                    ModificationType.DEFER ->
                        effectModifiers.addDuration(augments[0].baseEffect)
                    ModificationType.MODIFY ->
                        effectModifiers.addDuration(augments[0].baseEffect).addDuration(augments[1].modificationEffect)
                    ModificationType.REPLACE ->
                        when(augments[0].modificationInfo().durationModificationType){
                            ModificationType.DEFER ->
                                effectModifiers.addDuration(augments[1].baseEffect)
                            ModificationType.MODIFY ->
                                effectModifiers.addDuration(augments[1].baseEffect).addDuration(augments[0].modificationEffect)
                            ModificationType.REPLACE ->
                                effectModifiers.addDuration(augments[1].baseEffect)
                        }
                }
                when (augments[1].modificationInfo().rangeModificationType){
                    ModificationType.DEFER ->
                        effectModifiers.addRange(augments[0].baseEffect)
                    ModificationType.MODIFY ->
                        effectModifiers.addRange(augments[0].baseEffect).addRange(augments[1].modificationEffect)
                    ModificationType.REPLACE ->
                        when(augments[0].modificationInfo().rangeModificationType){
                            ModificationType.DEFER ->
                                effectModifiers.addRange(augments[1].baseEffect)
                            ModificationType.MODIFY ->
                                effectModifiers.addRange(augments[1].baseEffect).addRange(augments[0].modificationEffect)
                            ModificationType.REPLACE ->
                                effectModifiers.addRange(augments[1].baseEffect)
                        }
                }
                effectModifiers
            }
            Type.EMPTY ->
                effectModifiers
        }
    }

    fun processMultipleEntityHits(entityHitResults: List<EntityHitResult>, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect): List<Identifier>{
        var successes = 0
        val actionList: MutableList<Identifier> = mutableListOf()
        for (entityHitResult in entityHitResults){
            if(processEntityHit(entityHitResult,world,source,user,hand,level,effects).also { actionList.addAll(it) }.isNotEmpty()) {
                successes++
                val entity = entityHitResult.entity
                if (entity is LivingEntity){
                    effects.accept(entity, AugmentConsumer.Type.HARMFUL)
                }
            }
        }
        if (successes > 0){
            EntityHitActionEvent.EVENT.invoker().onAction(world,user,actionList, *entityHitResults.toTypedArray())
            effects.accept(user, AugmentConsumer.Type.BENEFICIAL)
        }
        return actionList
    }
    
    fun processSingleEntityHit(entityHitResult: EntityHitResult, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect): List<Identifier>{
        val actionList = processEntityHit(entityHitResult,world,source, user, hand, level, effects)
        if (actionList.isNotEmpty()){
            val entity = entityHitResult.entity
            if (entity is LivingEntity){
                effects.accept(entity, AugmentConsumer.Type.HARMFUL)
            }
            EntityHitActionEvent.EVENT.invoker().onAction(world,user,actionList, entityHitResult)
            effects.accept(user, AugmentConsumer.Type.BENEFICIAL)
        }
        return actionList
    }
    
    private fun processEntityHit(entityHitResult: EntityHitResult, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect): List<Identifier>{
        val returnList: MutableList<Identifier> = mutableListOf()
        if (type == Type.PAIRED){
            val result = augments[1].onEntityHit(entityHitResult, world,source, user,hand,level, effects,augments[0].augmentType, this)
            if (result.result.isAccepted){
                returnList.addAll(result.value)
                val result2 = augments[0].onEntityHit(entityHitResult, world,source, user,hand,level, effects, AugmentType.EMPTY, this)
                if (result2.result.isAccepted){
                    returnList.addAll(result.value)
                }
            }
        } else {
            for (augment in augments) {
                val result = augment.onEntityHit(entityHitResult, world,source, user, hand, level, effects, AugmentType.EMPTY, this)
                if (result.result.isAccepted){
                    returnList.addAll(result.value)
                }
            }
        }
        return returnList
    }
    
    fun processMultipleBlockHits(blockHitResults: List<BlockHitResult>, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect): List<Identifier>{
        var successes = 0
        val actionList: MutableList<Identifier> = mutableListOf()
        for (blockHitResult in blockHitResults){
            if(processBlockHit(blockHitResult,world,source,user,hand,level,effects).also { actionList.addAll(it) }.isNotEmpty()) {
                successes++
            }
        }
        if (successes > 0){
            BlockHitActionEvent.EVENT.invoker().onAction(world,user,actionList,*blockHitResults.toTypedArray())
            effects.accept(user, AugmentConsumer.Type.BENEFICIAL)
        }
        return actionList
    }

    fun processSingleBlockHit(blockHitResult: BlockHitResult, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect): List<Identifier>{
        val actionList = processBlockHit(blockHitResult,world,source, user, hand, level, effects)
        if (actionList.isNotEmpty()){
            BlockHitActionEvent.EVENT.invoker().onAction(world,user,actionList,blockHitResult)
            effects.accept(user, AugmentConsumer.Type.BENEFICIAL)
        }
        return actionList
    }
    
    private fun processBlockHit(blockHitResult: BlockHitResult, world: World,source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect): List<Identifier>{
        val returnList: MutableList<Identifier> = mutableListOf()
        if (type == Type.PAIRED){
            val result = augments[1].onBlockHit(blockHitResult, world,source, user,hand,level, effects,augments[0].augmentType, this)
            if (result.result.isAccepted){
                returnList.addAll(result.value)
                val result2 = augments[0].onBlockHit(blockHitResult, world,source, user,hand,level, effects, AugmentType.EMPTY, this)
                if (result2.result.isAccepted){
                    returnList.addAll(result.value)
                }
            }
        } else {
            for (augment in augments) {
                val result = augment.onBlockHit(blockHitResult, world,source, user, hand, level, effects, AugmentType.EMPTY, this)
                if (result.result.isAccepted){
                    returnList.addAll(result.value)
                }
            }
        }
        return returnList
    }

    fun processOnKill(entityHitResult: EntityHitResult, world: World,source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect){
        if (type == Type.PAIRED){
            val result = augments[1].onEntityKill(entityHitResult, world,source, user,hand,level, effects,augments[0].augmentType, this)
            if (result.result.isAccepted){
                augments[0].onEntityKill(entityHitResult, world,source, user,hand,level, effects, AugmentType.EMPTY, this)
            }
        } else {
            for (augment in augments) {
                val result = augment.onEntityKill(entityHitResult, world,source, user, hand, level, effects, AugmentType.EMPTY, this)
                if (!result.result.isAccepted) break
            }
        }
    }
    
    fun provideDamage(amount: Float, cause: ScepterAugment, entityHitResult: EntityHitResult, user: LivingEntity, world: World, hand: Hand, level: Int, effects: AugmentEffect): Float{
        return if (type == Type.PAIRED){
            if (cause == augments[0]){
                return augments[1].modifyDamage(amount,cause, entityHitResult, user, world, hand, level, effects, augments[0].augmentType, this)
            } else {
                return augments[0].modifyDamage(amount,cause, entityHitResult, user, world, hand, level, effects, augments[1].augmentType, this)
            }
        }
        return amount
    }
    
    fun provideDamageSource(builder: DamageSourceBuilder, cause: ScepterAugment, entityHitResult: EntityHitResult, source: Entity?, user: LivingEntity, world: World, hand: Hand, level: Int, effects: AugmentEffect): DamageSource{
        return if (type = Type.PAIRED){
            if (cause == augments[0]){
                augments[1].modifyDamageSource(builder,cause, entityHitResult, source, user, world, hand, level, effects, augments[0].augmentType, this).build()
            } else {
                augments[0].modifyDamageSource(builder,cause, entityHitResult, source, user, world, hand, level, effects, augments[1].augmentType, this).build()
            }
        } else {
            builder.build()
        }
    }
    
    fun provideSummons(summons: List<Entity>,cause: ScepterAugment, user: LivingEntity, world: World, hand: Hand, level: Int, effects: AugmentEffect): List<Entity>{
        return if (type == Type.PAIRED){
            if (cause == augments[0]) {
                augments[1].modifySummons(summons,cause, user, world, hand, level, effects, augments[0].augmentType, this)
            } else {
                augments[0].modifySummons(summons,cause, user, world, hand, level, effects, augments[1].augmentType, this)
            }
        } else {
            summons
        }
    }
    
    fun causeExplosion(builder: ExplosionBuilder,cause: ScepterAugment, user: LivingEntity, world: World, hand: Hand, level: Int, effects: AugmentEffect){
        return if (type = Type.PAIRED){
            if (cause == augments[0]) {
                augments[1].modifyExplosion(builder,cause, user, world, hand, level, effects, augments[0].augmentType, this).explode(world)
            } else {
                augments[0].modifyExplosion(builder,cause, user, world, hand, level, effects, augments[1].augmentType, this).explode(world)
            }
        } else {
            builder.explode(world)
        }
    }

    private enum class Type{
        EMPTY,
        SINGLE,
        PAIRED
    }

}