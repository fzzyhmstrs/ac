package me.fzzyhmstrs.amethyst_core.modifier

import me.fzzyhmstrs.amethyst_core.AC
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtElement
import net.minecraft.nbt.NbtList
import net.minecraft.nbt.NbtString
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleRegistry
import net.minecraft.util.Identifier
import java.util.function.Consumer

/**
 * a simple container that holds a consumer and a type notation for sorting.
 */
data class AugmentConsumer private constructor(val consumer: Consumer<List<LivingEntity>>, val type: Type) {
    enum class Type {
        HARMFUL,
        BENEFICIAL,
        AUTOMATIC;
    }

    companion object{
        val REGISTRY : SimpleRegistry<AugmentConsumer> = FabricRegistryBuilder.createSimple(RegistryKey.ofRegistry<AugmentConsumer>(Identifier(AC.MOD_ID,"augment_consumers"))).buildAndRegister()

        fun createAndRegisterConsumer(id: Identifier, consumer: Consumer<List<LivingEntity>>, type: Type): AugmentConsumer{
            return Registry.register(REGISTRY,id, AugmentConsumer(consumer, type))
        }

        fun toNbtList(consumers: Collection<AugmentConsumer>): NbtList{
            val list = NbtList()
            for (consumer in consumers){
                val id = REGISTRY.getId(consumer)
                if (id != null)
                    list.add(NbtString.of(id.toString()))
            }
            return list
        }

        fun fromNbtList(list: NbtList): List<AugmentConsumer>{
            if (list.heldType != NbtElement.STRING_TYPE) return emptyList()
            val consumers: MutableList<AugmentConsumer> = mutableListOf()
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