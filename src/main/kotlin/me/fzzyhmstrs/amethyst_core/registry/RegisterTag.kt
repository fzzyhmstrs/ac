package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier

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
}