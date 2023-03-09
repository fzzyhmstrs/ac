package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier

object RegisterAttribute {

    //spell level, acts as a multiplier. Value of 0.1 will add 10% to a spells effective level
    val SPELL_LEVEL: EntityAttribute = make("spell_level", 0.0, -1.0, 10.0)

    //spell cooldown, acts as a multiplier. A player with -0.1 spell cooldown has 10% increased casting speed (0.9 the normal cooldown)
    val SPELL_COOLDOWN: EntityAttribute = make("spell_cooldown", 0.0, -100.0, 1000.0)

    //spell mana cost, acts as a multiplier. A player with -0.1 spell mana cost has 10% reduced mana costs
    val SPELL_MANA_COST: EntityAttribute = make("spell_mana_cost", 0.0, -100.0, 1000.0)

    //spell damage, acts as a multiplier. A player with -10.0 spell damage has 10% reduced damage
    val SPELL_DAMAGE: EntityAttribute = make("spell_damage", 0.0, -100.0, 3200.0)

    //spell amplifier, a flat addition. Any non-integer values of this attribute may not do anything.
    // A spell amplifier of 1.0 will add 1 to the status effect level/amplifier of applied effects
    val SPELL_AMPLIFIER: EntityAttribute = make("spell_amplifier", 0.0, 0.0, 32.0)

    //spell duration, acts as a multiplier. A player with 0.1 spell duration has 10% increased duration of spell effects
    val SPELL_DURATION: EntityAttribute = make("spell_duration", 0.0, -100.0, 3200.0)

    //spell range, acts as a multiplier. A player with -0.1 spell damage has 10% reduced damage
    val SPELL_RANGE: EntityAttribute = make("spell_range", 0.0, -100.0, 500.0)

    //player enchantability, acts as a multiplier on the enchantability of items they put into an enchanting table. A player with 0.1 enchantability will have 10% increased item enchantability
    val ENCHANTABILITY: EntityAttribute = make("enchantability", 0.0, -100.0, 300.0)

    fun registerAll(){
        Registry.register(Registries.ATTRIBUTE, Identifier(AC.MOD_ID, "spell_level"), SPELL_LEVEL)
        Registry.register(Registries.ATTRIBUTE, Identifier(AC.MOD_ID, "spell_cooldown"), SPELL_COOLDOWN)
        Registry.register(Registries.ATTRIBUTE, Identifier(AC.MOD_ID, "spell_mana_cost"), SPELL_MANA_COST)
        Registry.register(Registries.ATTRIBUTE, Identifier(AC.MOD_ID, "spell_damage"), SPELL_DAMAGE)
        Registry.register(Registries.ATTRIBUTE, Identifier(AC.MOD_ID, "spell_amplifier"), SPELL_AMPLIFIER)
        Registry.register(Registries.ATTRIBUTE, Identifier(AC.MOD_ID, "spell_duration"), SPELL_DURATION)
        Registry.register(Registries.ATTRIBUTE, Identifier(AC.MOD_ID, "spell_range"), SPELL_RANGE)
        Registry.register(Registries.ATTRIBUTE, Identifier(AC.MOD_ID, "enchantability"), ENCHANTABILITY)
    }

    private fun make(name: String, base: Double, min: Double, max: Double): EntityAttribute {
        return ClampedEntityAttribute(
            "attribute.name.generic." + AC.MOD_ID + "." + name,
            base,
            min,
            max
        ).setTracked(true)
    }
}