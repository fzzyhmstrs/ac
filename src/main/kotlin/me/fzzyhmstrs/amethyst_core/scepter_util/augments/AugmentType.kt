package me.fzzyhmstrs.amethyst_core.scepter_util.augments

class AugmentType(val block: Boolean, val entity: Boolean, val empty: Boolean = false){

    companion object{
        val EMPTY = AugmentType(block = false, entity = false, empty = true)
        val BOLT = AugmentType(block = true, entity = true)
        val SINGLE_TARGET_OR_SELF = AugmentType(block = false, entity = true)
        val SINGLE_TARGET = AugmentType(block = false, entity = true)
        val DIRECTED_ENERGY = AugmentType(block = true, entity = true)
        val AOE_POSITIVE = AugmentType(block = false, entity = true)
        val AOE_NEGATIVE = AugmentType(block = false, entity = true)
        val SUMMON = AugmentType(block = true, entity = false)
        val BLOCK_TARGET = AugmentType(block = true, entity = false)
    }

}
