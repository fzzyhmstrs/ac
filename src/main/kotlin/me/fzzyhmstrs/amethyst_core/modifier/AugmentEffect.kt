package me.fzzyhmstrs.amethyst_core.modifier

import com.google.common.collect.ArrayListMultimap
import com.google.common.collect.Multimap
import me.fzzyhmstrs.fzzy_core.coding_util.*
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import kotlin.math.max

/**
 * A data container for [AugmentModifier] instances. The majority of the modifications spells make to their effect happen via the AugmentEffect itself.
 *
 * AugmentEffect is a level-based bucket, allowing spells to have scaling effects as they level up.
 *
 * [damage]: straightforward, this bucket holds a modifiable damage value. A [ScepterAugment][me.fzzyhmstrs.amethyst_core.scepter.augments.ScepterAugment] will have default values in its builtin AugmentEffect, and then [Augment Modifiers][AugmentModifier] pass modifications to that base through their compiled effect.
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
    internal var damageData: PerLvlF = PerLvlF(),
    internal var amplifierData: PerLvlI = PerLvlI(),
    internal var durationData: PerLvlI = PerLvlI(),
    internal var rangeData: PerLvlD = PerLvlD()
): Addable<AugmentEffect>{
    private var consumers: Multimap<AugmentConsumer.Type,AugmentConsumer> = ArrayListMultimap.create()

    private constructor( damageData: PerLvlF = PerLvlF(),
                         amplifierData: PerLvlI = PerLvlI(),
                         durationData: PerLvlI = PerLvlI(),
                         rangeData: PerLvlD = PerLvlD(),
                         consumers: Multimap<AugmentConsumer.Type,AugmentConsumer>): this(damageData, amplifierData, durationData, rangeData){
                             this.consumers = consumers
                         }

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
    fun withConsumers(vararg consumers: AugmentConsumer): AugmentEffect {
        for (consumer in consumers) {
            this.consumers.put(consumer.type, consumer)
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

    fun writeNbt(): NbtCompound{
        val nbtCompound = NbtCompound()
        nbtCompound.put("damage",ScalingPrimitivesHelper.writeNbt(damageData))
        nbtCompound.put("amplifier",ScalingPrimitivesHelper.writeNbt(amplifierData))
        nbtCompound.put("duration",ScalingPrimitivesHelper.writeNbt(durationData))
        nbtCompound.put("range",ScalingPrimitivesHelper.writeNbt(rangeData))
        val consumerCompound = NbtCompound()
        for (type in consumers.keySet()){
            val typeConsumers = consumers[type]
            val typeList = AugmentConsumer.toNbtList(typeConsumers)
            consumerCompound.put(type.name,typeList)
        }
        nbtCompound.put("consumers",consumerCompound)
        return nbtCompound
    }

    companion object{
        fun readNbt(nbtCompound: NbtCompound): AugmentEffect{
            val damageData = ScalingPrimitivesHelper.readNbt(nbtCompound.getCompound("damage"))
            val amplifierData = ScalingPrimitivesHelper.readNbt(nbtCompound.getCompound("amplifier"))
            val durationData = ScalingPrimitivesHelper.readNbt(nbtCompound.getCompound("duration"))
            val rangeData = ScalingPrimitivesHelper.readNbt(nbtCompound.getCompound("range"))
            val consumerCompound = nbtCompound.getCompound("consumers")
            val consumerMap: Multimap<AugmentConsumer.Type,AugmentConsumer> = ArrayListMultimap.create()
            for (type in AugmentConsumer.Type.values()){
                if (consumerCompound.contains(type.name,NbtElement.LIST_TYPE.toInt())){
                    val nbtList = consumerCompound.getList(type.name,NbtElement.STRING_TYPE.toInt())
                    val list = AugmentConsumer.fromNbtList(nbtList)
                    consumerMap.putAll(type,list)
                }
            }
            return AugmentEffect(damageData as PerLvlF,amplifierData as PerLvlI,durationData as PerLvlI,rangeData as PerLvlD,consumerMap)
        }
    }

}