package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifierHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.function.Consumer
import java.util.function.Predicate

/**
 * An [AbstractModifier] implementation purpose-built to work with the [ScepterAugment][me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment] and [AugmentScepterItem][me.fzzyhmstrs.amethyst_core.item_util.AugmentScepterItem] system.
 *
 * [levelModifier]: [Scepter Augments][me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment] have a leveling system. This modifier alters the level an affected spell casts as.
 *
 * [cooldownModifier]: Alters the cooldown of the affected spell. Operates as a percentage (cooldownModifier = 20.0 will add 20% to the spell cooldown, -20.0 will shorten the cooldown 20%, and so on).
 *
 * [manaCostModifier]: Alters the mana cost of the affected spell. Operates as a percentage.
 *
 * [effects]: Holds a [AugmentEffect] instance for passing to the affected spell.
 *
 * [xpModifier]: holds a [XpModifiers] instance that modifies how the affected Scepters statistics are incremented.
 *
 * [secondaryEffect]: a secondary [ScepterAugment] that the affected spell will attempt to cast in addition to its normal effect.
 */
open class AugmentModifier(
    modifierId: Identifier = ModifierDefaults.BLANK_ID,
    open var levelModifier: Int = 0,
    open var cooldownModifier: Double = 0.0,
    open var manaCostModifier: Double = 0.0,
    internal val availableForRoll: Boolean = true,
    internal val rollToll: Int = 5)
    :
    AbstractModifier<AugmentModifier>(modifierId)
{

    protected val effects: AugmentEffect = AugmentEffect()
    protected val xpModifier: XpModifiers = XpModifiers()
    private var secondaryEffect: ScepterAugment? = null

    private var hasSecondEffect: Boolean = false

    override fun plus(other: AugmentModifier): AugmentModifier {
        levelModifier += other.levelModifier
        cooldownModifier += other.cooldownModifier
        manaCostModifier += other.manaCostModifier
        xpModifier.plus(other.getXpModifiers())
        effects.plus(other.getEffectModifier())
        return this
    }

    fun hasSpellToAffect(): Boolean{
        return hasObjectToAffect()
    }
    fun checkSpellsToAffect(id: Identifier): Boolean{
        return checkObjectsToAffect(id)
    }
    fun hasSecondaryEffect(): Boolean{
        return hasSecondEffect
    }
    fun getSecondaryEffect(): ScepterAugment?{
        return secondaryEffect
    }
    fun getEffectModifier(): AugmentEffect {
        return effects
    }
    fun getXpModifiers(): XpModifiers {
        return xpModifier
    }

    fun withDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F): AugmentModifier {
        effects.plus(ModifierDefaults.BLANK_EFFECT.withDamage(damage, damagePerLevel, damagePercent))
        return this
    }
    fun withAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0): AugmentModifier {
        effects.plus(ModifierDefaults.BLANK_EFFECT.withAmplifier(amplifier, amplifierPerLevel, amplifierPercent))
        return this
    }
    fun withDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0): AugmentModifier {
        effects.plus(ModifierDefaults.BLANK_EFFECT.withDuration(duration, durationPerLevel, durationPercent))
        return this
    }
    fun withRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0): AugmentModifier {
        effects.plus(ModifierDefaults.BLANK_EFFECT.withRange(range, rangePerLevel, rangePercent))
        return this
    }
    fun withSecondaryEffect(effect: AugmentEffect): AugmentModifier {
        effects.plus(effect)
        return this
    }
    fun withXpMod(type: SpellType, xpMod: Int): AugmentModifier {
        val xpMods = when(type){
                SpellType.FURY ->{
                    ModifierDefaults.BLANK_XP_MOD.withFuryMod(xpMod)}
                SpellType.WIT ->{
                    ModifierDefaults.BLANK_XP_MOD.withWitMod(xpMod)}
                SpellType.GRACE ->{
                    ModifierDefaults.BLANK_XP_MOD.withGraceMod(xpMod)}
                else -> return this
            }
        xpModifier.plus(xpMods)
        return this
    }
    fun withSpellToAffect(predicate: Predicate<Identifier>): AugmentModifier {
        addObjectToAffect(predicate)
        return this
    }
    fun withSecondaryEffect(augment: ScepterAugment): AugmentModifier {
        secondaryEffect = augment
        hasSecondEffect = true
        return this
    }
    fun withConsumer(consumer: Consumer<List<LivingEntity>>, type: AugmentConsumer.Type): AugmentModifier {
        effects.withConsumer(consumer,type)
        return this
    }
    fun withConsumer(augmentConsumer: AugmentConsumer): AugmentModifier {
        effects.withConsumer(augmentConsumer.consumer,augmentConsumer.type)
        return this
    }
    fun withDescendant(modifier: AugmentModifier): AugmentModifier {
        addDescendant(modifier)
        return this
    }

    override fun acceptableItemStacks(): MutableList<ItemStack>{
        return ModifierHelper.scepterAcceptableItemStacks(1)
    }

    override fun compiler(): Compiler {
        return Compiler(arrayListOf(), AugmentModifier())
    }

    override fun getName(): Text {
        return AcText.translatable(getTranslationKey())
    }

    override fun getModifierHelper(): AbstractModifierHelper<*> {
        return ModifierHelper
    }
}