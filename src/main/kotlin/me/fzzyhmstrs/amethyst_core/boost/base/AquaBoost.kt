package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.paired.DamageSourceBuilder
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.scepter.CustomDamageSources
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier

class AquaBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"aqua_boost"), Enchantments.AQUA_AFFINITY, 1) {

    override fun modifyDamageSource(damageSource: DamageSourceBuilder, attacker: LivingEntity, source: Entity?): DamageSourceBuilder {
        return damageSource.set(CustomDamageSources.WaterDamageSource(source, attacker))
    }
}