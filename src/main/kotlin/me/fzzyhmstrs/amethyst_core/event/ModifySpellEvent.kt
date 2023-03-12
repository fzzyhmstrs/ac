package me.fzzyhmstrs.amethyst_core.event

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import net.fabricmc.fabric.api.event.Event
import net.fabricmc.fabric.api.event.EventFactory
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.world.World

/**
 * Event allows for secondary effects before a spell-cast or cancellation of a spell.
 *
 * This event can be used to perform various side effects before spell-casting happens, such as
 * 1. Performing actions on the player, such as hurting them on attempting to cast a spell
 * 2. Incrementing a stat or other record, such as a Gem of Promise listening for spell-casts to make progress towards igniting
 * 3. Cast a completely different spell and tell the scepter that it still successfully cast one (without triggering reset)
 * 4. Cause spell casting to completely fail, unrelated to the spells normal mechanism for checking that.
 *
 * Event listeners for this event are expected to pass back an ActionResult.
 *
 * When passing back the ActionResult, the following results mean:
 *
 * `PASS` - Keep running listeners, I may have done something but don't want to affect the EVENT or the spell.
 *
 * `SUCCESS` - Same as PASS, does **not** cancel the event
 *
 * `CONSUME` - I've performed an alternate action instead of the normal spell casting, but still want the spell cooldown to trigger like it was cast
 *
 * `FAIL` - Spell casting should be canceled and cooldown reset.
 *
 * @param world World. The game world instance for use in modifications as necessary
 * @param user PlayerEntity. The spell caster
 * @param hand Hand. The players active hand.
 * @param modifiers [AbstractModifier.CompiledModifiers]. Compiled modifiers after mutation by the [ModifyModifiersEvent]. Mutation of modifiers should *not* happen inside this event.
 */
fun interface ModifySpellEvent {

    companion object{
        val EVENT: Event<ModifySpellEvent> = EventFactory.createArrayBacked(ModifySpellEvent::class.java)
            {listeners ->
                ModifySpellEvent {world,user,hand,modifiers ->
                    for (listener in listeners) {
                        val result = listener.modifySpell(world, user, hand, modifiers)
                        if (result== ActionResult.PASS || result == ActionResult.SUCCESS){
                            continue
                        } else {
                            return@ModifySpellEvent result
                        }
                    }
                    return@ModifySpellEvent ActionResult.PASS
                }
            }
    }


    fun modifySpell(world: World, user: LivingEntity, hand: Hand, modifiers: AbstractModifier.CompiledModifiers<AugmentModifier>): ActionResult

}