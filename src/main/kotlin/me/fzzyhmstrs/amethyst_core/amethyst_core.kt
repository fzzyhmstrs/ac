package me.fzzyhmstrs.amethyst_core

import me.fzzyhmstrs.amethyst_core.advancement.FeatureCriteria
import me.fzzyhmstrs.amethyst_core.command.PairedSpellCommand
import me.fzzyhmstrs.amethyst_core.item.AbstractAugmentBookItem
import me.fzzyhmstrs.amethyst_core.modifier.GcChecker
import me.fzzyhmstrs.amethyst_core.registry.*
import me.fzzyhmstrs.amethyst_core.scepter.ScepterHelper
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.minecraft.client.MinecraftClient
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory
import kotlin.random.Random


object AC: ModInitializer {
    const val MOD_ID = "amethyst_core"
    val acRandom = Random(System.currentTimeMillis())
    val fallbackId = Identifier("vanishing_curse")
    val LOGGER = LoggerFactory.getLogger("amethyst_core")

    val TIER_1_SPELL_SCEPTERS: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier(MOD_ID,"tier_one_spell_scepters"))
    val TIER_2_SPELL_SCEPTERS: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier(MOD_ID,"tier_two_spell_scepters"))
    val TIER_3_SPELL_SCEPTERS: TagKey<Item> = TagKey.of(RegistryKeys.ITEM, Identifier(MOD_ID,"tier_three_spell_scepters"))

    override fun onInitialize() {
        RegisterAttribute.registerAll()
        RegisterBaseEntity.registerAll()
        ModifierRegistry.registerAll()
        BoostRegistry.registerAll()
        GcChecker.registerProcessor()
        ScepterHelper.registerServer()
        FeatureCriteria.registerServer()
        AbstractAugmentBookItem.registerServer()
        PairedSpellCommand.registerAll()
    }

    fun identity(path: String): Identifier{
        return Identifier(MOD_ID,path)
    }
}

object ACC: ClientModInitializer {

    fun getPlayer(): PlayerEntity?{
        return MinecraftClient.getInstance().player
    }

    val acRandom = Random(System.currentTimeMillis())

    override fun onInitializeClient() {
        RegisterBaseRenderer.registerAll()
        ScepterHelper.registerClient()
        AbstractAugmentBookItem.registerClient()
    }
}