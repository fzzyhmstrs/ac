package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentConsumer
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.util.Identifier

object ModifierRegistry {

    internal val modifierRollList: MutableList<AugmentModifier> = mutableListOf()

    fun registerWithRolling(modifier: AugmentModifier){
        if (modifier.availableForRoll){
            modifierRollList.add(modifier)
        }
        ModifierRegistry.register(modifier)
    }

    /**
     * example harmful [AugmentConsumer] that applies wither to targets specified to receive Harmful effects in the [ScepterAugment][me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment] implementation.
     */
    private val DEBUG_NECROTIC_CONSUMER = AugmentConsumer({ list: List<LivingEntity> -> necroticConsumer(list)}, AugmentConsumer.Type.HARMFUL)
    private fun necroticConsumer(list: List<LivingEntity>){
        list.forEach {
            it.addStatusEffect(
                StatusEffectInstance(StatusEffects.WITHER,80)
            )
        }
    }

    /**
     * example beneficial [AugmentConsumer] that applies regeneration to targets specified to receive beneficial effects. Most commonly, this will be the player than cast the [ScepterAugment][me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment], but may also be other targets of, for example, a mass healing spell.
     */
    private val DEBUG_HEALING_CONSUMER = AugmentConsumer({ list: List<LivingEntity> -> healingConsumer(list)}, AugmentConsumer.Type.BENEFICIAL)
    private fun healingConsumer(list: List<LivingEntity>){
        list.forEach {
            it.addStatusEffect(
                StatusEffectInstance(StatusEffects.REGENERATION,40)
            )
        }
    }

    /**
     * built-in modifiers. Attuned and Thrifty are provided with Imbuing recipes for use with _Amethyst Imbuement_ by default.
     *
     * Amethyst Imbuement namespace kept for Reach and Enduring lineages to avoid breaking changes in-game
     */
    val GREATER_ATTUNED = AugmentModifier(Identifier(AC.MOD_ID,"greater_attuned"), cooldownModifier = -22.5)
    val ATTUNED = AugmentModifier(Identifier(AC.MOD_ID,"attuned"), cooldownModifier = -15.0).withDescendant(GREATER_ATTUNED)
    val LESSER_ATTUNED = AugmentModifier(Identifier(AC.MOD_ID,"lesser_attuned"), cooldownModifier = -7.5).withDescendant(ATTUNED)
    val GREATER_THRIFTY = AugmentModifier(Identifier(AC.MOD_ID,"greater_thrifty"), manaCostModifier = -15.0)
    val THRIFTY = AugmentModifier(Identifier(AC.MOD_ID,"thrifty"), manaCostModifier = -10.0).withDescendant(GREATER_THRIFTY)
    val LESSER_THRIFTY = AugmentModifier(Identifier(AC.MOD_ID,"lesser_thrifty"), manaCostModifier = -5.0).withDescendant(THRIFTY)
    val GREATER_REACH = AugmentModifier(Identifier("amethyst_imbuement","greater_reach")).withRange(rangePercent = 24.0)
    val REACH = AugmentModifier(Identifier("amethyst_imbuement","reach")).withDescendant(GREATER_REACH).withRange(rangePercent = 16.0)
    val LESSER_REACH = AugmentModifier(Identifier("amethyst_imbuement","lesser_reach")).withDescendant(REACH).withRange(rangePercent = 8.0)
    val GREATER_ENDURING = AugmentModifier(Identifier("amethyst_imbuement","greater_enduring")).withDuration(durationPercent = 65)
    val ENDURING = AugmentModifier(Identifier("amethyst_imbuement","enduring")).withDescendant(GREATER_ENDURING).withDuration(durationPercent = 30)
    val LESSER_ENDURING = AugmentModifier(Identifier("amethyst_imbuement","lesser_enduring")).withDescendant(ENDURING).withDuration(durationPercent = 15)
    val MODIFIER_DEBUG = AugmentModifier(Identifier(AC.MOD_ID,"modifier_debug")).withDamage(2.0F,2.0F).withRange(2.75)
    val MODIFIER_DEBUG_2 = AugmentModifier(Identifier(AC.MOD_ID,"modifier_debug_2"), levelModifier = 1).withDuration(10, durationPercent = 15).withAmplifier(1)
    val MODIFIER_DEBUG_3 = AugmentModifier(Identifier(AC.MOD_ID,"modifier_debug_3")).withConsumer(DEBUG_HEALING_CONSUMER).withConsumer(DEBUG_NECROTIC_CONSUMER)

    internal fun registerAll(){
        ModifierRegistry.register(GREATER_ATTUNED)
        ModifierRegistry.register(ATTUNED)
        ModifierRegistry.register(LESSER_ATTUNED)
        ModifierRegistry.register(GREATER_THRIFTY)
        ModifierRegistry.register(THRIFTY)
        ModifierRegistry.register(LESSER_THRIFTY)
        ModifierRegistry.register(GREATER_REACH)
        ModifierRegistry.register(REACH)
        ModifierRegistry.register(LESSER_REACH)
        ModifierRegistry.register(GREATER_ENDURING)
        ModifierRegistry.register(ENDURING)
        ModifierRegistry.register(LESSER_ENDURING)
        ModifierRegistry.register(MODIFIER_DEBUG)
        ModifierRegistry.register(MODIFIER_DEBUG_2)
        ModifierRegistry.register(MODIFIER_DEBUG_3)
    }

}