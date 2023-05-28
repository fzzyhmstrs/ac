package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.fzzy_core.nbt_util.NbtKeys
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier

interface ScepterLike{

    /**
     * the fallback ID is used when a scepter needs a starting state, like a spell, modifier, or whatever other implementation. For Augment Scepters, this is the base augment added by default (Magic Missile for Amethyst Imbuement)
     */
    val fallbackId: Identifier
    
    /**
     * Defines the spell power level of the scepter, generally 1, 2, or 3 (low, medium, high), but can be higher if higher tier spells are implemented.
     */
  
    fun getTier(): Int
  
    /**
     * as needed implementations can add nbt needed for their basic funcitoning.
     *
     * Remember to call super.
     */
    fun writeDefaultNbt(stack: ItemStack, scepterNbt: NbtCompound){
    }

    /**
     * called to initialize NBT or other stored information in memory. useful if there are things that need tracking like progress on something, or an active state. Called when the item is crafted and as needed when used.
     *
     * Remember to call super.
     */
    fun initializeScepter(stack: ItemStack, scepterNbt: NbtCompound){
        writeDefaultNbt(stack, scepterNbt)
    }

    /**
     * function to define when a scepter needs post-crafting initialization. For states stored in memory, this will be at least once at the beginning of every game session (to repopulate a map or similar).
     */
    fun needsInitialization(stack: ItemStack, scepterNbt: NbtCompound): Boolean


    fun addDefaultEnchantments(stack: ItemStack, scepterNbt: NbtCompound)

    fun getActiveEnchant(stack: ItemStack): String{
        val nbt: NbtCompound = stack.orCreateNbt
        return if (nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())){
            nbt.getString(NbtKeys.ACTIVE_ENCHANT.str())
        } else {
            initializeScepter(stack,nbt)
            nbt.getString(NbtKeys.ACTIVE_ENCHANT.str())
        }
    }
}
