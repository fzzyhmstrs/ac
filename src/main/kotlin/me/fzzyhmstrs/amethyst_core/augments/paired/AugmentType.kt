package me.fzzyhmstrs.amethyst_core.augments.paired

class AugmentType(val block: Boolean, val entity: Boolean, val damage: Boolean, val explodes: Boolean, val summon: Boolean, val beneficial: Boolean, val empty: Boolean = false){

    val negativeEffect: Boolean
        get() = entity && !damage && !beneficial

    val positiveEffect: Boolean
        get() = entity && !damage && beneficial


    companion object{
        val EMPTY = AugmentType(block = false, entity = false, damage = false, explodes = false, summon = false, beneficial = false, empty = true)
        val BOLT = AugmentType(block = true, entity = true, damage = true, explodes = false, summon = false, beneficial = false)
        val BALL = AugmentType(block = true, entity = true, damage = true, explodes = true, summon = false, beneficial = false)
        val SINGLE_TARGET_OR_SELF = AugmentType(block = false, entity = true, damage = false, explodes = false, summon = false, beneficial = true)
        val SINGLE_TARGET = AugmentType(block = false, entity = true, damage = false, explodes = false, summon = false, beneficial = false)
        val SINGLE_TARGET_DAMAGE = AugmentType(block = false, entity = true, damage = true, explodes = false, summon = false, beneficial = false)
        val DIRECTED_ENERGY = AugmentType(block = true, entity = true, damage = true, explodes = false, summon = false, beneficial = false)
        val AOE_POSITIVE = AugmentType(block = false, entity = true, damage = false, explodes = false, summon = false, beneficial = true)
        val AOE_NEGATIVE = AugmentType(block = false, entity = true, damage = false, explodes = false, summon = false, beneficial = false)
        val AOE_DAMAGE = AugmentType(block = false, entity = true, damage = true, explodes = false, summon = false, beneficial = false)
        val SUMMON = AugmentType(block = true, entity = false, damage = true, explodes = false, summon = true, beneficial = false)
        val SUMMON_BOOM = AugmentType(block = true, entity = false, damage = false, explodes = true, summon = true, beneficial = false)
        val SUMMON_GOOD = AugmentType(block = true, entity = false, damage = false, explodes = false, summon = true, beneficial = true)
        val BLOCK_TARGET = AugmentType(block = true, entity = false, damage = false, explodes = false, summon = false, beneficial = true)
    }

}
