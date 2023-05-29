package me.fzzyhmstrs.amethyst_core.boost

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.DamageSourceBuilder
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.ProcessContext
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
    open fun modifyDamage(amount: Float, cause: ScepterAugment, entityHitResult: EntityHitResult, user: LivingEntity, world: World, hand: Hand, level: Int, effects: AugmentEffect, spells: PairedAugments): Float{
        return amount
    }
    open fun onEntityKill(entityHitResult: EntityHitResult, context: ProcessContext, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments) {
    }
    open fun modifyDamageSource(damageSource: DamageSourceBuilder, attacker: LivingEntity, source: Entity?): DamageSourceBuilder {
        return damageSource
    }

    open fun name(): Text{
        return AcText.translatable("boost.${id.path}.${id.namespace}")
    }
    open fun appendDescription(description: MutableList<Text>){
        description.add(AcText.translatable("boost.${id.path}.${id.namespace}.desc"))
    }

    abstract fun matches(stack: ItemStack, augment: ScepterAugment): Boolean
    abstract fun asStack(): ItemStack

    protected fun isInTag(augment: Enchantment,tag: TagKey<Enchantment>): Boolean{
        val opt = Registries.ENCHANTMENT.getEntry(Registries.ENCHANTMENT.getRawId(augment))
        var bl = false
        opt.ifPresent { entry -> bl = entry.isIn(tag) }
        return bl
    }
}