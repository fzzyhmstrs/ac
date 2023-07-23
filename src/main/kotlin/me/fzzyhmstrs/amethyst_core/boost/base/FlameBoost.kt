package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.paired.DamageSourceBuilder
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

class FlameBoost: EnchantmentAugmentBoost(AC.identity("flame_boost"), Enchantments.FLAME, 1) {

    override fun <T> modifyDamageSource(
        builder: DamageSourceBuilder,
        context: ProcessContext,
        entityHitResult: EntityHitResult,
        source: Entity?,
        user: T,
        world: World,
        hand: Hand,
        spells: PairedAugments
    )
    :
    DamageSourceBuilder
    where
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return builder.add(DamageTypes.IN_FIRE)
    }

    override fun <T> modifyDamage(amount: Float, context: ProcessContext, entityHitResult: EntityHitResult, user: T, world: World, hand: Hand, spells: PairedAugments)
    :
    Float
    where
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val entity = entityHitResult.entity
        if (entity is LivingEntity){
            entity.setOnFireFor(2)
        }
        return super.modifyDamage(amount, context, entityHitResult, user, world, hand, spells)
    }
}