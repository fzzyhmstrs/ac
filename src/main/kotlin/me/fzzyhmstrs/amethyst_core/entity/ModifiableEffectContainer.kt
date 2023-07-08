package me.fzzyhmstrs.amethyst_core.entity

import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue

class ModifiableEffectContainer() {

    private val effects: ConcurrentHashMap<Identifier, ConcurrentLinkedQueue<ModifiableEffect>> = ConcurrentHashMap()

    fun run(type: Identifier, entity: Entity, context: ProcessContext?){
        val queue = effects.computeIfAbsent(type) {ConcurrentLinkedQueue()}
        for (effect in queue){
            effect.run(entity, context)
        }
    }

    fun add(type: Identifier, effect: ModifiableEffect){
        val queue = effects.computeIfAbsent(type) {ConcurrentLinkedQueue()}
        queue.add(effect)
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
            val effectList = ModifiableEffect.fromNbtList(nbtCompound.getList(key, 8))
            val queue = effects.computeIfAbsent(id) {ConcurrentLinkedQueue()}
            queue.addAll(effectList)
        }
    }

}