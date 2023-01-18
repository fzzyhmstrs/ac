package me.fzzyhmstrs.amethyst_core.modifier_util

import com.google.common.collect.HashMultimap
import com.google.common.collect.Multimap
import net.minecraft.block.BlockState
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import java.util.*

class EquipmentModifier(modifierId: Identifier = ModifierDefaults.BLANK_ID, val persistent: Boolean = false, val randomSelectable: Boolean = false): AbstractModifier<EquipmentModifier>(modifierId) {

    private val attributeModifiers: Multimap<EntityAttribute, EntityAttributeModifier> = HashMultimap.create()
    private val postHitConsumers: MutableList<ToolConsumer> = mutableListOf()
    private val postMineConsumers: MutableList<MiningConsumer> = mutableListOf()
    private val onUseConsumers: MutableList<ToolConsumer> = mutableListOf()
    private val onDamagedConsumers: MutableList<DamageConsumer> = mutableListOf()
    private val killOtherConsumers: MutableList<ToolConsumer> = mutableListOf()
    private val tickConsumers: MutableList<ToolConsumer> = mutableListOf()

    override fun plus(other: EquipmentModifier): EquipmentModifier {
        attributeModifiers.putAll(other.attributeModifiers)
        postHitConsumers.addAll(other.postHitConsumers)
        postMineConsumers.addAll(other.postMineConsumers)
        onUseConsumers.addAll(other.onUseConsumers)
        onDamagedConsumers.addAll(other.onDamagedConsumers)
        killOtherConsumers.addAll(other.killOtherConsumers)
        tickConsumers.addAll(other.tickConsumers)
        return this
    }

    fun withAttributeModifier(attribute: EntityAttribute, uuid: String, amount: Double, operation: EntityAttributeModifier.Operation): EquipmentModifier {
        val modifier = EntityAttributeModifier(UUID.fromString(uuid), this::getTranslationKey,amount,operation)
        attributeModifiers.put(attribute,modifier)
        return this
    }

    fun attributeModifiers(): Multimap<EntityAttribute, EntityAttributeModifier>{
        return attributeModifiers
    }

    fun withPostHit(onHit: ToolConsumer): EquipmentModifier {
        postHitConsumers.add(onHit)
        return this
    }

    fun postHit(stack: ItemStack, user: LivingEntity, target: LivingEntity?){
        postHitConsumers.forEach {
            it.apply(stack, user, target)
        }
    }

    fun withPostMine(onMine: MiningConsumer): EquipmentModifier {
        postMineConsumers.add(onMine)
        return this
    }

    fun postMine(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: PlayerEntity){
        postMineConsumers.forEach {
            it.apply(stack, world, state, pos, miner)
        }
    }

    fun withOnUse(onUse: ToolConsumer): EquipmentModifier {
        onUseConsumers.add(onUse)
        return this
    }

    fun onUse(stack: ItemStack, user: LivingEntity, target: LivingEntity?){
        onUseConsumers.forEach {
            it.apply(stack, user, target)
        }
    }

    fun withOnDamaged(onDamaged: DamageConsumer): EquipmentModifier {
        onDamagedConsumers.add(onDamaged)
        return this
    }

    fun onDamaged(stack: ItemStack, user: LivingEntity, attacker: LivingEntity?, source: DamageSource, amount: Float){
        onDamagedConsumers.forEach {
            it.apply(stack, user, attacker, source, amount)
        }
    }

    fun withKilledOther(killOther: ToolConsumer): EquipmentModifier {
        killOtherConsumers.add(killOther)
        return this
    }

    fun killedOther(stack: ItemStack, user: LivingEntity, target: LivingEntity?){
        killOtherConsumers.forEach {
            it.apply(stack, user, target)
        }
    }

    fun withTick(tick: ToolConsumer): EquipmentModifier {
        tickConsumers.add(tick)
        return this
    }

    fun tick(stack: ItemStack, user: LivingEntity, target: LivingEntity?){
        tickConsumers.forEach {
            it.apply(stack, user, target)
        }
    }
    
    fun withDescendant(modifier: EquipmentModifier): EquipmentModifier {
        addDescendant(modifier)
        return this
    }

    override fun compiler(): Compiler {
        return Compiler(mutableListOf(), EquipmentModifier())
    }

    override fun getModifierHelper(): AbstractModifierHelper<*> {
        return EquipmentModifierHelper
    }

    @FunctionalInterface
    fun interface ToolConsumer{
        fun apply(stack: ItemStack, user: LivingEntity, target: LivingEntity?)
    }

    @FunctionalInterface
    fun interface MiningConsumer{
        fun apply(stack: ItemStack, world: World, state: BlockState, pos: BlockPos, miner: PlayerEntity)
    }

    @FunctionalInterface
    fun interface DamageConsumer{
        fun apply(stack: ItemStack, user: LivingEntity, attacker: LivingEntity?, source: DamageSource, amount: Float)
    }

}
