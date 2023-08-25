package me.fzzyhmstrs.amethyst_core.event

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

/**
 * Event runs after a successful spell-cast
 */
fun interface ModifyAugmentEffectsEvent {

    companion object{
        val EVENT: Event<ModifyAugmentEffectsEvent> = EventFactory.createArrayBacked(ModifyAugmentEffectsEvent::class.java)
            { listeners ->
                ModifyAugmentEffectsEvent { world, user, stack, effects, spell ->
                    for (listener in listeners){
                        listener.modifyEffects(world, user, stack, effects, spell)
                    }
                }
            }
    }

    fun modifyEffects(world: World, user: LivingEntity, stack: ItemStack, effects: AugmentEffect, spell: ScepterAugment)
}