package me.fzzyhmstrs.amethyst_core.entity

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
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
import java.util.function.Consumer

class TickEffect private constructor (private val consumer: Consumer<Entity>){

    fun tick(entity: Entity){
        consumer.accept(entity)
    }

    companion object{
        val REGISTRY : SimpleRegistry<TickEffect> = FabricRegistryBuilder.createSimple(RegistryKey.ofRegistry<TickEffect>(Identifier(AC.MOD_ID,"tick_effects"))).buildAndRegister()

        fun createAndRegisterConsumer(id: Identifier, consumer: Consumer<Entity>): TickEffect{
            return Registry.register(REGISTRY,id, TickEffect(consumer))
        }

        fun toNbtList(effects: Collection<TickEffect>): NbtList {
            val list = NbtList()
            for (consumer in effects){
                val id = REGISTRY.getId(consumer)
                if (id != null)
                    list.add(NbtString.of(id.toString()))
            }
            return list
        }

        fun fromNbtList(list: NbtList): List<TickEffect>{
            if (list.heldType != NbtElement.STRING_TYPE) return emptyList()
            val consumers: MutableList<TickEffect> = mutableListOf()
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
