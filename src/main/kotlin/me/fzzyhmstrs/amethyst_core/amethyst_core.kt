package me.fzzyhmstrs.amethyst_core

import me.fzzyhmstrs.amethyst_core.item_util.AbstractAugmentBookItem
import me.fzzyhmstrs.amethyst_core.compat.gear_core.GcChecker
import me.fzzyhmstrs.amethyst_core.registry.*
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.PlaceItemAugment
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.api.ModInitializer
import net.minecraft.enchantment.Enchantment
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import kotlin.random.Random


object AC: ModInitializer {
    const val MOD_ID = "amethyst_core"
    val acRandom = Random(System.currentTimeMillis())
    val fallbackId = Identifier("vanishing_curse")

    override fun onInitialize() {
        RegisterAttribute.registerAll()
        RegisterBaseEntity.registerAll()
        RegisterTag.registerAll()
        ModifierRegistry.registerAll()
        GcChecker.registerProcessor()
        ScepterHelper.registerServer()
        AbstractAugmentBookItem.registerServer()
    }

    fun Enchantment.isIn(tag: TagKey<Enchantment>): Boolean{
        val entry = Registries.ENCHANTMENT.getEntry(this)
        return entry.isIn(tag)
    }
}

object ACC: ClientModInitializer {
    val acRandom = Random(System.currentTimeMillis())

    override fun onInitializeClient() {
        RegisterBaseRenderer.registerAll()
        PlaceItemAugment.registerClient()
        AbstractAugmentBookItem.registerClient()
    }
}