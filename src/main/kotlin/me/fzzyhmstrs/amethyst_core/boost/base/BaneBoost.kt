package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.EntityGroup
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

class BaneBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"bane_boost"), Enchantments.BANE_OF_ARTHROPODS, 5) {

    override fun <T> modifyDamage(amount: Float, context: ProcessContext, entityHitResult: EntityHitResult, user: T, world: World, hand: Hand, spells: PairedAugments)
            :
            Float
            where
            T: LivingEntity,
            T: SpellCastingEntity
    {
        val entity = entityHitResult.entity
        return if(entity is LivingEntity && entity.group == EntityGroup.ARTHROPOD){
            amount * 1.25f
        } else {
            amount
        }
    }
}