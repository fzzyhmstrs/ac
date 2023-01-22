package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.fzzy_core.mana_util.ManaHelper
import me.fzzyhmstrs.fzzy_core.mana_util.ManaItem
import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterToolMaterial
import me.fzzyhmstrs.fzzy_core.item_util.CustomFlavorToolItem
import net.minecraft.entity.Entity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

/**
 * Base scepter item. Can be used for any number of implementations.
 *
 * This is a [ManaItem], so an implementation can interact with healing and damaging the item in a non-destructive way, and the durability bar is blue rather than the green-red gradient.
 *
 * The only inbuilt functionality at this level is the scepter will auto-register itself in the [ManaHelper] so it can heal damage over time. Besides that, anything is fair game. For overrides of [use] it is recommended to call the super to this class first so any initialization that is needed can happen before any extended functionality.
 */
abstract class AbstractScepterItem(material: ScepterToolMaterial, settings: Settings): CustomFlavorToolItem(material, settings), ManaItem {

    /**
     * the fallback ID is used when a scepter needs a starting state, like a spell, modifier, or whatever other implementation. For Augment Scepters, this is the base augment added by default (Magic Missile for Amethyst Imbuement)
     */
    abstract val fallbackId: Identifier
    private val tickerManaRepair: Long = material.healCooldown()

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)

        if (world.isClient()) return TypedActionResult.pass(stack)
        val nbt = stack.orCreateNbt
        if (needsInitialization(stack, nbt) && !world.isClient){
            initializeScepter(stack, nbt)
        }
        return super.use(world, user, hand)
    }

    override fun getRepairTime(): Int{
        return tickerManaRepair.toInt()
    }

    /**
     * by default Scepter Items are fireproof to prevent drastic loss in progress should one die in lava or similar.
     */
    override fun isFireproof(): Boolean {
        return true
    }

    override fun getItemBarColor(stack: ItemStack): Int {
        return MathHelper.hsvToRgb(0.66f,1.0f,1.0f)
    }

    override fun getUseAction(stack: ItemStack): UseAction {
        return UseAction.BLOCK
    }

    override fun onCraft(stack: ItemStack, world: World, player: PlayerEntity) {
        if (world.isClient) return
        val nbt = stack.orCreateNbt
        initializeScepter(stack, nbt)
    }

    /**
     * as needed implementations can add nbt needed for their basic funcitoning.
     *
     * Remember to call super.
     */
    open fun writeDefaultNbt(stack: ItemStack, scepterNbt: NbtCompound){
    }

    /**
     * called to initialize NBT or other stored information in memory. useful if there are things that need tracking like progress on something, or an active state. Called when the item is crafted and as needed when used.
     *
     * Remember to call super.
     */
    open fun initializeScepter(stack: ItemStack, scepterNbt: NbtCompound){
        writeDefaultNbt(stack, scepterNbt)
        ManaHelper.initializeManaItem(stack)
    }

    /**
     * function to define when a scepter needs post-crafting initialization. For states stored in memory, this will be at least once at the beginning of every game session (to repopulate a map or similar).
     */
    open fun needsInitialization(stack: ItemStack, scepterNbt: NbtCompound): Boolean{
        return ManaHelper.needsInitialization(stack) || Nbt.getItemStackId(scepterNbt) == -1L
    }

    /**
     * remember to call super.
     */
    //removes cooldown on the item if you switch item
    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (world.isClient) return
        val nbt = stack.orCreateNbt
        if (needsInitialization(stack, nbt) && !world.isClient){
            initializeScepter(stack, nbt)
        }
        //slowly heal damage over time
        if (ManaHelper.tickHeal(stack)){
            healDamage(1,stack)
        }
    }
}
