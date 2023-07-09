package me.fzzyhmstrs.amethyst_core.entity

import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class ModifiableEffectContainer() {

    private val effects: ConcurrentHashMap<Identifier, ConcurrentLinkedQueue<ModifiableEffectInstance>> = ConcurrentHashMap()

    fun run(type: Identifier, entity: Entity, owner: Entity?, context: ProcessContext){
        val time = entity.world.time
        val queue = effects.computeIfAbsent(type) {ConcurrentLinkedQueue()}
        val iterator = queue.iterator()
        while (iterator.hasNext()){
            val effectInstance = iterator.next()
            if (effectInstance.isExpired(time)){
                iterator.remove()
            } else {
                effectInstance.run(entity, owner, context)
            }
        }
    }

    fun add(type: Identifier, effect: ModifiableEffect){
        addTemporary(type,effect,-1)
    }

    fun addTemporary(type: Identifier, effect: ModifiableEffect, lifespan: Int){
        val queue = effects.computeIfAbsent(type) {ConcurrentLinkedQueue()}
        queue.add(ModifiableEffectInstance(effect,lifespan))
    }

    fun writeNbt(): NbtCompound{
        val nbt = NbtCompound()
        for (entry in effects){
            val key = entry.key.toString()
            nbt.put(key,ModifiableEffect.toNbtList(entry.value))
        }
        return nbt
    }

    fun readNbt(nbtCompound: NbtCompound){
        for (key in nbtCompound.keys) {
            val id = Identifier(key)
            val effectList = ModifiableEffect.fromNbtList(nbtCompound.getList(key, 10))
            val queue = effects.computeIfAbsent(id) {ConcurrentLinkedQueue()}
            queue.addAll(effectList)
        }
    }

}