package me.fzzyhmstrs.amethyst_core.boost

import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.augments.paired.*
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

abstract class AugmentBoost(val id: Identifier) {
    open val boostEffect = AugmentEffect()
    open val cooldownModifier: PerLvlI = PerLvlI()
    open val manaCostModifier: PerLvlI = PerLvlI()

    open fun modifyStack(stack: ItemStack): ItemStack{
        return stack
    }
    open fun <T> modifyDamage(amount: Float, context: ProcessContext, entityHitResult: EntityHitResult, user: T, world: World, hand: Hand, spells: PairedAugments)
    :
    Float
    where
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return amount
    }
    open fun <T> modifyDamageSource(builder: DamageSourceBuilder, context: ProcessContext, entityHitResult: EntityHitResult, source: Entity?, user: T, world: World, hand: Hand, spells: PairedAugments)
    :
    DamageSourceBuilder
    where
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return builder
    }
    open fun modifyExplosion(builder: ExplosionBuilder, context: ProcessContext, user: LivingEntity?, world: World, hand: Hand, spells: PairedAugments)
    :
    ExplosionBuilder
    {
        return builder
    }
    fun <T> modifyCount(start: Int, context: ProcessContext, user: T, world: World, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    :
    Int
    where
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return start
    }

    open fun name(): Text{
        return AcText.translatable("boost.${id.path}.${id.namespace}")
    }
    open fun appendDescription(description: MutableList<Text>){
        description.add(AcText.translatable("boost.${id.path}.${id.namespace}.desc"))
    }

    abstract fun matches(stack: ItemStack): Boolean
    open fun canAccept(augment: ScepterAugment): Boolean{return true}
    abstract fun asStacks(): Collection<ItemStack>

    protected fun isInTag(augment: Enchantment,tag: TagKey<Enchantment>): Boolean{
        val opt = Registries.ENCHANTMENT.getEntry(Registries.ENCHANTMENT.getRawId(augment))
        var bl = false
        opt.ifPresent { entry -> bl = entry.isIn(tag) }
        return bl
    }
}