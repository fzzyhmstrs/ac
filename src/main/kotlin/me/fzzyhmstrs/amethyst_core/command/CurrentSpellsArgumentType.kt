package me.fzzyhmstrs.amethyst_core.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.fzzyhmstrs.amethyst_core.ACC
import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import net.minecraft.client.network.ClientCommandSource
import net.minecraft.command.CommandSource
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.registry.Registries
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class CurrentSpellsArgumentType(): ArgumentType<Identifier> {

    private val invalidIdException = DynamicCommandExceptionType { id -> AcText.translatable("commands.amethyst_core.failed.invalid_id",id)}

    override fun parse(reader: StringReader): Identifier {
        val i = reader.cursor
        val identifier = Identifier.fromCommandInput(reader)
        return if (Registries.ENCHANTMENT.get(identifier) is ScepterAugment){
            identifier
        } else {
            reader.cursor = i
            throw invalidIdException.create(identifier)
        }

    }

    override fun <S> listSuggestions(
        context: CommandContext<S>,
        builder: SuggestionsBuilder
    ): CompletableFuture<Suggestions> {
        val source = context.source
        if (source is ServerCommandSource){
            val player = source.player
            if (player != null){
                val spellIds = if (!player.mainHandStack.isEmpty){
                    EnchantmentHelper.get(player.mainHandStack).keys.stream().filter {enchant -> enchant is ScepterAugment }.map { enchant -> Registries.ENCHANTMENT.getId(enchant) }.filter { id -> id != null } .toList()
                } else {
                    mutableListOf()
                }
                if (spellIds.isNotEmpty()){
                    return CommandSource.suggestIdentifiers(spellIds,builder)
                }
            }
        } else if (source is ClientCommandSource){
            val player = ACC.getPlayer()
            if (player != null){
                val spellIds = if (!player.mainHandStack.isEmpty){
                    EnchantmentHelper.get(player.mainHandStack).keys.stream().filter {enchant -> enchant is ScepterAugment }.map { enchant -> Registries.ENCHANTMENT.getId(enchant) }.filter { id -> id != null } .toList()
                } else {
                    mutableListOf()
                }
                return CommandSource.suggestIdentifiers(spellIds,builder)
            }
        }
        return CommandSource.suggestIdentifiers(listOf(),builder)
    }
}
