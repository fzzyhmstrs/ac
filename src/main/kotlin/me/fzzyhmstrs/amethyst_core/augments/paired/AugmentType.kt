package me.fzzyhmstrs.amethyst_core.augments.paired

import me.fzzyhmstrs.amethyst_core.AC
import net.minecraft.util.Identifier

class AugmentType(){

    private val typeData: MutableSet<Identifier> = mutableSetOf()

    fun with(feature: Identifier): AugmentType{
        typeData.add(feature)
        return this
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

        val BLOCK = Identifier(AC.MOD_ID,"interacts_blocks")
        val ENTITY = Identifier(AC.MOD_ID,"interacts_entities")
        val DAMAGE = Identifier(AC.MOD_ID,"damage_dealing")
        val EXPLODES = Identifier(AC.MOD_ID,"explodes")
        val SUMMONS = Identifier(AC.MOD_ID,"summons_mob")
        val PROJECTILE = Identifier(AC.MOD_ID,"spawns_projectiles")
        val BENEFICIAL = Identifier(AC.MOD_ID,"beneficial_effects")
        val AOE = Identifier(AC.MOD_ID,"area_of_effect")
        val EMPTY_TYPE = Identifier(AC.MOD_ID,"empty_type")

        val EMPTY = AugmentType().with(EMPTY_TYPE)
        val BOLT = AugmentType().with(BLOCK).with(ENTITY).with(DAMAGE).with(PROJECTILE)
        val BALL = AugmentType().with(BLOCK).with(ENTITY).with(DAMAGE).with(EXPLODES).with(PROJECTILE)
        val SINGLE_TARGET_OR_SELF = AugmentType().with(ENTITY).with(BENEFICIAL)
        val SINGLE_TARGET = AugmentType().with(ENTITY)
        val TARGET_DAMAGE = AugmentType().with(ENTITY).with(DAMAGE)
        val AREA_DAMAGE = AugmentType().with(ENTITY).with(DAMAGE).with(AOE)
        val DIRECTED_ENERGY = AugmentType().with(BLOCK).with(ENTITY).with(DAMAGE).with(AOE)
        val AOE_POSITIVE = AugmentType().with(ENTITY).with(BENEFICIAL).with(AOE)
        val AOE_NEGATIVE = AugmentType().with(ENTITY).with(AOE)
        val SUMMON = AugmentType().with(BLOCK).with(ENTITY).with(DAMAGE).with(SUMMONS)
        val SUMMON_BOOM = AugmentType().with(BLOCK).with(ENTITY).with(DAMAGE).with(SUMMONS).with(EXPLODES)
        val SUMMON_GOOD = AugmentType().with(BLOCK).with(ENTITY).with(SUMMONS).with(BENEFICIAL)
        val BLOCK_TARGET = AugmentType().with(BLOCK).with(BENEFICIAL)
        val BLOCK_AREA = AugmentType().with(BLOCK).with(BENEFICIAL).with(AOE)
    }

}
