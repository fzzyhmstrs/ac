package me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired

class AugmentType(val block: Boolean, val entity: Boolean, val damage: Boolean,val beneficial: Boolean, val empty: Boolean = false){

    companion object{
        val EMPTY = AugmentType(block = false, entity = false, damage = false, beneficial = false, empty = true)
        val BOLT = AugmentType(block = true, entity = true, damage = true, beneficial = false)
        val SINGLE_TARGET_OR_SELF = AugmentType(block = false, entity = true, damage = false, beneficial = true)
        val SINGLE_TARGET = AugmentType(block = false, entity = true, damage = false, beneficial = false)
        val DIRECTED_ENERGY = AugmentType(block = true, entity = true, damage = true, beneficial = false)
        val AOE_POSITIVE = AugmentType(block = false, entity = true, damage = false, beneficial = true)
        val AOE_NEGATIVE = AugmentType(block = false, entity = true, false, beneficial = false)
        val SUMMON = AugmentType(block = true, entity = false, damage = false, beneficial = false)
        val BLOCK_TARGET = AugmentType(block = true, entity = false, damage = false, beneficial = true)
    }

}
