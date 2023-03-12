package me.fzzyhmstrs.amethyst_core.event

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
                AfterSpellEvent { world, user, stack ->
                    for (listener in listeners){
                        listener.afterCast(world, user, stack)
                    }
                }
            }
    }

    fun afterCast(world: World, user: LivingEntity, stack: ItemStack)
}