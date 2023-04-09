package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketChecker
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketUtil
import me.fzzyhmstrs.gear_core.interfaces.ModifierTracking
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import java.util.*
import kotlin.collections.ArrayList

object GcCompat {

    private val augmentMap: MutableMap<UUID, AbstractModifier.CompiledModifiers<AugmentModifier>> = mutableMapOf()

    fun registerAugmentModifierProcessor(){
        EquipmentModifierHelper.registerModifierProcessor {stack, entity -> processEquipmentAugmentModifiers(stack, entity)}
    }

    fun modifyCompiledAugmentModifiers(original: AbstractModifier.CompiledModifiers<AugmentModifier>, uuid: UUID): AbstractModifier.CompiledModifiers<AugmentModifier>{
        val augments = augmentMap[uuid]?:return original
        val list: ArrayList<AugmentModifier> = arrayListOf()
        list.addAll(augments.modifiers)
        list.addAll(original.modifiers)
        return AbstractModifier.CompiledModifiers(list,AugmentModifier().plus(augments.compiledData).plus(original.compiledData))
    }

    private fun processEquipmentAugmentModifiers(stack: ItemStack, entity: LivingEntity){
        val item = stack.item
        if (item !is ModifierTracking) return
        val uuid = entity.uuid
        val list: MutableList<Identifier> = mutableListOf()
        if (TrinketChecker.trinketsLoaded) {
            val stacks = TrinketUtil.getTrinketStacks(entity)
            for (stack1 in stacks) {
                val chk = stack1.item
                if (chk is ModifierTracking) {
                    list.addAll(chk.getModifiers(stack1, ModifierHelper.getType()))
                }
            }
        }
        for(armor in entity.armorItems) {
            val chk = armor.item
            if (chk is ModifierTracking){
                list.addAll(chk.getModifiers(armor, ModifierHelper.getType()))
            }
        }
        if (list.isNotEmpty()){
            val list2: MutableList<AugmentModifier> = mutableListOf()
            list.forEach {
                val chk = ModifierHelper.getModifierByType(it)
                if (chk != null) list2.add(chk)
            }
            val compiler = ModifierDefaults.BLANK_AUG_MOD.compiler()
            list2.forEach {
                compiler.add(it)
            }
            augmentMap[uuid] = compiler.compile()
        }
    }
}