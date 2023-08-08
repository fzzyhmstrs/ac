package me.fzzyhmstrs.amethyst_core.entity

import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

class ModifiableEffectInstance(val effect: ModifiableEffect?, private val lifespan: Int = -1) {

    private var firstRunTime: Long = 0L
    private val isTemporary = lifespan != -1

    fun isExpired(time: Long): Boolean{
        if (lifespan == -1) return false
        if (firstRunTime == 0L){
            firstRunTime = time
            return false
        }
        return (time - firstRunTime).toInt() >= lifespan
    }
    fun run(entity: Entity, attackerOrOwner: Entity?, context: ProcessContext){
        effect?.run(entity,attackerOrOwner, context)
    }

    fun toNbt(): NbtCompound{
        val nbt = NbtCompound()
        val id = ModifiableEffect.REGISTRY.getId(effect) ?: return nbt
        nbt.putString("effect",id.toString())
        nbt.putInt("lifespan", lifespan)
        nbt.putLong("firstRunTime", firstRunTime)
        return nbt
    }

    companion object{
        fun fromNbt(nbt: NbtCompound): ModifiableEffectInstance{
            val id = Identifier(nbt.getString("effect"))
            val effect = ModifiableEffect.REGISTRY.get(id)
            val lifespan = nbt.getInt("lifespan")
            val firstRunTime = nbt.getLong("firstRunTime")
            val instance = ModifiableEffectInstance(effect,lifespan)
            instance.firstRunTime = firstRunTime
            return  instance
        }

    }

}