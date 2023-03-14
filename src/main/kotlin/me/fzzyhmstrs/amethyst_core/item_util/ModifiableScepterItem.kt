package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierHelper
import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierInitializer
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterToolMaterial
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType
import net.minecraft.client.item.TooltipContext
import net.minecraft.nbt.NbtCompound
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier

/**
 * An abstract scepter integrated into the [Modifier][AbstractModifier] System.
 *
 * Does not provide any default functionality beyond gathering and initializing Modifiers for whatever use they are needed.
 */

abstract class ModifiableScepterItem(material: ScepterToolMaterial, settings: Settings): AbstractScepterItem(material, settings), Modifiable{

    val defaultModifiers: MutableList<Identifier> = mutableListOf()

    fun withModifiers(defaultMods: List<AugmentModifier> = listOf()): ModifiableScepterItem{
        defaultMods.forEach {
            defaultModifiers.add(it.modifierId)
        }
        return this
    }

    override fun canBeModifiedBy(type: ModifierHelperType): Boolean {
        return (type == ModifierRegistry.MODIFIER_TYPE)
    }

    override fun defaultModifiers(type: ModifierHelperType): MutableList<Identifier> {
        return if (canBeModifiedBy(type)) defaultModifiers else mutableListOf()
    }

    override fun addModifierTooltip(stack: ItemStack, tooltip: MutableList<Text>, context: TooltipContext) {
        ModifierHelper.addModifierTooltip(stack, tooltip, context)
    }

    override fun initializeScepter(stack: ItemStack, scepterNbt: NbtCompound) {
        super.initializeScepter(stack, scepterNbt)
        ModifierHelper.gatherActiveModifiers(stack)
    }
}