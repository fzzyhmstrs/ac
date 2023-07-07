package me.fzzyhmstrs.amethyst_core.entity

import me.fzzyhmstrs.amethyst_core.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import net.minecraft.nbt.NbtCompound

class TickEffect private constructor (private val consumer: Consumer<LivingEntity>){

    fun tick(entity: LivingEntity){
        consumer.accept(entity)
    }

    companion object{
        val REGISTRY : SimpleRegistry<AugmentConsumer> = FabricRegistryBuilder.createSimple(TickEffect::class.java, Identifier(
            AC.MOD_ID,"tick_effects")
        ).buildAndRegister()

        fun createAndRegisterConsumer(id: Identifier, consumer: Consumer<LivingEntity>): TickEffect{
            return Registry.register(REGISTRY,id, TickEffect(consumer))
        }

        fun toNbtList(effects: Collection<TickEffect>): NbtList{
            val list = NbtList()
            for (consumer in consumers){
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
