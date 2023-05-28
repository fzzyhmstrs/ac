package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.PairedAugments
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EntityGroup
import net.minecraft.entity.LivingEntity
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

class SmiteBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"smite_boost"), Enchantments.SMITE, 5) {

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
        return if(entity is LivingEntity && entity.group == EntityGroup.UNDEAD){
            amount * 1.25f
        } else {
            amount
        }
    }
}