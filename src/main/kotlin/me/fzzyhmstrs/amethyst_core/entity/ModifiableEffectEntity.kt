package me.fzzyhmstrs.amethyst_core.entity

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Identifier
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * interface for meshing an entity (or any object really) with the Modifier system. The interface provides a base [AugmentEffect] instance for storing and passing effect attributes. See that doc for details on what is stored.
 *
 * The [passEffects] method is used to pass in modifications to the default AugmentEffect from the calling object. In default implementations, the Scepter Augments apply their effect modifiers to the Missile Entities they summon before spawning them.
 *
 * passEffects defines how you want your entity to be affected by any modifications. For example, if you want the area-of-effect of a status effect cloud your entity creates to be affected, add a setRange or addRange call to passEffects so any calling object that affects range will affect this entity. Then, in the AOE effect implementation, define the range/size of the cloud with the [entityEffects] range instance rather than with a static number.
 */

interface ModifiableEffectEntity{

    var entityEffects: AugmentEffect
    var level: Int
    var spells: PairedAugments
    var modifiableEffects: ModifiableEffectContainer
    var processContext: ProcessContext

    fun passEffects(spells: PairedAugments, ae: AugmentEffect, level: Int){
        entityEffects = AugmentEffect().plus(ae)
        this.level = level
        this.spells = spells
    }

    fun passContext(context: ProcessContext){
        this.processContext = context
    }

    fun writeModifiableNbt(nbtCompound: NbtCompound){
        val modifiableNbt = NbtCompound()
        modifiableNbt.put("entityEffects",entityEffects.writeNbt())
        modifiableNbt.putInt("level",level)
        modifiableNbt.put("spells", AugmentHelper.writePairedAugmentsToNbt(spells))
        modifiableNbt.put("modifiableEffects",modifiableEffects.writeNbt())
        modifiableNbt.put("processContext", processContext.writeNbt())
        nbtCompound.put("modifiable_effects",modifiableNbt)
    }

    fun readModifiableNbt(nbtCompound: NbtCompound){
        if (!nbtCompound.contains("modifiable_effects")) return
        val modifiableNbt = nbtCompound.getCompound("modifiable_effects")
        entityEffects = AugmentEffect.readNbt(modifiableNbt.getCompound("entityEffects"))
        level = modifiableNbt.getInt("level").takeIf { it > 0 } ?: 1
        spells = AugmentHelper.getOrCreatePairedAugmentsFromNbt(modifiableNbt.getCompound("spells"))
        modifiableEffects = ModifiableEffectContainer()
        modifiableEffects.readNbt(modifiableNbt.getCompound("modifiableEffects"))
        processContext = ProcessContext.readNbt(modifiableNbt.getCompound("processContext"))
    }
    
    fun tickTickEffects(entity: Entity, owner: Entity?, context: ProcessContext = processContext){
        modifiableEffects.run(TICK,entity,owner,context)
    }

    fun runEffect(type: Identifier, entity: Entity,owner: Entity?, context: ProcessContext = processContext){
        modifiableEffects.run(type,entity,owner,context)
    }

    fun addEffect(type: Identifier, effect: ModifiableEffect){
        modifiableEffects.add(type, effect)
    }
    fun addTemporaryEffect(type: Identifier, effect: ModifiableEffect, lifespan: Int){
        modifiableEffects.addTemporary(type, effect, lifespan)
    }
    companion object{

        val TICK = Identifier(AC.MOD_ID, "tick_effects")
        val DAMAGE = Identifier(AC.MOD_ID, "damage_effects")
        val ON_DAMAGED = Identifier(AC.MOD_ID, "on_damaged_effects")
        val KILL = Identifier(AC.MOD_ID, "kill_effects")
        val ON_REMOVED = Identifier(AC.MOD_ID, "on_removed_effects")
    }
}
