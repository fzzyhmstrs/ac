package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentConsumer
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.ComplexAugmentModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierHelper
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType
import me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.effect.StatusEffectInstance
import net.minecraft.entity.effect.StatusEffects
import net.minecraft.util.Identifier

object ModifierRegistry {

    internal val modifierRollList: ArrayList<ComplexAugmentModifier> = ArrayList(150)

    fun getModifierRollList():List<ComplexAugmentModifier>{
        return modifierRollList
    }

    fun registerWithRolling(modifier: ComplexAugmentModifier, weight: Int = 5){
        if (modifier.availableForRoll){
            for (i in 1..weight) {
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
    val GREATER_ATTUNED = ComplexAugmentModifier(Identifier(AC.MOD_ID,"greater_attuned"), cooldownModifier = -22.5, rollToll = 4)
    val ATTUNED = ComplexAugmentModifier(Identifier(AC.MOD_ID,"attuned"), cooldownModifier = -15.0, rollToll = 4).withDescendant(GREATER_ATTUNED)
    val LESSER_ATTUNED = ComplexAugmentModifier(Identifier(AC.MOD_ID,"lesser_attuned"), cooldownModifier = -7.5, rollToll = 3).withDescendant(ATTUNED)
    val GREATER_THRIFTY = ComplexAugmentModifier(Identifier(AC.MOD_ID,"greater_thrifty"), manaCostModifier = -15.0, rollToll = 4)
    val THRIFTY = ComplexAugmentModifier(Identifier(AC.MOD_ID,"thrifty"), manaCostModifier = -10.0, rollToll = 4).withDescendant(GREATER_THRIFTY)
    val LESSER_THRIFTY = ComplexAugmentModifier(Identifier(AC.MOD_ID,"lesser_thrifty"), manaCostModifier = -5.0, rollToll = 3).withDescendant(THRIFTY)
    val GREATER_REACH = ComplexAugmentModifier(Identifier("amethyst_imbuement","greater_reach"), rollToll = 4).withRange(rangePercent = 24.0)
    val REACH = ComplexAugmentModifier(Identifier("amethyst_imbuement","reach"), rollToll = 4).withDescendant(GREATER_REACH).withRange(rangePercent = 16.0)
    val LESSER_REACH = ComplexAugmentModifier(Identifier("amethyst_imbuement","lesser_reach"), rollToll = 3).withDescendant(REACH).withRange(rangePercent = 8.0)
    val GREATER_ENDURING = ComplexAugmentModifier(Identifier("amethyst_imbuement","greater_enduring"), rollToll = 4).withDuration(durationPercent = 65)
    val ENDURING = ComplexAugmentModifier(Identifier("amethyst_imbuement","enduring"), rollToll = 4).withDescendant(GREATER_ENDURING).withDuration(durationPercent = 30)
    val LESSER_ENDURING = ComplexAugmentModifier(Identifier("amethyst_imbuement","lesser_enduring"), rollToll = 3).withDescendant(ENDURING).withDuration(durationPercent = 15)
    val MODIFIER_DEBUG = AugmentModifier(Identifier(AC.MOD_ID,"modifier_debug")).withDamage(2.0F,2.0F).withRange(2.75)
    val MODIFIER_DEBUG_2 = AugmentModifier(Identifier(AC.MOD_ID,"modifier_debug_2"), levelModifier = 1).withDuration(10, durationPercent = 15).withAmplifier(1)
    val MODIFIER_DEBUG_3 = AugmentModifier(Identifier(AC.MOD_ID,"modifier_debug_3")).withConsumer(DEBUG_HEALING_CONSUMER).withConsumer(DEBUG_NECROTIC_CONSUMER)

    val MODIFIER_TYPE = ModifierHelperType.register(ModifierType)

    object ModifierType: ModifierHelperType<AugmentModifier>(Identifier(AC.MOD_ID,"amethyst_core_helper"), ModifierHelper){
        override fun getModifierIdKey(): String {
            return "modifier_id"
        }
        override fun getModifiersKey(): String {
            return "modifiers"
        }

        override fun getModifierInitKey(): String {
            return "ac_"
        }
    }

    internal fun registerAll(){
        registerWithRolling(GREATER_ATTUNED,2)
        registerWithRolling(ATTUNED as ComplexAugmentModifier,4)
        registerWithRolling(LESSER_ATTUNED as ComplexAugmentModifier,6)
        registerWithRolling(GREATER_THRIFTY,2)
        registerWithRolling(THRIFTY as ComplexAugmentModifier,4)
        registerWithRolling(LESSER_THRIFTY as ComplexAugmentModifier,6)
        registerWithRolling(GREATER_REACH as ComplexAugmentModifier,2)
        registerWithRolling(REACH as ComplexAugmentModifier,4)
        registerWithRolling(LESSER_REACH as ComplexAugmentModifier,6)
        registerWithRolling(GREATER_ENDURING as ComplexAugmentModifier,2)
        registerWithRolling(ENDURING as ComplexAugmentModifier,4)
        registerWithRolling(LESSER_ENDURING as ComplexAugmentModifier,6)
        ModifierRegistry.register(MODIFIER_DEBUG)
        ModifierRegistry.register(MODIFIER_DEBUG_2)
        ModifierRegistry.register(MODIFIER_DEBUG_3)
        /*ModifyModifiersEvent.EVENT.register{ _, user, _, modifiers ->
            modifiers.combineWith(ModifierHelper.getActiveModifiers(user), AugmentModifier())
        } no longer needed now that modifiers are player-based */ 
    }

}
