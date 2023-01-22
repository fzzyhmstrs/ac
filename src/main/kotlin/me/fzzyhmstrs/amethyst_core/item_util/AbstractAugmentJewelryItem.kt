package me.fzzyhmstrs.amethyst_core.item_util

import com.google.common.collect.Multimap
import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.TrinketItem
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.item_util.interfaces.Flavorful
import me.fzzyhmstrs.fzzy_core.registry.EventRegistry
import me.fzzyhmstrs.fzzy_core.trinket_util.AugmentTasks
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.damage.DamageSource
import net.minecraft.item.ItemStack
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import java.util.*

/**
 * used with the Trinkets mod to create jewelry that interacts with [Equipment Augments][me.fzzyhmstrs.fzzy_core.trinket_util.base_augments.BaseAugment].
 *
 * An item built using this class will be "modular". It will not be limited to one pre-defined effect, rather it can be enchanted/imbued with a variety of Augment effects to provide a custom trinket to the player.
 *
 * the [AugmentTasks] interface is called in various Trinkets methods to apply Augment effects based on various actions.
 *
 * Notably absent from this default item is an implementation for activated abilities (abilities that might be turned on/off with use)
 */
open class AbstractAugmentJewelryItem(settings: Settings): TrinketItem(settings), AugmentTasks, Flavorful<AbstractAugmentJewelryItem> {

    override var glint: Boolean = false
    override var flavor: String = ""
    override var flavorDesc: String = ""
        
    private val flavorText: MutableText by lazy{
        makeFlavorText()
    }
    
    private val flavorTextDesc: MutableText by lazy{
        makeFlavorTextDesc()
    }
    
    private fun makeFlavorText(): MutableText{
        val id = Registry.ITEM.getId(this)
        return AcText.translatable("item.${id.namespace}.${id.path}.flavor").formatted(Formatting.WHITE, Formatting.ITALIC)
    }
    
    private fun makeFlavorTextDesc(): MutableText{
        val id = Registry.ITEM.getId(this)
        return AcText.translatable("item.${id.namespace}.${id.path}.flavor.desc").formatted(Formatting.WHITE)
    }

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
        addFlavorText(tooltip, context)
    }

    override fun getFlavorItem(): AbstractAugmentJewelryItem {
        return this
    }
    
    override fun flavorText(): MutableText{
        return flavorText
    }
    override fun flavorDescText(): MutableText{
        return flavorTextDesc
    }

    override fun getModifiers(
        stack: ItemStack,
        slot: SlotReference,
        entity: LivingEntity,
        uuid: UUID
    ): Multimap<EntityAttribute, EntityAttributeModifier> {
        val modifiers = super.getModifiers(stack, slot, entity, uuid)
        modifierEnchantmentTasks(stack,entity.world,entity, modifiers)
        return modifiers
    }

    override fun onEquip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        super.onEquip(stack, slot, entity)
        if (entity.world.isClient()) return
        equipEnchantmentTasks(stack,entity.world,entity)
    }

    override fun onUnequip(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        super.onUnequip(stack, slot, entity)
        if(entity.world.isClient()) return
        unequipEnchantmentTasks(stack,entity.world,entity)
    }

    override fun tick(stack: ItemStack, slot: SlotReference, entity: LivingEntity) {
        if(entity.world.isClient()) return
        if (EventRegistry.ticker_30.isReady()){
            intermittentTick(stack, entity)
        }
    }

    open fun intermittentTick(stack: ItemStack, entity: LivingEntity){
        passiveEnchantmentTasks(stack,entity.world,entity)
    }

/*    override fun onWearerDamaged(stack: ItemStack, wearer: LivingEntity, attacker: LivingEntity?, source: DamageSource, amount: Float): Float{
        return 0f
    }

    override fun postWearerHit(stack: ItemStack, wearer: LivingEntity, target: LivingEntity){
    }

    override fun onWearerKilledOther(stack: ItemStack, wearer: LivingEntity, victim: LivingEntity, world: ServerWorld) {
    }*/

}
