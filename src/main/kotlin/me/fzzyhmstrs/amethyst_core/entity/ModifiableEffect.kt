package me.fzzyhmstrs.amethyst_core.entity

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleRegistry
import net.minecraft.util.Identifier
import java.util.function.BiConsumer
import java.util.function.Consumer

class ModifiableEffect private constructor (private val consumer: BiConsumer<Entity,ProcessContext?>){

    fun run(entity: Entity, context: ProcessContext?){
        consumer.accept(entity, context)
    }

    companion object{
        val REGISTRY : SimpleRegistry<ModifiableEffect> = FabricRegistryBuilder.createSimple(RegistryKey.ofRegistry<ModifiableEffect>(Identifier(AC.MOD_ID,"tick_effects"))).buildAndRegister()

        fun createAndRegisterConsumer(id: Identifier, consumer: BiConsumer<Entity, ProcessContext?>): ModifiableEffect{
            return Registry.register(REGISTRY,id, ModifiableEffect(consumer))
        }

        fun toNbtList(effects: Collection<ModifiableEffect>): NbtList {
            val list = NbtList()
            for (consumer in effects){
                val id = REGISTRY.getId(consumer)
                if (id != null)
                    list.add(NbtString.of(id.toString()))
            }
            return list
        }

        fun fromNbtList(list: NbtList): List<ModifiableEffect>{
            if (list.heldType != NbtElement.STRING_TYPE) return emptyList()
            val consumers: MutableList<ModifiableEffect> = mutableListOf()
            for (el in list){
                val idString = el.asString()
                val id = Identifier(idString)
                val consumer = REGISTRY.get(id)
                if (consumer != null)
                    consumers.add(consumer)
            }
            return consumers
        }

    } 
}
