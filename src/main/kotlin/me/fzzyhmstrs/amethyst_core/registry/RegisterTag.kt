package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

@Suppress("unused", "MemberVisibilityCanBePrivate")
object RegisterTag {
    val FIRE_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"fire_augments"))
    val ICE_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"ice_augments"))
    val LIGHTNING_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"lightning_augments"))
    val ELEMENTAL_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"elemental_augments"))
    val ARCANE_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"arcane_augments"))
    val HEALER_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"healer_augments"))
    val EFFECTS_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"effects_augments"))
    val BUILDER_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"builder_augments"))
    val TRAVELER_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"traveler_augments"))
    val BOLT_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"bolt_augments"))
    val SOUL_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"soul_augments"))

    val TIER_1_SPELL_SCEPTERS = FzzyPort.ITEM.tagOf(Identifier(AC.MOD_ID,"tier_one_spell_scepters"))
    val TIER_2_SPELL_SCEPTERS = FzzyPort.ITEM.tagOf(Identifier(AC.MOD_ID,"tier_two_spell_scepters"))
    val TIER_3_SPELL_SCEPTERS = FzzyPort.ITEM.tagOf(Identifier(AC.MOD_ID,"tier_three_spell_scepters"))

    fun registerAll(){}

    fun getStyleFromSpell(spell: ScepterAugment): TagStyle{
        return if (FzzyPort.ENCHANTMENT.isInTag(spell,LIGHTNING_AUGMENTS)){
            TagStyle.LIGHTNING
        } else if (FzzyPort.ENCHANTMENT.isInTag(spell,ICE_AUGMENTS)){
            TagStyle.ICE
        } else if (FzzyPort.ENCHANTMENT.isInTag(spell,FIRE_AUGMENTS)){
            TagStyle.FIRE
        } else if (FzzyPort.ENCHANTMENT.isInTag(spell,HEALER_AUGMENTS)){
            TagStyle.HEALER
        } else if (FzzyPort.ENCHANTMENT.isInTag(spell,TRAVELER_AUGMENTS)){
            TagStyle.TRAVELER
        } else if (FzzyPort.ENCHANTMENT.isInTag(spell,ARCANE_AUGMENTS)){
            TagStyle.ARCANE
        } else if (FzzyPort.ENCHANTMENT.isInTag(spell,BUILDER_AUGMENTS)){
            TagStyle.BUILDER
        } else if (FzzyPort.ENCHANTMENT.isInTag(spell,SOUL_AUGMENTS)){
            TagStyle.SOUL
        } else {
            TagStyle.EMPTY
        }
    }

    enum class TagStyle(val x: Int, val y: Int, val color: Formatting){
        BUILDER(177,1,Formatting.GRAY),
        TRAVELER(217,1,Formatting.GRAY),
        FIRE(177,41,Formatting.RED),
        HEALER(217,41,Formatting.YELLOW),
        ICE(177,81,Formatting.BLUE),
        ARCANE(217,81,Formatting.LIGHT_PURPLE),
        LIGHTNING(177,121,Formatting.GOLD),
        SOUL(217,121,Formatting.DARK_AQUA),
        EMPTY(137,1,Formatting.GRAY)
    }
}