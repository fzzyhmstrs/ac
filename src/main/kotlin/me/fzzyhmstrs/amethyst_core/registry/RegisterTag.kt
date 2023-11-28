package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.spell_power.api.MagicSchool

object RegisterTag {
    val FIRE_AUGMENTS: TagKey<Enchantment> = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier(AC.MOD_ID,"fire_augments"))
    val ICE_AUGMENTS: TagKey<Enchantment> = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier(AC.MOD_ID,"ice_augments"))
    val LIGHTNING_AUGMENTS: TagKey<Enchantment> = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier(AC.MOD_ID,"lightning_augments"))
    val ELEMENTAL_AUGMENTS: TagKey<Enchantment> = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier(AC.MOD_ID,"elemental_augments"))
    val ARCANE_AUGMENTS: TagKey<Enchantment> = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier(AC.MOD_ID,"arcane_augments"))
    val HEALER_AUGMENTS: TagKey<Enchantment> = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier(AC.MOD_ID,"healer_augments"))
    val EFFECTS_AUGMENTS: TagKey<Enchantment> = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier(AC.MOD_ID,"effects_augments"))
    val BUILDER_AUGMENTS: TagKey<Enchantment> = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier(AC.MOD_ID,"builder_augments"))
    val TRAVELER_AUGMENTS: TagKey<Enchantment> = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier(AC.MOD_ID,"traveler_augments"))
    val BOLT_AUGMENTS: TagKey<Enchantment> = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier(AC.MOD_ID,"bolt_augments"))
    val SOUL_AUGMENTS: TagKey<Enchantment> = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier(AC.MOD_ID,"soul_augments"))

    val TIER_1_SPELL_SCEPTERS = TagKey.of(RegistryKeys.ITEM, Identifier(AC.MOD_ID,"tier_one_spell_scepters"))
    val TIER_2_SPELL_SCEPTERS = TagKey.of(RegistryKeys.ITEM, Identifier(AC.MOD_ID,"tier_two_spell_scepters"))
    val TIER_3_SPELL_SCEPTERS = TagKey.of(RegistryKeys.ITEM, Identifier(AC.MOD_ID,"tier_three_spell_scepters"))

    fun registerAll(){}

    fun getStyleFromSpell(spell: ScepterAugment): TagStyle{
        return if (spell.isIn(LIGHTNING_AUGMENTS)){
            TagStyle.LIGHTNING
        } else if (spell.isIn(ICE_AUGMENTS)){
            TagStyle.ICE
        } else if (spell.isIn(FIRE_AUGMENTS)){
            TagStyle.FIRE
        } else if (spell.isIn(HEALER_AUGMENTS)){
            TagStyle.HEALER
        } else if (spell.isIn(TRAVELER_AUGMENTS)){
            TagStyle.TRAVELER
        } else if (spell.isIn(ARCANE_AUGMENTS)){
            TagStyle.ARCANE
        } else if (spell.isIn(BUILDER_AUGMENTS)){
            TagStyle.BUILDER
        } else if (spell.isIn(SOUL_AUGMENTS)){
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