package me.fzzyhmstrs.amethyst_core.entity_util

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect

/**
 * interface for meshing an entity (or any object really) with the Modifier system. The interface provides a base [AugmentEffect] instance for storing and passing effect attributes. See that doc for details on what is stored.
 *
 * The [passEffects] method is used to pass in modifications to the default AugmentEffect from the calling object. In default implementations, the Scepter Augments apply their effect modifiers to the Missile Entities they summon before spawning them.
 *
 * passEffects defines how you want your entity to be affected by any modifications. For example, if you want the area-of-effect of a status effect cloud your entity creates to be affected, add a setRange or addRange call to passEffects so any calling object that affects range will affect this entity. Then, in the AOE effect implementation, define the range/size of the cloud with the [entityEffects] range instance rather than with a static number.
 */

interface ModifiableEffectEntity {

    var entityEffects: AugmentEffect
    var level: Int

    fun passEffects(ae: AugmentEffect, level: Int){
        entityEffects = AugmentEffect().plus(ae)
        this.level = level
    }

}