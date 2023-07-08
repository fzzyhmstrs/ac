package me.fzzyhmstrs.amethyst_core.entity

import me.fzzyhmstrs.amethyst_core.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.nbt.NbtCompound
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * interface for meshing an entity (or any object really) with the Modifier system. The interface provides a base [AugmentEffect] instance for storing and passing effect attributes. See that doc for details on what is stored.
 *
 * The [passEffects] method is used to pass in modifications to the default AugmentEffect from the calling object. In default implementations, the Scepter Augments apply their effect modifiers to the Missile Entities they summon before spawning them.
 *
 * passEffects defines how you want your entity to be affected by any modifications. For example, if you want the area-of-effect of a status effect cloud your entity creates to be affected, add a setRange or addRange call to passEffects so any calling object that affects range will affect this entity. Then, in the AOE effect implementation, define the range/size of the cloud with the [entityEffects] range instance rather than with a static number.
 */

interface ModifiableEffectEntity<T: Entity> {

    var entityEffects: AugmentEffect
    var level: Int
    var spells: PairedAugments
    val tickEffects: ConcurrentLinkedQueue<TickEffect>
    var processContext: ProcessContext

    fun passEffects(spells: PairedAugments, ae: AugmentEffect, level: Int){
        entityEffects = AugmentEffect().plus(ae)
        this.level = level
        this.spells = spells
    }

    fun writeModifiableNbt(nbtCompound: NbtCompound){
        val modifiableNbt = NbtCompound()
        modifiableNbt.put("entityEffects",entityEffects.writeNbt())
        modifiableNbt.putInt("level",level)
        modifiableNbt.put("spells", AugmentHelper.writePairedAugmentsToNbt(spells))
        modifiableNbt.put("tickEffects",TickEffect.toNbtList(tickEffects))
        modifiableNbt.put("processContext", processContext.writeNbt())
        nbtCompound.put("modifiable_effects",modifiableNbt)
    }

    fun readModifiableNbt(nbtCompound: NbtCompound){
        if (!nbtCompound.contains("modifiable_effects")) return
        val modifiableNbt = nbtCompound.getCompound("modifiable_effects")
        entityEffects = AugmentEffect.readNbt(modifiableNbt.getCompound("entityEffects"))
        level = modifiableNbt.getInt("level")
        spells = AugmentHelper.getOrCreatePairedAugmentsFromNbt(modifiableNbt.getCompound("spells"))
        tickEffects.addAll(TickEffect.fromNbtList(modifiableNbt.getList("tickEffects",8)))
        processContext = ProcessContext.readNbt(modifiableNbt.getCompound("processContext"))
    }

    fun tickingEntity(): T
    
    fun tickTickEffects(){
        for (effect in tickEffects){
            effect.tick(tickingEntity())
        }
    }

    fun addTickEffect(effect: TickEffect){
        tickEffects.add(effect)
    }
}
