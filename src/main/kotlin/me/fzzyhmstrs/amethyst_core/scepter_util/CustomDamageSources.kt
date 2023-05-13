package me.fzzyhmstrs.amethyst_core.scepter_util

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity

object CustomDamageSources {

    class GenericDamageSource(source: Entity?, attacker: LivingEntity): CustomDamageSource("customGeneric", source,attacker)
    class SoulDamageSource(source: Entity?, attacker: LivingEntity): CustomDamageSource("customSoul", source,attacker)
    class MagicDamageSource(source: Entity?, attacker: LivingEntity): CustomDamageSource("customMagic", source,attacker)
    class HolyDamageSource(source: Entity?, attacker: LivingEntity): CustomDamageSource("customHoly", source,attacker)
    class FireDamageSource(source: Entity?, attacker: LivingEntity): CustomDamageSource("customFire", source,attacker)
    class LightningDamageSource(source: Entity?, attacker: LivingEntity): CustomDamageSource("customLightning", source,attacker)
    class FreezingDamageSource(source: Entity?, attacker: LivingEntity): CustomDamageSource("customFreezing", source,attacker)
}