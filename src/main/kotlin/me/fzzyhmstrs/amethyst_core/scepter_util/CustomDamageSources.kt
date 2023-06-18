package me.fzzyhmstrs.amethyst_core.scepter_util

import me.fzzyhmstrs.amethyst_core.AC
import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.util.Identifier
import net.minecraft.world.World

object CustomDamageSources {

    //val SOUL = RegistryKey.of(RegistryKeys.DAMAGE_TYPE, Identifier(AC.MOD_ID,"soul"))

    fun lightningBolt(world: World, source: Entity?, attacker: Entity?): DamageSource{
        return world.damageSources.create(DamageTypes.LIGHTNING_BOLT,source, attacker)
    }

    fun freeze(world: World, source: Entity?, attacker: Entity?): DamageSource{
        return world.damageSources.create(DamageTypes.FREEZE,source, attacker)
    }

/*
    class SoulDamageSource(source: Entity?): EntityDamageSource("soul", source){
    }
    class SmitingDamageSource(source: Entity?): EntityDamageSource("smite", source){
    }
*/

}