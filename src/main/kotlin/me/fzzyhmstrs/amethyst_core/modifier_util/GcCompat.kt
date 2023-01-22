package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.interfaces.AugmentModifying
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketChecker
import me.fzzyhmstrs.fzzy_core.trinket_util.TrinketUtil
import me.fzzyhmstrs.amethyst_core.interfaces.AugmentTracking
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifierHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import java.util.*

object GcCompat {

    private val augmentMap: MutableMap<UUID, AbstractModifier.CompiledModifiers<AugmentModifier>> = mutableMapOf()

    fun returnAugmentModifiers(stack: ItemStack): List<AugmentModifier>{
        val modifiers: AbstractModifier.CompiledModifiers<EquipmentModifier> = EquipmentModifierHelper.getActiveModifiers(stack)
        return (modifiers.compiledData as AugmentModifying<EquipmentModifier>).augmentModifiers
    }

    fun modifyCompiledAugmentModifiers(original: AbstractModifier.CompiledModifiers<AugmentModifier>, uuid: UUID): AbstractModifier.CompiledModifiers<AugmentModifier>{
        val augments = augmentMap[uuid]?:return original
        val list: MutableList<AugmentModifier> = mutableListOf()
        list.addAll(augments.modifiers)
        list.addAll(original.modifiers)
        return AbstractModifier.CompiledModifiers(list,AugmentModifier().plus(augments.compiledData).plus(original.compiledData))
    }

    fun processEquipmentAugmentModifiers(stack: ItemStack, entity: LivingEntity){
        val item = stack.item
        if (item !is AugmentTracking) return
        val uuid = entity.uuid
        val list: MutableList<AugmentModifier> = mutableListOf()
        if (TrinketChecker.trinketsLoaded) {
            val stacks = TrinketUtil.getTrinketStacks(entity)
            for (stack1 in stacks) {
                val chk = stack1.item
                if (chk is AugmentTracking) {
                    list.addAll(chk.getModifiers(stack1))
                }
            }
        }
        for(armor in entity.armorItems) {
            val chk = armor.item
            if (chk is AugmentTracking){
                list.addAll(chk.getModifiers(armor))
            }
        }
        val mainhand = entity.getEquippedStack(EquipmentSlot.MAINHAND)
        val chk2 = mainhand.item
        if (chk2 is AugmentTracking){
            list.addAll(chk2.getModifiers(mainhand))
        }
        val offhand = entity.getEquippedStack(EquipmentSlot.OFFHAND)
        val chk3 = offhand.item
        if (chk3 is AugmentTracking){
            list.addAll(chk3.getModifiers(offhand))
        }
        if (list.isNotEmpty()){
            val compiler = ModifierDefaults.BLANK_AUG_MOD.compiler()
            list.forEach {
                compiler.add(it)
            }
            augmentMap[uuid] = compiler.compile()
        }
    }
}