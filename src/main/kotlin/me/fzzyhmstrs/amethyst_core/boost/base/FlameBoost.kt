package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.scepter.CustomDamageSources
import me.fzzyhmstrs.amethyst_core.scepter.augments.paired.DamageSourceBuilder
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier

class FlameBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"flame_boost"), Enchantments.FLAME, 1) {

    override fun modifyDamageSource(damageSource: DamageSourceBuilder, attacker: LivingEntity, source: Entity?): DamageSourceBuilder {
        damageSource.set(CustomDamageSources.FireDamageSource(source, attacker))
        return damageSource.fire(false)
    }
}