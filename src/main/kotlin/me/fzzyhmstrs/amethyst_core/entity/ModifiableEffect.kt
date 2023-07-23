package me.fzzyhmstrs.amethyst_core.entity

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleRegistry
import net.minecraft.util.Identifier
import java.util.function.BiConsumer
import java.util.function.Consumer

class ModifiableEffect private constructor (private val consumer: EffectConsumer){

    fun run(entity: Entity, owner: Entity?, context: ProcessContext){
        consumer.run(entity,owner, context)
    }

    companion object{
        val REGISTRY : SimpleRegistry<ModifiableEffect> = FabricRegistryBuilder.createSimple(RegistryKey.ofRegistry<ModifiableEffect>(AC.identity("tick_effects"))).buildAndRegister()

        fun createAndRegisterConsumer(id: Identifier, consumer: EffectConsumer): ModifiableEffect{
            return Registry.register(REGISTRY,id, ModifiableEffect(consumer))
        }

        fun toNbtList(effects: Collection<ModifiableEffectInstance>): NbtList {
            val list = NbtList()
            for (instance in effects){
                list.add(instance.toNbt())
            }
            return list
        }

        fun fromNbtList(list: NbtList): List<ModifiableEffectInstance>{
            if (list.heldType != NbtElement.COMPOUND_TYPE) return emptyList()
            val instances: MutableList<ModifiableEffectInstance> = mutableListOf()
            for (el in list){
                val instance = ModifiableEffectInstance.fromNbt(el as NbtCompound)
                instances.add(instance)
            }
            return instances
        }

    }

    @FunctionalInterface
    fun interface EffectConsumer{
        fun run(entity: Entity, owner: Entity?, context: ProcessContext)
    }
}
