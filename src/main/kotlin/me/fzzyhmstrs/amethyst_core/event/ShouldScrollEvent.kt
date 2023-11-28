package me.fzzyhmstrs.amethyst_core.event

import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

/**
 * Event runs after a successful spell-cast
 */
fun interface ShouldScrollEvent {

    companion object{
        val EVENT: Event<ShouldScrollEvent> = EventFactory.createArrayBacked(ShouldScrollEvent::class.java)
            { listeners ->
                ShouldScrollEvent { stack, playerEntity ->
                    for (listener in listeners){
                        if (!listener.shouldScroll(stack, playerEntity)) return@ShouldScrollEvent false
                    }
                    true
                }
            }
    }

    fun shouldScroll(stack: ItemStack, playerEntity: PlayerEntity): Boolean
}