package me.fzzyhmstrs.amethyst_core.modifier_util

import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import net.minecraft.entity.LivingEntity
import net.minecraft.util.Identifier
import net.minecraft.util.StringIdentifiable
import java.util.function.Consumer

@Suppress("MemberVisibilityCanBePrivate", "CanBeParameter")
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
 * By default, [Scepters][me.fzzyhmstrs.amethyst_core.item_util.AugmentScepterItem] increment the relevant [SpellType][me.fzzyhmstrs.amethyst_core.scepter_util.SpellType] statistic by 1 per spell cast. The three constructor parameters modify that 1 by the stored value.
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

    companion object{
        val CODEC: Codec<XpModifiers> = RecordCodecBuilder.create { instance: RecordCodecBuilder.Instance<XpModifiers> ->
            instance.group(
                Codec.INT.optionalFieldOf("furyXpMod", 0).forGetter { xpModifiers -> xpModifiers.furyXpMod },
                Codec.INT.optionalFieldOf("witXpMod", 0).forGetter { xpModifiers -> xpModifiers.witXpMod },
                Codec.INT.optionalFieldOf("graceXpMod", 0).forGetter { xpModifiers -> xpModifiers.graceXpMod }
            ).apply(instance){f, w, g -> XpModifiers(f,w,g) } }
    }
}

/**
 * a simple container that holds a consumer and a type notation for sorting.
 */
data class AugmentConsumer(val consumer: Consumer<List<LivingEntity>>, val type: Type) {

    companion object{
    }

    enum class Type: StringIdentifiable {
        HARMFUL,
        BENEFICIAL,
        AUTOMATIC;

        override fun asString(): String {
            return this.name
        }

        companion object{
            val CODEC: Codec<Type> = StringIdentifiable.createCodec(Type::values)
        }
    }
}