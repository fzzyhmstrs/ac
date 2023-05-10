package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.event.ModifyModifiersEvent
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

    fun markDirty(user: LivingEntity){
        augmentMap.remove(user.uuid)
    }

    fun registerAugmentModifierProcessor(){
        ModifyModifiersEvent.EVENT.register{ _, user, _, modifiers ->
            val newMods = augmentMap[user.uuid]?:processEquipmentAugmentModifiers(user)
            return@register modifiers.combineWith(newMods, AugmentModifier())
        }
    }

    private fun processEquipmentAugmentModifiers(entity: LivingEntity): AbstractModifier.CompiledModifiers<AugmentModifier>{
        val list: MutableList<Identifier> = mutableListOf()
        if (TrinketChecker.trinketsLoaded) {
            val stacks = TrinketUtil.getTrinketStacks(entity)
            for (stack1 in stacks) {
                val chk = stack1.item
                if (chk is ModifierTracking) {
                    list.addAll(chk.getModifiers(stack1,ModifierHelper.getType()))
                }
            }
        }
        for(armor in entity.armorItems) {
            val chk = armor.item
            if (chk is ModifierTracking){
                list.addAll(chk.getModifiers(armor,ModifierHelper.getType()))
            }
        }
        val mainHand = entity.mainHandStack
        val chkMain = mainHand.item
        if (chkMain is ModifierTracking){
            list.addAll(chkMain.getModifiers(mainHand,ModifierHelper.getType()))
        }
        val offHand = entity.offHandStack
        val chkOff = offHand.item
        if (chkOff is ModifierTracking){
            list.addAll(chkOff.getModifiers(offHand,ModifierHelper.getType()))
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
            val mods = compiler.compile()
            augmentMap[entity.uuid] = mods
            return mods
        }
        return ModifierDefaults.BLANK_COMPILED_DATA
    }
}
