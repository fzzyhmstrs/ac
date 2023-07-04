package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.paired.DamageSourceBuilder
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.util.Identifier

class PiercingBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"aqua_boost"), Enchantments.PIERCING, 4) {

    override fun modifyDamageSource(damageSource: DamageSourceBuilder, attacker: LivingEntity, source: Entity?): DamageSourceBuilder {
        return damageSource.add(DamageTypes.MAGIC)
    }

}