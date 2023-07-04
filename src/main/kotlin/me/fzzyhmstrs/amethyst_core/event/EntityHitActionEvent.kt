package me.fzzyhmstrs.amethyst_core.event

import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

/**
 * Event runs after a successful spell-cast
 */
fun interface EntityHitActionEvent {

    companion object{
        val EVENT: Event<EntityHitActionEvent> = EventFactory.createArrayBacked(EntityHitActionEvent::class.java)
            { listeners ->
                EntityHitActionEvent { world, user, actions, hitResult ->
                    for (listener in listeners){
                        listener.onAction(world, user, actions, *hitResult)
                    }
                }
            }
    }

    fun onAction(world: World, user: LivingEntity, actions: List<Identifier>, vararg hitResult: EntityHitResult)
}
