package me.fzzyhmstrs.amethyst_core.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.fzzyhmstrs.amethyst_core.scepter.augments.ScepterAugment
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import net.minecraft.command.CommandSource
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class AllSpellsArgumentType(): ArgumentType<Identifier> {

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
        val list = Registries.ENCHANTMENT.ids.stream().filter { id -> Registries.ENCHANTMENT.get(id) is ScepterAugment }.toList()
        return CommandSource.suggestIdentifiers(list,builder)
    }
}
