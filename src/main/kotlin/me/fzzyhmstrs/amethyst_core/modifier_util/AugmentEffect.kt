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
    private var damageData: PerLvlF? = null,
    private var amplifierData: PerLvlI? = null,
    private var durationData: PerLvlI? = null,
    private var rangeData: PerLvlD? = null
): Addable<AugmentEffect>{
    private var consumers: Multimap<AugmentConsumer.Type,AugmentConsumer> = ArrayListMultimap.create()

    private fun damageData(): PerLvlF{
        return if (damageData == null)
            PerLvlF().also { damageData = it }
        else
            return damageData as PerLvlF
    }
    private fun amplifierData(): PerLvlI{
        return if (amplifierData == null)
            PerLvlI().also { amplifierData = it }
        else
            return amplifierData as PerLvlI
    }
    private fun durationData(): PerLvlI{
        return if (durationData == null)
            PerLvlI().also { durationData = it }
        else
            return durationData as PerLvlI
    }
    private fun rangeData(): PerLvlD{
        return if (rangeData == null)
            PerLvlD().also { rangeData = it }
        else
            return rangeData as PerLvlD
    }

    override fun plus(other: AugmentEffect): AugmentEffect {
        if (other.damageData != null)
            damageData().plus(other.damageData())
        if (other.amplifierData != null)
            amplifierData().plus(other.amplifierData())
        if (other.durationData != null)
            durationData().plus(other.durationData())
        if (other.rangeData != null)
            rangeData().plus(other.rangeData())
        consumers.putAll(other.consumers)
        /*for (key in other.consumers.keySet()){
            println("adding an augment effect consumer of type $key")
            consumers.putAll(key,other.consumers.get(key))
        }*/
        return this
    }
    fun damage(level: Int = 0): Float{
        return max(0.0F, damageData?.value(level) ?: 0f)
    }
    fun amplifier(level: Int = 0): Int{
        return max(0, amplifierData?.value(level) ?: 0)
    }
    fun duration(level: Int = 0): Int{
        return max(0, durationData?.value(level) ?: 0)
    }
    fun range(level: Int = 0): Double{
        return max(1.0, rangeData?.value(level) ?: 1.0)
    }
    fun consumers(): Multimap<AugmentConsumer.Type,AugmentConsumer>{
        return consumers
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
        damageData().set(damage, damagePerLevel, damagePercent)
        return this
    }
    fun addDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F){
        damageData().plus(damage, damagePerLevel, damagePercent)
    }
    fun addDamage(ae: AugmentEffect){
        damageData().plus(ae.damageData())
    }
    fun setDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F){
        damageData().set(damage, damagePerLevel, damagePercent)
    }
    fun withAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0): AugmentEffect {
        amplifierData().set(amplifier, amplifierPerLevel, amplifierPercent)
        return this
    }
    fun addAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0){
        amplifierData().plus(amplifier, amplifierPerLevel, amplifierPercent)
    }
    fun addAmplifier(ae: AugmentEffect){
        amplifierData().plus(ae.amplifierData())
    }
    fun setAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0){
        amplifierData().set(amplifier, amplifierPerLevel, amplifierPercent)
    }
    fun withDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0): AugmentEffect {
        durationData().set(duration, durationPerLevel, durationPercent)
        return this
    }
    fun addDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0){
        durationData().plus(duration, durationPerLevel, durationPercent)
    }
    fun addDuration(ae: AugmentEffect){
        durationData().plus(ae.durationData())
    }
    fun setDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0){
        durationData().set(duration, durationPerLevel, durationPercent)
    }
    fun withRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0): AugmentEffect {
        rangeData().plus(PerLvlD(range, rangePerLevel, rangePercent))
        return this
    }
    fun addRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0){
        rangeData().plus(PerLvlD(range, rangePerLevel, rangePercent))
    }
    fun addRange(ae: AugmentEffect){
        rangeData().plus(ae.rangeData())
    }
    fun setRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0){
        rangeData().set(range, rangePerLevel, rangePercent)
    }
    fun withConsumer(consumer: Consumer<List<LivingEntity>>, type: AugmentConsumer.Type): AugmentEffect {
        addConsumer(consumer, type)
        return this
    }
    fun addConsumer(consumer: Consumer<List<LivingEntity>>, type: AugmentConsumer.Type){
        consumers.put(type,AugmentConsumer(consumer, type))
    }
    fun addConsumers(list: List<AugmentConsumer>){
        list.forEach {
            consumers.put(it.type,AugmentConsumer(it.consumer, it.type))


        }
    }
    fun setConsumers(list: MutableList<AugmentConsumer>, type: AugmentConsumer.Type){
        consumers[type].clear()
        consumers.putAll(type,list)
    }
    fun setConsumers(ae: AugmentEffect){
        consumers = ae.consumers
    }
}