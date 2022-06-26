package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import net.minecraft.item.ItemStack
import net.minecraft.text.LiteralText
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.function.Predicate

open class AbstractModifier(val modifierId: Identifier){

    private var descendant: Identifier = ModifierDefaults.BLANK_ID
    private val lineage: List<Identifier> by lazy { generateLineage() }
    private var objectsToAffect: Predicate<Identifier>? = null

    private var hasDesc: Boolean = false
    private var hasObjectToAffect: Boolean = false

    fun hasDescendant(): Boolean{
        return hasDesc
    }
    fun addDescendant(modifier: AbstractModifier){
        val id = modifier.modifierId
        descendant = id
        hasDesc = true
    }
    fun getModLineage(): List<Identifier>{
        return lineage
    }
    private fun generateLineage(): List<Identifier>{
        val nextInLineage = ModifierRegistry.get(descendant)
        val lineage: MutableList<Identifier> = mutableListOf(this.modifierId)
        lineage.addAll(nextInLineage?.getModLineage() ?: listOf())
        return lineage
    }
    open fun hasObjectToAffect(): Boolean{
        return hasObjectToAffect
    }
    open fun addObjectToAffect(predicate: Predicate<Identifier>){
        objectsToAffect = predicate
        hasObjectToAffect = true
    }
    open fun checkObjectsToAffect(id: Identifier): Boolean{
        return objectsToAffect?.test(id) ?: return false
    }
    open fun getName(): Text {
        return LiteralText("$modifierId")
    }
    open fun isAcceptableItem(stack: ItemStack): Boolean{
        acceptableItemStacks().forEach {
            if (stack.isOf(it.item)){
                return true
            }
        }
        return false
    }
    open fun acceptableItemStacks(): MutableList<ItemStack>{
        return mutableListOf()
    }
}