package me.fzzyhmstrs.amethyst_core.event

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.world.World

/**
 * Event for modifying the compiled scepter modifiers before a spell is cast
 *
 * This event doesn't have a "fail" state. All listeners will be run on all invocations. Listeners are provided the compiled data in sequence, with each listener passing it's mutations to the next listener for further modification if necessary.
 *
 * Listeners are expected to pass a compiled modifier back. Either:
 * 1. Pass the originally provided modifiers back
 * 2. Build a new set of modifiers with the desired mutations and pass that.
 *
 * @param world World. The game world instance for use in modifications as necessary
 * @param user PlayerEntity. The spell caster
 * @param stack ItemStack. The scepter casting the spell.
 * @param modifiers [AbstractModifier.CompiledModifiers]. The "upstream" compiled modifier for passing back or modification.
 */
fun interface ModifyModifiersEvent {

    companion object{
        val EVENT: Event<ModifyModifiersEvent> = EventFactory.createArrayBacked(ModifyModifiersEvent::class.java)
        {listeners ->
            ModifyModifiersEvent {world,user,stack,modifiers ->
                var checkedModifiers = modifiers
                for (listener in listeners) {
                    checkedModifiers = listener.modifyModifiers(world, user, stack, checkedModifiers)
                }
                return@ModifyModifiersEvent checkedModifiers
            }
        }
    }

    fun modifyModifiers(world: World, user: LivingEntity, stack: ItemStack, modifiers: AbstractModifier.CompiledModifiers<AugmentModifier>): AbstractModifier.CompiledModifiers<AugmentModifier>
}