package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EntityGroup
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

class ImpalingBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"impaling_boost"), Enchantments.IMPALING, Enchantments.IMPALING.maxLevel) {

    override fun modifyDamage(
        amount: Float,
        cause: ScepterAugment,
        entityHitResult: EntityHitResult,
        user: LivingEntity,
        world: World,
        hand: Hand,
        level: Int,
        effects: AugmentEffect,
        spells: PairedAugments
    ): Float {
        val entity = entityHitResult.entity
        return if(entity is LivingEntity && entity.group == EntityGroup.AQUATIC){
            amount * 1.25f
        } else {
            amount
        }
    }
}