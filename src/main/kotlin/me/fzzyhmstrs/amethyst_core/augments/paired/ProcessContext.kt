package me.fzzyhmstrs.amethyst_core.augments.paired

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.entity.TickEffect
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.entity.Entity
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.DefaultedRegistry
import net.minecraft.registry.Registry
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.SimpleRegistry
import net.minecraft.util.Identifier
import java.util.function.Consumer

class ProcessContext(private val type: Identifier, private val data: NbtCompound) {

    constructor(type: Identifier): this(type, NbtCompound())

    fun getNbt(): NbtCompound{
        return data
    }
    fun getType(): Identifier{
        return type
    }

    fun writeNbt(): NbtCompound{
        val nbtCompound = NbtCompound()
        nbtCompound.putString("type", type.toString())
        nbtCompound.put("data", data.copy())
        return nbtCompound
    }

    companion object {

        val EMPTY_ID = Identifier(AC.MOD_ID,"empty_context")
        val EMPTY: ProcessContext
            get() = ProcessContext(EMPTY_ID)

        val FROM_ENTITY_ID = Identifier(AC.MOD_ID,"from_entity_context")
        val FROM_ENTITY: ProcessContext
            get() = ProcessContext(FROM_ENTITY_ID)

        fun readNbt(nbtCompound: NbtCompound): ProcessContext{
            val type = Identifier(nbtCompound.getString("type"))
            val data = nbtCompound.getCompound("data")
            return ProcessContext(type,data)
        }

    }

}