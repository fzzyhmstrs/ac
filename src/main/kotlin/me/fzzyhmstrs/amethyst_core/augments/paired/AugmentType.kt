package me.fzzyhmstrs.amethyst_core.augments.paired

import me.fzzyhmstrs.amethyst_core.AC
import net.minecraft.util.Identifier

class AugmentType private constructor (private val typeData: Set<Identifier>){

    constructor(): this(setOf())

    fun plus(feature: Identifier): AugmentType{
        val set = typeData.toMutableSet()
        set.add(feature)
        return AugmentType(set.toSet())
    }

    fun has(feature: Identifier): Boolean{
        return typeData.contains(feature)
    }

    val negativeEffect: Boolean
        get() = has(ENTITY) && !has(DAMAGE) && !has(BENEFICIAL)

    val positiveEffect: Boolean
        get() = has(ENTITY) && !has(DAMAGE) && has(BENEFICIAL)

    val empty: Boolean
        get() = has(EMPTY_TYPE)

    companion object{

        val BLOCK = AC.identity("interacts_blocks")
        val ENTITY = AC.identity("interacts_entities")
        val DAMAGE = AC.identity("damage_dealing")
        val EXPLODES = AC.identity("explodes")
        val SUMMONS = AC.identity("summons_mob")
        val PROJECTILE = AC.identity("spawns_projectiles")
        val BENEFICIAL = AC.identity("beneficial_effects")
        val AOE = AC.identity("area_of_effect")
        val EMPTY_TYPE = AC.identity("empty_type")

        val EMPTY = Builder().with(EMPTY_TYPE).build()
        val BOLT = Builder().with(BLOCK).with(ENTITY).with(DAMAGE).with(PROJECTILE).build()
        val BALL = Builder().with(BLOCK).with(ENTITY).with(DAMAGE).with(EXPLODES).with(PROJECTILE).build()
        val SINGLE_TARGET_OR_SELF = Builder().with(ENTITY).with(BENEFICIAL).build()
        val SINGLE_TARGET = Builder().with(ENTITY).build()
        val TARGET_DAMAGE = Builder().with(ENTITY).with(DAMAGE).build()
        val AREA_DAMAGE = Builder().with(ENTITY).with(DAMAGE).with(AOE).build()
        val DIRECTED_ENERGY = Builder().with(BLOCK).with(ENTITY).with(DAMAGE).with(AOE).build()
        val AOE_POSITIVE = Builder().with(ENTITY).with(BENEFICIAL).with(AOE).build()
        val AOE_NEGATIVE = Builder().with(ENTITY).with(AOE).build()
        val SUMMON = Builder().with(BLOCK).with(ENTITY).with(DAMAGE).with(SUMMONS).build()
        val SUMMON_BOOM = Builder().with(BLOCK).with(ENTITY).with(DAMAGE).with(SUMMONS).with(EXPLODES).build()
        val SUMMON_GOOD = Builder().with(BLOCK).with(ENTITY).with(SUMMONS).with(BENEFICIAL).build()
        val BLOCK_TARGET = Builder().with(BLOCK).with(BENEFICIAL).build()
        val BLOCK_AREA = Builder().with(BLOCK).with(BENEFICIAL).with(AOE).build()
    }

    class Builder{
        private val typeData: MutableSet<Identifier> = mutableSetOf()
        fun with(feature: Identifier): Builder{
            typeData.add(feature)
            return this
        }
        fun build(): AugmentType{
            return AugmentType(typeData.toSet())
        }
    }
}


