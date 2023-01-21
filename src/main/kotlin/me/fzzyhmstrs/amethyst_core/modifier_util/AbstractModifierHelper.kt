package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.coding_util.AcText
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import kotlin.math.max

abstract class AbstractModifierHelper<T: AbstractModifier<T>> : ModifierInitializer{

    private val modifiers: MutableMap<Long ,MutableList<Identifier>> = mutableMapOf()
    private val activeModifiers: MutableMap<Long, AbstractModifier.CompiledModifiers<T>> = mutableMapOf()
    abstract val fallbackData: AbstractModifier.CompiledModifiers<T>

    abstract fun gatherActiveModifiers(stack: ItemStack)

    abstract fun getTranslationKeyFromIdentifier(id: Identifier): String
    
    abstract fun getDescTranslationKeyFromIdentifier(id: Identifier): String

    open fun addModifierTooltip(stack: ItemStack, tooltip: MutableList<Text>, context: TooltipContext){
        val nbt = stack.nbt ?: return
        if (!nbt.contains(NbtKeys.MODIFIERS.str())) return
        val ids = getModifiersFromNbt(stack)
        for (id in ids){
            val mod = ModifierRegistry.get(id)?:continue
            tooltip.add(AcText.translatable(mod.getTranslationKey()).append(AcText.literal(" - ")).append(AcText.translatable(mod.getDescTranslationKey())))
        }
    }

    fun getModifiersById(itemStackId: Long): List<Identifier>{
        return modifiers[itemStackId]?: listOf()
    }

    fun setModifiersById(itemStackId: Long, compiledData: AbstractModifier.CompiledModifiers<T>){
        activeModifiers[itemStackId] = compiledData
    }

    fun addModifier(modifier: Identifier, stack: ItemStack): Boolean{
        val nbt = stack.orCreateNbt
        return addModifier(modifier, stack, nbt)
    }

    protected fun addModifier(modifier: Identifier, stack: ItemStack, nbt: NbtCompound): Boolean{
        val id = Nbt.makeItemStackId(stack)
        if (!modifiers.containsKey(id)) {
            initializeModifiers(nbt, id)
        }
        val highestModifier = checkDescendant(modifier,stack)
        if (highestModifier != null){
            val mod = getModifierByType(modifier)
            return if (mod?.hasDescendant() == true){
                val highestDescendantPresent: Int = checkModifierLineage(mod, stack)
                if (highestDescendantPresent < 0){
                    false
                } else {
                    val lineage = mod.getModLineage()
                    val newDescendant = lineage[highestDescendantPresent]
                    val currentGeneration = lineage[max(highestDescendantPresent - 1,0)]
                    modifiers[id]?.add(newDescendant)
                    addModifierToNbt(newDescendant, nbt)
                    removeModifier(stack, currentGeneration, nbt)
                    gatherActiveModifiers(stack)
                    true
                }
            } else {
                false
            }
        }
        addModifierToNbt(modifier, nbt)
        modifiers[id]?.add(modifier)
        gatherActiveModifiers(stack)
        return true
    }

    protected fun checkDescendant(modifier: Identifier, stack: ItemStack): Identifier?{
        val id = Nbt.getItemStackId(stack)
        if (id == -1L) return null
        val mod = getModifierByType(modifier)
        val lineage = mod?.getModLineage() ?: return null
        var highestModifier: Identifier? = null
        lineage.forEach { identifier ->
            if (modifiers[id]?.contains(identifier) == true){
                highestModifier = identifier
            }
        }
        return highestModifier
    }

    protected fun removeModifier(stack: ItemStack, modifier: Identifier, nbt: NbtCompound){
        val id = Nbt.getItemStackId(nbt)
        modifiers[id]?.remove(modifier)
        gatherActiveModifiers(stack)
        removeModifierFromNbt(modifier,nbt)
    }

    fun addModifierToNbt(modifier: Identifier, stack: ItemStack){
        val nbt = stack.orCreateNbt
        addModifierToNbt(modifier, nbt)
    }

    fun addModifierToNbt(modifier: Identifier, nbt: NbtCompound){
        val newEl = NbtCompound()
        newEl.putString(NbtKeys.MODIFIER_ID.str(),modifier.toString())
        Nbt.addNbtToList(newEl, NbtKeys.MODIFIERS.str(),nbt)
    }

    protected fun removeModifierFromNbt(modifier: Identifier, nbt: NbtCompound){
        Nbt.removeNbtFromList(NbtKeys.MODIFIERS.str(),nbt) { nbtEl: NbtCompound ->
            if (nbtEl.contains(NbtKeys.MODIFIER_ID.str())){
                val chk = Identifier(nbtEl.getString(NbtKeys.MODIFIER_ID.str()))
                chk == modifier
            } else {
                false
            }
        }
    }

    override fun initializeModifiers(stack: ItemStack, nbt: NbtCompound, list: List<Identifier>){
        if (list.isNotEmpty()){
            if (!nbt.contains(NbtKeys.MOD_INIT.str() + stack.translationKey)){
                list.forEach{
                    addModifierToNbt(it,nbt)
                }
                nbt.putBoolean(NbtKeys.MOD_INIT.str() + stack.translationKey,true)
            }
        }
        if (nbt.contains(NbtKeys.MODIFIERS.str())){
            val id = Nbt.makeItemStackId(stack)
            initializeModifiers(nbt, id)
            gatherActiveModifiers(stack)
        }
    }

    protected fun initializeModifiers(nbt: NbtCompound, id: Long){
        val nbtList = nbt.getList(NbtKeys.MODIFIERS.str(),10)
        modifiers[id] = mutableListOf()
        for (el in nbtList){
            val compound = el as NbtCompound
            if (compound.contains(NbtKeys.MODIFIER_ID.str())){
                val modifier = compound.getString(NbtKeys.MODIFIER_ID.str())
                modifiers[id]?.add(Identifier(modifier))
            }
        }
    }

    fun getModifiers(stack: ItemStack): List<Identifier>{
        val nbt = stack.orCreateNbt
        val id = Nbt.getItemStackId(nbt)
        if (id == -1L) return listOf()
        if (!modifiers.containsKey(id)) {
            if (stack.nbt?.contains(NbtKeys.MODIFIERS.str()) == true) {
                initializeModifiers(nbt, id)
            }
        }
        return modifiers[id] ?: listOf()
    }

    fun getModifiersFromNbt(stack: ItemStack): List<Identifier>{
        val list: MutableList<Identifier> = mutableListOf()
        val nbt = stack.nbt?:return list
        if (nbt.contains(NbtKeys.MODIFIERS.str())){
            val nbtList = Nbt.readNbtList(nbt, NbtKeys.MODIFIERS.str())
            nbtList.forEach {
                val nbtCompound = it as NbtCompound
                if (nbtCompound.contains(NbtKeys.MODIFIER_ID.str())){
                    list.add(Identifier(Nbt.readStringNbt(NbtKeys.MODIFIER_ID.str(), nbtCompound)))
                }
            }
        }
        return list
    }

    fun getActiveModifiers(stack: ItemStack): AbstractModifier.CompiledModifiers<T> {
        val id = Nbt.getItemStackId(stack)
        /*if (id != -1L && !activeModifiers.containsKey(id)){
            initializeModifiers(stack, stack.orCreateNbt)
        }*/
        val compiledData = activeModifiers[id]
        return  compiledData ?: fallbackData
    }

    fun checkModifierLineage(modifier: Identifier, stack: ItemStack): Boolean{
        val mod = getModifierByType(modifier)
        return if (mod != null){
            checkModifierLineage(mod, stack) >= 0
        } else {
            false
        }
    }

    protected fun checkModifierLineage(mod: T, stack: ItemStack): Int{
        val id = Nbt.getItemStackId(stack)
        val lineage = mod.getModLineage()
        val highestOrderDescendant = lineage.size
        var highestDescendantPresent = 0
        lineage.forEachIndexed { index, identifier ->
            if (modifiers[id]?.contains(identifier) == true){
                highestDescendantPresent = index + 1
            }
        }
        return if(highestDescendantPresent < highestOrderDescendant){
            highestDescendantPresent
        } else {
            -1
        }
    }

    fun getNextInLineage(modifier: Identifier, stack: ItemStack): Identifier{
        val mod = getModifierByType(modifier)
        return if (mod != null){
            val lineage = mod.getModLineage()
            val nextInLineIndex = checkModifierLineage(mod, stack)
            if (nextInLineIndex == -1){
                modifier
            } else {
                lineage[nextInLineIndex]
            }
        } else {
            modifier
        }
    }

    fun getMaxInLineage(modifier: Identifier): Identifier{
        val mod = getModifierByType(modifier)
        return mod?.getModLineage()?.last() ?: return modifier
    }

    abstract fun getModifierByType(id: Identifier): T?

    inline fun <reified A : AbstractModifier<A>> gatherActiveAbstractModifiers(
        stack: ItemStack,
        objectToAffect: Identifier,
        compiler: AbstractModifier<A>.Compiler
    ): AbstractModifier.CompiledModifiers<A> {
        val id = Nbt.getItemStackId(stack)
        getModifiersById(id).forEach { identifier ->
            val modifier = ModifierRegistry.getByType<A>(identifier)
            if (modifier != null) {
                if (!modifier.hasObjectToAffect()) {
                    compiler.add(modifier)
                } else {
                    if (modifier.checkObjectsToAffect(objectToAffect)) {
                        compiler.add(modifier)
                    }
                }
            }
        }
        return compiler.compile()
    }

    companion object{

        val EMPTY = EmptyModifier()

        fun getEmptyHelper(): EmptyModifierHelper{
            return EmptyModifierHelper
        }

        object EmptyModifierHelper: AbstractModifierHelper<EmptyModifier>() {
            override val fallbackData: AbstractModifier.CompiledModifiers<EmptyModifier> = AbstractModifier.CompiledModifiers(listOf(),EMPTY)

            override fun gatherActiveModifiers(stack: ItemStack) {
            }

            override fun getTranslationKeyFromIdentifier(id: Identifier): String {
                return ""
            }

            override fun getDescTranslationKeyFromIdentifier(id: Identifier): String {
                return ""
            }

            override fun getModifierByType(id: Identifier): EmptyModifier? {
                return null
            }

        }

        class EmptyModifier:AbstractModifier<EmptyModifier>(ModifierDefaults.BLANK_ID){
            override fun plus(other: EmptyModifier): EmptyModifier {
                return this
            }

            override fun compiler(): Compiler {
                return Compiler(mutableListOf(), EmptyModifier())
            }

            override fun getModifierHelper(): AbstractModifierHelper<*> {
                return EquipmentModifierHelper
            }

        }
    }
}
