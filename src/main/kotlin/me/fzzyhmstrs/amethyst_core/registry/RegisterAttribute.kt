package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.compat.spell_power.SpChecker
import me.fzzyhmstrs.amethyst_core.event.ModifyAugmentEffectsEvent
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute.SHIELDING
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.ClampedEntityAttribute
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.damage.DamageSource
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.tag.DamageTypeTags
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Identifier
import net.minecraft.util.math.random.Random

object RegisterAttribute {

    //spell level, acts as a multiplier. Value of 0.1 will add 10% to a spells effective level
    val SPELL_LEVEL: EntityAttribute = make("spell_level", 1.0, 0.0, 10.0)

    //spell cooldown, acts as a multiplier. A player with -0.1 spell cooldown has 10% increased casting speed (0.9 the normal cooldown)
    val SPELL_COOLDOWN: EntityAttribute = make("spell_cooldown", 1.0, -8.9, 1.99)

    //spell mana cost, acts as a multiplier. A player with -0.1 spell mana cost has 10% reduced mana costs
    val SPELL_MANA_COST: EntityAttribute = make("spell_mana_cost", 1.0, -8.9, 1.99)

    //spell damage, acts as a multiplier. A player with -10.0 spell damage has 10% reduced damage
    val SPELL_DAMAGE: EntityAttribute = make("spell_damage", 1.0, 0.0, 32.0)

    //spell amplifier, a flat addition. Any non-integer values of this attribute may not do anything.
    // A spell amplifier of 1.0 will add 1 to the status effect level/amplifier of applied effects
    val SPELL_AMPLIFIER: EntityAttribute = make("spell_amplifier", 0.0, 0.0, 32.0)

    //spell duration, acts as a multiplier. A player with 0.1 spell duration has 10% increased duration of spell effects
    val SPELL_DURATION: EntityAttribute = make("spell_duration", 1.0, 0.0, 32.0)

    //spell range, acts as a multiplier. A player with -0.1 spell damage has 10% reduced damage
    val SPELL_RANGE: EntityAttribute = make("spell_range", 1.0, 0.0, 5.0)

    //spell experience gain, acts as a multiplier. 1.0 is no change, 0.0 is no experience gain.
    val SPELL_EXPERIENCE: EntityAttribute = make("spell_experience", 1.0, 0.0, 32.0)

    //spell critical chance, a float chance. 0.0 is no chance, 1.0 is every hit
    val SPELL_CRITICAL_CHANCE: EntityAttribute = make("spell_critical_chance", 0.0, 0.0, 1.0)

    //spell critical multiplier, acts as a multiplier. 1.5 is 1.5x normal damage, 1.0 is no extra damage
    val SPELL_CRITICAL_MULTIPLIER: EntityAttribute = make("spell_critical_multiplier", 1.5, 1.0, 10.0)

    //damage multiplication, acts as a multiplier. Value of 0.1 will add 10% to the damage inflicted on the affected entity Max value will be a 3200x
    val DAMAGE_MULTIPLICATION: EntityAttribute = make("damage_multiplication", 1.0, 0.0, 32.0)

    //Player experience bonus gained. every 1.0 in this attribute will be an extra 1 XP gained whenever a player entity gains XP.
    val PLAYER_EXPERIENCE: EntityAttribute = make("player_experience", 0.0, 0.0, 10.0)

    //shielding represents a fractional chance that damage is entirely blocked
    val SHIELDING: EntityAttribute = make("shielding", 0.0, 0.0, 1.0)

    //magic resistance provides protection from magic damage based on the fraction of 1 resistance present. 1 resistance is total protection
    val MAGIC_RESISTANCE: EntityAttribute = make("magic_resistance", 0.0, 0.0, 1.0)

    fun registerAll(){
        ModifyAugmentEffectsEvent.EVENT.register{ _, user, _, effects, spell ->
                val crit = AC.acRandom.nextFloat() < user.getAttributeValue(SPELL_CRITICAL_CHANCE)
                if (crit){
                    user.world.playSound(null,user.blockPos, SoundEvents.ENTITY_PLAYER_ATTACK_CRIT, SoundCategory.PLAYERS, 0.5f,1.0f)
                    val multiplier = user.getAttributeValue(SPELL_CRITICAL_MULTIPLIER)
                    effects.addDamage(0f,0f,((multiplier - 1.0) * 100f).toFloat())
                }
                if (SpChecker.spellPowerLoaded){
                    val multiplier = SpChecker.getModFromSpell(user,spell)
                    if (multiplier != 0.0) {
                        effects.addDamage(0f, 0f, multiplier.toFloat())
                        effects.addAmplifier(0, 0, multiplier.toInt())
                        effects.addDuration(0, 0, multiplier.toInt())
                        effects.addRange(0.0, 0.0, multiplier)
                    }
                }

            }
    }

    fun damageIsBlocked(random: Random, entity: LivingEntity, damageSource: DamageSource): Boolean{
        if (damageSource.isIn(DamageTypeTags.BYPASSES_INVULNERABILITY)
            || damageSource.isIn(DamageTypeTags.BYPASSES_EFFECTS)
            || damageSource.isIn(DamageTypeTags.IS_FALL)
            ) return false
        val chance = entity.getAttributeValue(SHIELDING)
        return random.nextFloat() < chance
    }

    private fun make(name: String, base: Double, min: Double, max: Double): EntityAttribute {
        return Registry.register(Registries.ATTRIBUTE, Identifier(AC.MOD_ID, name),
        ClampedEntityAttribute("attribute.name.generic." + AC.MOD_ID + "." + name, base, min, max).setTracked(true))
    }
}
