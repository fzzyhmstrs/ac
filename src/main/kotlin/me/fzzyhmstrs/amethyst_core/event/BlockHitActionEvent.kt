package me.fzzyhmstrs.amethyst_core.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.world.World

/**
 * Event runs after a successful spell-cast
 */
fun interface BlockHitActionEvent {

    companion object{
        val EVENT: Event<BlockHitActionEvent> = EventFactory.createArrayBacked(BlockHitActionEvent::class.java)
            { listeners ->
                BlockHitActionEvent { world, user, actions, hitResult ->
                    for (listener in listeners){
                        listener.onAction(world, user, actions, *hitResult)
                    }
                }
            }
    }

    fun onAction(world: World, user: LivingEntity, actions: List<Identifier>, vararg hitResult: BlockHitResult)
}
