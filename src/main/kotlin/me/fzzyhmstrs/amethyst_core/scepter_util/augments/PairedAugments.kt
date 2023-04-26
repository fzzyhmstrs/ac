package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlD
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlF
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.entity.LivingEntity
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
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
            Type.EMPTY -> ParticleTypes.CRIT
            Type.SINGLE -> augments[0].castParticleType()
            Type.PAIRED -> augments[1].castParticleType()
        }
    }

    fun getCastParticleType(): ParticleEffect {
        return castParticleEffect
    }

    fun getHitParticleType(hit: HitResult): ParticleEffect {
        return when(type){
            Type.EMPTY -> ParticleTypes.CRIT
            Type.SINGLE -> augments[0].hitParticleType(hit)
            Type.PAIRED -> augments[1].hitParticleType(hit)
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
            Type.EMPTY ->
                effectModifiers
            Type.PAIRED -> {
                when (augments[1].damageModificationType()){
                    ModificationType.DEFER ->
                        effectModifiers.addDamage(augments[0].baseEffect)
                    ModificationType.MODIFY ->
                        effectModifiers.addDamage(augments[0].baseEffect).addDamage(augments[1].modificationEffect)
                    ModificationType.REPLACE ->
                        when(augments[0].damageModificationType()){
                            ModificationType.DEFER ->
                                effectModifiers.addDamage(augments[1].baseEffect)
                            ModificationType.MODIFY ->
                                effectModifiers.addDamage(augments[1].baseEffect).addDamage(augments[0].modificationEffect)
                            ModificationType.REPLACE ->
                                effectModifiers.addDamage(augments[1].baseEffect)
                        }
                }
                when (augments[1].amplifierModificationType()){
                    ModificationType.DEFER ->
                        effectModifiers.addAmplifier(augments[0].baseEffect)
                    ModificationType.MODIFY ->
                        effectModifiers.addAmplifier(augments[0].baseEffect).addAmplifier(augments[1].modificationEffect)
                    ModificationType.REPLACE ->
                        when(augments[0].amplifierModificationType()){
                            ModificationType.DEFER ->
                                effectModifiers.addAmplifier(augments[1].baseEffect)
                            ModificationType.MODIFY ->
                                effectModifiers.addAmplifier(augments[1].baseEffect).addAmplifier(augments[0].modificationEffect)
                            ModificationType.REPLACE ->
                                effectModifiers.addAmplifier(augments[1].baseEffect)
                        }
                }
                when (augments[1].durationModificationType()){
                    ModificationType.DEFER ->
                        effectModifiers.addDuration(augments[0].baseEffect)
                    ModificationType.MODIFY ->
                        effectModifiers.addDuration(augments[0].baseEffect).addDuration(augments[1].modificationEffect)
                    ModificationType.REPLACE ->
                        when(augments[0].durationModificationType()){
                            ModificationType.DEFER ->
                                effectModifiers.addDuration(augments[1].baseEffect)
                            ModificationType.MODIFY ->
                                effectModifiers.addDuration(augments[1].baseEffect).addDuration(augments[0].modificationEffect)
                            ModificationType.REPLACE ->
                                effectModifiers.addDuration(augments[1].baseEffect)
                        }
                }
                when (augments[1].rangeModificationType()){
                    ModificationType.DEFER ->
                        effectModifiers.addRange(augments[0].baseEffect)
                    ModificationType.MODIFY ->
                        effectModifiers.addRange(augments[0].baseEffect).addRange(augments[1].modificationEffect)
                    ModificationType.REPLACE ->
                        when(augments[0].rangeModificationType()){
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

        }
    }

    fun processEntityHit(entityHitResult: EntityHitResult, world: World, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect){
        for (augment in augments){
            val result = augment.onEntityHit(entityHitResult, world, user,hand,level, effects)
            if (result == ActionResult.SUCCESS || result == ActionResult.FAIL) break
        }
    }

    fun processBlockHit(blockHitResult: BlockHitResult, world: World, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect){
        for (augment in augments){
            val result = augment.onBlockHit(blockHitResult, world, user,hand,level, effects)
            if (result == ActionResult.SUCCESS || result == ActionResult.FAIL) break
        }
    }

    private enum class Type{
        EMPTY,
        SINGLE,
        PAIRED
    }

}