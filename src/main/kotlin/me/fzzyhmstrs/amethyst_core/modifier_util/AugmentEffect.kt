package me.fzzyhmstrs.amethyst_core.modifier_util

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import me.fzzyhmstrs.fzzy_core.coding_util.Addable
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlD
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlF
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.entity.LivingEntity
import java.util.function.Consumer
import kotlin.math.max

/**
 * A data container for [AugmentModifier] instances. The majority of the modifications spells make to their effect happen via the AugmentEffect itself.
 *
 * AugmentEffect is a level-based bucket, allowing spells to have scaling effects as they level up.
 *
 * [damage]: straightforward, this bucket holds a modifiable damage value. A [ScepterAugment][me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment] will have default values in its builtin AugmentEffect, and then [Augment Modifiers][AugmentModifier] pass modifications to that base through their compiled effect.
 *
 * [amplifier]: bucket for effects that have various levels of effect power. The simplest case for this is a spell that applies status effects.
 *
 * [duration]: bucket for holding the duration of an effect. Useful for status effects, or something like setting fire ticks.
 *
 * [range]: bucket for range of effect. Might be size of a effect cloud, or search range when selecting targets.
 *
 * [consumers]: advanced effects go here. Spells will pass a list of LivingEntity to the [accept] method and all compiled consumers will apply to that list. See the [ModifierRegistry][me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry] for example consumers.
 */
data class AugmentEffect(
    private var damageData: PerLvlF = PerLvlF(),
    private var amplifierData: PerLvlI = PerLvlI(),
    private var durationData: PerLvlI = PerLvlI(),
    private var rangeData: PerLvlD = PerLvlD()
): Addable<AugmentEffect>{
    private var consumers: Multimap<AugmentConsumer.Type,AugmentConsumer> = ArrayListMultimap.create()

    override fun plus(other: AugmentEffect): AugmentEffect {
        damageData = damageData.plus(other.damageData)
        amplifierData = amplifierData.plus(other.amplifierData)
        durationData = durationData.plus(other.durationData)
        rangeData = rangeData.plus(other.rangeData)
        for (key in other.consumers.keys()){
            consumers.putAll(key,other.consumers.get(key))
        }
        return this
    }
    fun damage(level: Int = 0): Float{
        return max(0.0F, damageData.value(level))
    }
    fun amplifier(level: Int = 0): Int{
        return max(0, amplifierData.value(level))
    }
    fun duration(level: Int = 0): Int{
        return max(0, durationData.value(level))
    }
    fun range(level: Int = 0): Double{
        return max(1.0, rangeData.value(level))
    }
    fun consumers(): MutableList<AugmentConsumer>{
        val list = mutableListOf<AugmentConsumer>()
        for (key in consumers.keys()){
            consumers[key].forEach {
                list.add(it)
            }
        }
        return list
    }
    fun accept(list: List<LivingEntity>, type: AugmentConsumer.Type? = null){
        consumers[type].forEach {
            it.consumer.accept(list)
        }
    }
    fun accept(entity: LivingEntity, type: AugmentConsumer.Type? = null){
        accept(listOf(entity), type)
    }

    fun withDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F): AugmentEffect {
        return this.copy(damageData = PerLvlF(damage, damagePerLevel, damagePercent))
    }
    fun addDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F): AugmentEffect{
        damageData.plus(PerLvlF(damage, damagePerLevel, damagePercent))
        return this
    }
    fun addDamage(ae: AugmentEffect): AugmentEffect{
        damageData.plus(ae.damageData)
        return this
    }
    fun setDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F): AugmentEffect{
        damageData = PerLvlF(damage, damagePerLevel, damagePercent)
        return this
    }
    fun withAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0): AugmentEffect {
        return this.copy(amplifierData = PerLvlI(amplifier, amplifierPerLevel, amplifierPercent))
    }
    fun addAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0): AugmentEffect{
        amplifierData.plus(PerLvlI(amplifier, amplifierPerLevel, amplifierPercent))
        return this
    }
    fun addAmplifier(ae: AugmentEffect): AugmentEffect{
        amplifierData.plus(ae.amplifierData)
        return this
    }
    fun setAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0): AugmentEffect{
        amplifierData = PerLvlI(amplifier, amplifierPerLevel, amplifierPercent)
        return this
    }
    fun withDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0): AugmentEffect {
        return this.copy(durationData = PerLvlI(duration, durationPerLevel, durationPercent))
    }
    fun addDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0): AugmentEffect{
        durationData.plus(PerLvlI(duration, durationPerLevel, durationPercent))
        return this
    }
    fun addDuration(ae: AugmentEffect): AugmentEffect{
        durationData.plus(ae.durationData)
        return this
    }
    fun setDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0): AugmentEffect{
        durationData = PerLvlI(duration, durationPerLevel, durationPercent)
        return this
    }
    fun withRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0): AugmentEffect {
        return this.copy(rangeData = PerLvlD(range, rangePerLevel, rangePercent))
    }
    fun addRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0): AugmentEffect{
        rangeData.plus(PerLvlD(range, rangePerLevel, rangePercent))
        return this
    }
    fun addRange(ae: AugmentEffect): AugmentEffect{
        rangeData.plus(ae.rangeData)
        return this
    }
    fun setRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0): AugmentEffect{
        rangeData = PerLvlD(range, rangePerLevel, rangePercent)
        return this
    }
    fun withConsumer(consumer: Consumer<List<LivingEntity>>, type: AugmentConsumer.Type): AugmentEffect {
        addConsumer(consumer, type)
        return this
    }
    fun addConsumer(consumer: Consumer<List<LivingEntity>>, type: AugmentConsumer.Type): AugmentEffect{
        consumers.put(type,AugmentConsumer(consumer, type))
        return this
    }
    fun addConsumers(list: List<AugmentConsumer>): AugmentEffect{
        list.forEach {
            consumers.put(it.type,AugmentConsumer(it.consumer, it.type))
        }
        return this
    }
    fun setConsumers(list: MutableList<AugmentConsumer>, type: AugmentConsumer.Type): AugmentEffect{
        consumers[type].clear()
        consumers.putAll(type,list)
        return this
    }
    fun setConsumers(ae: AugmentEffect): AugmentEffect{
        consumers = ae.consumers
        return this
    }
}