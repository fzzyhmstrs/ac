package me.fzzyhmstrs.amethyst_core

import me.fzzyhmstrs.amethyst_core.item_util.AbstractAugmentBookItem
import me.fzzyhmstrs.amethyst_core.modifier_util.GcChecker
import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute
import me.fzzyhmstrs.amethyst_core.registry.RegisterBaseEntity
import me.fzzyhmstrs.amethyst_core.registry.RegisterBaseRenderer
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import kotlin.random.Random


object AC: ModInitializer {
    const val MOD_ID = "amethyst_core"
    val acRandom = Random(System.currentTimeMillis())
    val fallbackId = Identifier("vanishing_curse")

    val TIER_1_SPELL_SCEPTERS = TagKey.of(RegistryKeys.ITEM, Identifier(AC.MOD_ID,"tier_one_spell_scepters"))
    val TIER_2_SPELL_SCEPTERS = TagKey.of(RegistryKeys.ITEM, Identifier(AC.MOD_ID,"tier_two_spell_scepters"))
    val TIER_3_SPELL_SCEPTERS = TagKey.of(RegistryKeys.ITEM, Identifier(AC.MOD_ID,"tier_three_spell_scepters"))

    override fun onInitialize() {
        RegisterAttribute.registerAll()
        RegisterBaseEntity.registerAll()
        ModifierRegistry.registerAll()
        GcChecker.registerProcessor()
        ScepterHelper.registerServer()
        AbstractAugmentBookItem.registerServer()
    }
}

object ACC: ClientModInitializer {
    val acRandom = Random(System.currentTimeMillis())

    override fun onInitializeClient() {
        RegisterBaseRenderer.registerAll()
        ScepterHelper.registerClient()
        AbstractAugmentBookItem.registerClient()
    }
}