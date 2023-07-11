package me.fzzyhmstrs.amethyst_core.modifier

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.scepter.SpellType
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import java.util.function.Supplier

@Suppress("MemberVisibilityCanBePrivate")
    //alternative version with the AugmentEffect directly included

/**
 * default and blank properties for initialization etc.
 */
object ModifierDefaults{
    val BLANK_ID = Identifier(AC.MOD_ID,"blank_modifier")
    val BLANK_AUG_MOD = AugmentModifier(BLANK_ID)
    val BLANK_EFFECT = AugmentEffect()
    val BLANK_XP_MOD = XpModifiers()
    val BLANK_COMPILED_DATA = AbstractModifier.CompiledModifiers(arrayListOf(), BLANK_AUG_MOD)

}

/**
 * container for [AugmentModifier] scepter experience modification.
 *
 * By default, [Scepters][me.fzzyhmstrs.amethyst_core.item.AugmentScepterItem] increment the relevant [SpellType][me.fzzyhmstrs.amethyst_core.scepter.SpellType] statistic by 1 per spell cast. The three constructor parameters modify that 1 by the stored value.
 *
 * For example, if [furyXpMod] stores a value of 3, Fury spells will gain a scepter 4 Fury experience per spell cast rather than 1.
 */
data class XpModifiers(var furyXpMod: Int = 0, var witXpMod: Int = 0, var graceXpMod: Int = 0){
    fun plus(xpMods: XpModifiers?){
        if(xpMods == null) return
        this.furyXpMod += xpMods.furyXpMod
        this.witXpMod += xpMods.witXpMod
        this.graceXpMod += xpMods.graceXpMod
    }
    fun getMod(spellKey: String): Int{
        return when(spellKey){
            SpellType.FURY.name ->{this.furyXpMod}
            SpellType.WIT.name ->{this.witXpMod}
            SpellType.GRACE.name ->{this.graceXpMod}
            else -> 0
        }
    }
    fun withFuryMod(furyXpMod: Int = 0): XpModifiers {
        return this.copy(furyXpMod = furyXpMod)
    }
    fun withWitMod(witXpMod: Int = 0): XpModifiers {
        return this.copy(witXpMod = witXpMod)
    }
    fun withGraceMod(graceXpMod: Int = 0): XpModifiers {
        return this.copy(graceXpMod = graceXpMod)
    }
}

fun MutableList<Text>.addLang(key: String, args: Array<Any> = arrayOf(), vararg conditions: Supplier<Boolean>){
    var bl = true
    for (condition in conditions){
        if (!condition.get()){
            bl = false
            break
        }
    }
    if (bl){
        this.add(AcText.translatable(key, args))
    } else {
        this.add(AcText.translatable(key, args).formatted(Formatting.OBFUSCATED))
    }

}

fun MutableList<Text>.addLang(key: String, vararg conditions: Supplier<Boolean>){
    var bl = true
    for (condition in conditions){
        if (!condition.get()){
            bl = false
            break
        }
    }
    if (bl){
        this.add(AcText.translatable(key))
    } else {
        this.add(AcText.translatable(key).formatted(Formatting.OBFUSCATED))
    }
}



