package me.fzzyhmstrs.amethyst_core.event

import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Identifier
import net.minecraft.world.World

/**
 * Event runs after a successful spell-cast
 */
fun interface AfterSpellEvent {

    companion object{
        val EVENT: Event<AfterSpellEvent> = EventFactory.createArrayBacked(AfterSpellEvent::class.java)
            { listeners ->
                AfterSpellEvent { world, user, stack,actions, spell, pairedAugments ->
                    for (listener in listeners){
                        listener.afterCast(world, user, stack, actions, spell, pairedAugments)
                    }
                }
            }
    }

    fun afterCast(world: World, user: LivingEntity, stack: ItemStack, actions: List<Identifier>, spell: ScepterAugment, pairedAugments: PairedAugments)
}
