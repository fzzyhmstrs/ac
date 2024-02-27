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
    open var manaCostModifier: Double = 0.0)
    :
    AbstractModifier<AugmentModifier>(modifierId)
{

    private var effects: AugmentEffect? = null
    protected fun getModEffects(): AugmentEffect{
        return if (effects == null)
            AugmentEffect().also { effects = it }
        else
            effects as AugmentEffect
    }
    private var xpModifier: XpModifiers? = null
    protected fun getModXpModifier(): XpModifiers{
        return if (xpModifier == null)
            XpModifiers().also { xpModifier = it }
        else
            xpModifier as XpModifiers
    }

    override fun plus(other: AugmentModifier): AugmentModifier {
        levelModifier += other.levelModifier
        cooldownModifier += other.cooldownModifier
        manaCostModifier += other.manaCostModifier
        if (other.xpModifier != null)
            getModXpModifier().plus(other.getModXpModifier())
        if (other.effects != null)
            getModEffects().plus(other.getModEffects())
        return this
    }

    fun hasSpellToAffect(): Boolean{
        return hasObjectToAffect()
    }
    fun checkSpellsToAffect(id: Identifier): Boolean{
        return checkObjectsToAffect(id)
    }
    open fun hasSecondaryEffect(): Boolean{
        return false
    }
    open fun getSecondaryEffect(): ScepterAugment?{
        return null
    }
    fun getEffectModifier(): AugmentEffect? {
        return effects
    }
    fun getXpModifiers(): XpModifiers? {
        return xpModifier
    }

    fun withDamage(damage: Float = 0.0F, damagePerLevel: Float = 0.0F, damagePercent: Float = 0.0F): AugmentModifier {
        getModEffects().addDamage(damage, damagePerLevel, damagePercent)
        return this
    }
    fun withAmplifier(amplifier: Int = 0, amplifierPerLevel: Int = 0, amplifierPercent: Int = 0): AugmentModifier {
        getModEffects().addAmplifier(amplifier, amplifierPerLevel, amplifierPercent)
        return this
    }
    fun withDuration(duration: Int = 0, durationPerLevel: Int = 0, durationPercent: Int = 0): AugmentModifier {
        getModEffects().addDuration(duration, durationPerLevel, durationPercent)
        return this
    }
    fun withRange(range: Double = 0.0, rangePerLevel: Double = 0.0, rangePercent: Double = 0.0): AugmentModifier {
        getModEffects().addRange(range, rangePerLevel, rangePercent)
        return this
    }
    fun withSecondaryEffect(effect: AugmentEffect): AugmentModifier {
        getModEffects().plus(effect)
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
        getModXpModifier().plus(xpMods)
        return this
    }
    fun withSpellToAffect(predicate: Predicate<Identifier>): AugmentModifier {
        addObjectToAffect(predicate)
        return this
    }
    fun withConsumer(consumer: Consumer<List<LivingEntity>>, type: AugmentConsumer.Type): AugmentModifier {
        getModEffects().withConsumer(consumer,type)
        return this
    }
    fun withConsumer(augmentConsumer: AugmentConsumer): AugmentModifier {
        getModEffects().withConsumer(augmentConsumer.consumer,augmentConsumer.type)
        return this
    }
    fun withDescendant(modifier: AugmentModifier): AugmentModifier {
        addDescendant(modifier)
        return this
    }

    override fun isAcceptableItem(stack: ItemStack): Boolean {
        val nbt = stack.nbt ?: return super.isAcceptableItem(stack)
        val mods = getModifierHelper().getModifiersFromNbt(nbt)
        var furthestDescendant = this.modifierId
        var descendant = this
        do {
            descendant = descendant.getDescendant() ?: break
            furthestDescendant = descendant.modifierId
        } while (descendant.hasDescendant())
        if (mods.contains(furthestDescendant)) return false
        return super.isAcceptableItem(stack)
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

    override fun getModifierHelper(): AbstractModifierHelper<AugmentModifier> {
        return ModifierHelper
    }

    override fun getTranslationKey(): String {
        return super.getTranslationKey()
    }
}