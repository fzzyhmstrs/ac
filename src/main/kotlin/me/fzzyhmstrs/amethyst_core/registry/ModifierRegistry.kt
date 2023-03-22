package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentConsumer
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierHelper
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierInitializer
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object ModifierRegistry {

    internal val modifierRollList: ArrayList<AugmentModifier> = ArrayList(150)

    fun registerWithRolling(modifier: AugmentModifier, weight: Int = 5){
        if (modifier.availableForRoll){
            for (i in 1..5) {
                modifierRollList.add(modifier)
            }
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
    val GREATER_ATTUNED = AugmentModifier(Identifier(AC.MOD_ID,"greater_attuned"), cooldownModifier = -22.5, rollToll = 4)
    val ATTUNED = AugmentModifier(Identifier(AC.MOD_ID,"attuned"), cooldownModifier = -15.0, rollToll = 4).withDescendant(GREATER_ATTUNED)
    val LESSER_ATTUNED = AugmentModifier(Identifier(AC.MOD_ID,"lesser_attuned"), cooldownModifier = -7.5, rollToll = 3).withDescendant(ATTUNED)
    val GREATER_THRIFTY = AugmentModifier(Identifier(AC.MOD_ID,"greater_thrifty"), manaCostModifier = -15.0, rollToll = 4)
    val THRIFTY = AugmentModifier(Identifier(AC.MOD_ID,"thrifty"), manaCostModifier = -10.0, rollToll = 4).withDescendant(GREATER_THRIFTY)
    val LESSER_THRIFTY = AugmentModifier(Identifier(AC.MOD_ID,"lesser_thrifty"), manaCostModifier = -5.0, rollToll = 3).withDescendant(THRIFTY)
    val GREATER_REACH = AugmentModifier(Identifier("amethyst_imbuement","greater_reach"), rollToll = 4).withRange(rangePercent = 24.0)
    val REACH = AugmentModifier(Identifier("amethyst_imbuement","reach"), rollToll = 4).withDescendant(GREATER_REACH).withRange(rangePercent = 16.0)
    val LESSER_REACH = AugmentModifier(Identifier("amethyst_imbuement","lesser_reach"), rollToll = 3).withDescendant(REACH).withRange(rangePercent = 8.0)
    val GREATER_ENDURING = AugmentModifier(Identifier("amethyst_imbuement","greater_enduring"), rollToll = 4).withDuration(durationPercent = 65)
    val ENDURING = AugmentModifier(Identifier("amethyst_imbuement","enduring"), rollToll = 4).withDescendant(GREATER_ENDURING).withDuration(durationPercent = 30)
    val LESSER_ENDURING = AugmentModifier(Identifier("amethyst_imbuement","lesser_enduring"), rollToll = 3).withDescendant(ENDURING).withDuration(durationPercent = 15)
    val MODIFIER_DEBUG = AugmentModifier(Identifier(AC.MOD_ID,"modifier_debug")).withDamage(2.0F,2.0F).withRange(2.75)
    val MODIFIER_DEBUG_2 = AugmentModifier(Identifier(AC.MOD_ID,"modifier_debug_2"), levelModifier = 1).withDuration(10, durationPercent = 15).withAmplifier(1)
    val MODIFIER_DEBUG_3 = AugmentModifier(Identifier(AC.MOD_ID,"modifier_debug_3")).withConsumer(DEBUG_HEALING_CONSUMER).withConsumer(DEBUG_NECROTIC_CONSUMER)

    val MODIFIER_TYPE = Registry.register(ModifierHelperType.REGISTRY,ModifierType.id,ModifierType)

    object ModifierType: ModifierHelperType(Identifier(AC.MOD_ID,"amethyst_core_helper")){
        override fun getModifierIdKey(): String {
            return "modifier_id"
        }
        override fun getModifiersKey(): String {
            return "modifiers"
        }
        override fun getModifierInitializer(): ModifierInitializer {
            return ModifierHelper
        }
    }

    internal fun registerAll(){
        registerWithRolling(GREATER_ATTUNED,2)
        registerWithRolling(ATTUNED,4)
        registerWithRolling(LESSER_ATTUNED,6)
        registerWithRolling(GREATER_THRIFTY,2)
        registerWithRolling(THRIFTY,4)
        registerWithRolling(LESSER_THRIFTY,6)
        registerWithRolling(GREATER_REACH,2)
        registerWithRolling(REACH,4)
        registerWithRolling(LESSER_REACH,6)
        registerWithRolling(GREATER_ENDURING,2)
        registerWithRolling(ENDURING,4)
        registerWithRolling(LESSER_ENDURING,6)
        ModifierRegistry.register(MODIFIER_DEBUG)
        ModifierRegistry.register(MODIFIER_DEBUG_2)
        ModifierRegistry.register(MODIFIER_DEBUG_3)
    }

}
