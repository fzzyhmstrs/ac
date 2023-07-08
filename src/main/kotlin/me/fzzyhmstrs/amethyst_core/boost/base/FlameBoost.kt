package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.augments.paired.DamageSourceBuilder
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

class FlameBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"flame_boost"), Enchantments.FLAME, 1) {

    override fun modifyDamageSource(damageSource: DamageSourceBuilder, attacker: LivingEntity, source: Entity?): DamageSourceBuilder {
        return damageSource.add(DamageTypes.IN_FIRE)
    }

    override fun modifyDamage(
        amount: Float,
        context: ProcessContext,
        entityHitResult: EntityHitResult,
        user: LivingEntity,
        world: World,
        hand: Hand,
        level: Int,
        effects: AugmentEffect,
        spells: PairedAugments
    ): Float {
        val entity = entityHitResult.entity
        if (entity is LivingEntity){
            entity.setOnFireFor(2)
        }
        return super.modifyDamage(amount, context, entityHitResult, user, world, hand, level, effects, spells)
    }
}