package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
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
open class ComplexAugmentModifier(
    modifierId: Identifier = ModifierDefaults.BLANK_ID,
    levelModifier: Int = 0,
    cooldownModifier: Double = 0.0,
    manaCostModifier: Double = 0.0,
    internal val availableForRoll: Boolean = true,
    internal val rollToll: Int = 5)
    :
    AugmentModifier(modifierId)
{

    private var secondaryEffect: ScepterAugment? = null

    override fun hasSecondaryEffect(): Boolean{
        return secondaryEffect != null
    }
    override fun getSecondaryEffect(): ScepterAugment?{
        return secondaryEffect
    }
    fun withSecondaryEffect(augment: ScepterAugment): ComplexAugmentModifier {
        secondaryEffect = augment
        return this
    }
}
