package me.fzzyhmstrs.amethyst_core.event

import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

/**
 * Event runs after a successful spell-cast
 */
fun interface AfterSpellEvent {

    companion object{
        val EVENT: Event<AfterSpellEvent> = EventFactory.createArrayBacked(AfterSpellEvent::class.java)
            { listeners ->
                AfterSpellEvent { world, user, stack, spell ->
                    for (listener in listeners){
                        listener.afterCast(world, user, stack, spell)
                    }
                }
            }
    }

    fun afterCast(world: World, user: LivingEntity, stack: ItemStack, spell: ScepterAugment)
}