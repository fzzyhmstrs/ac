package me.fzzyhmstrs.amethyst_core.augments.paired

import me.fzzyhmstrs.amethyst_core.AC
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier

class ProcessContext(private val data: NbtCompound) {

    constructor(): this(NbtCompound())

    fun copy(): ProcessContext{
        return ProcessContext(data.copy())
    }

    fun<T> get(dataKey: Data<T>): T{
        return dataKey.get(data)
    }

    fun<T> set(dataKey: Data<T>, value: T): ProcessContext{
        dataKey.set(data, value)
        return this
    }

    fun double(): ProcessContext{
        set(DOUBLE,true)
        return this
    }

    fun isDouble(): Boolean{
        return get(DOUBLE)
    }

    fun beforeRemoval(): ProcessContext{
        set(BEFORE_REMOVAL,true)
        return this
    }

    fun isBeforeRemoval(): Boolean{
        return get(BEFORE_REMOVAL)
    }

    fun writeNbt(): NbtCompound{
        val nbtCompound = NbtCompound()
        nbtCompound.put("data", data.copy())
        return nbtCompound
    }

    companion object {

        val ENTITY_ID = object : Data<Int>("entity_id_id",IntDataType){}
        val ENTITY_TYPE = object : Data<Identifier>("entity_type_id",IdentifierDataType){}
        val FROM_ENTITY = object : Data<Boolean>("from_entity",BooleanDataType){}
        val SPELL = object : Data<Identifier>("spell_id",IdentifierDataType){}
        val DOUBLE = object : Data<Boolean>("double_spell",BooleanDataType){}
        val BEFORE_REMOVAL = object : Data<Boolean>("before_removal",BooleanDataType){}
        val COOLDOWN = object : Data<Int>("custom_cooldown",IntDataType){}

        val EMPTY_CONTEXT: ProcessContext
            get() = ProcessContext()
        val FROM_ENTITY_CONTEXT: ProcessContext
            get() = ProcessContext().set(FROM_ENTITY, true)

        fun readNbt(nbtCompound: NbtCompound): ProcessContext{
            val data = nbtCompound.getCompound("data")
            return ProcessContext(data)
        }

    }

    open class Data<T> (private val key: String,private val dataType: DataType<T>){
        fun get(nbtCompound: NbtCompound): T{
            return dataType.fromNbt(key, nbtCompound)
        }

        fun set(nbtCompound: NbtCompound, value: T){
            dataType.toNbt(key,nbtCompound, value)
        }
    }

    interface DataType<T>{
        fun fromNbt(key: String,nbtCompound: NbtCompound): T
        fun toNbt(key: String,nbtCompound: NbtCompound,data: T)
    }

    object IntDataType: DataType<Int> {
        override fun fromNbt(key: String, nbtCompound: NbtCompound): Int {
            return nbtCompound.getInt(key)
        }
        override fun toNbt(key: String, nbtCompound: NbtCompound, data: Int) {
            nbtCompound.putInt(key, data)
        }
    }
    object BooleanDataType: DataType<Boolean> {
        override fun fromNbt(key: String, nbtCompound: NbtCompound): Boolean {
            return nbtCompound.getBoolean(key)
        }
        override fun toNbt(key: String, nbtCompound: NbtCompound, data: Boolean) {
            nbtCompound.putBoolean(key, data)
        }
    }
    object StringDataType: DataType<String> {
        override fun fromNbt(key: String, nbtCompound: NbtCompound): String {
            return nbtCompound.getString(key)
        }
        override fun toNbt(key: String, nbtCompound: NbtCompound, data: String) {
            nbtCompound.putString(key, data)
        }
    }
    object IdentifierDataType: DataType<Identifier> {
        override fun fromNbt(key: String, nbtCompound: NbtCompound): Identifier {
            val str = nbtCompound.getString(key)
            return Identifier(str)
        }
        override fun toNbt(key: String, nbtCompound: NbtCompound, data: Identifier) {
            nbtCompound.putString(key, data.toString())
        }
    }
    object FloatDataType: DataType<Float> {
        override fun fromNbt(key: String, nbtCompound: NbtCompound): Float {
            return nbtCompound.getFloat(key)
        }
        override fun toNbt(key: String, nbtCompound: NbtCompound, data: Float) {
            nbtCompound.putFloat(key, data)
        }
    }

    object DoubleDataType: DataType<Double> {
        override fun fromNbt(key: String, nbtCompound: NbtCompound): Double {
            return nbtCompound.getDouble(key)
        }
        override fun toNbt(key: String, nbtCompound: NbtCompound, data: Double) {
            nbtCompound.putDouble(key, data)
        }
    }

}