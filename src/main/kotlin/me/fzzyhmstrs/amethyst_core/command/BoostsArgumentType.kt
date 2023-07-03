package me.fzzyhmstrs.amethyst_core.command

import com.mojang.brigadier.StringReader
import com.mojang.brigadier.arguments.ArgumentType
import com.mojang.brigadier.context.CommandContext
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType
import com.mojang.brigadier.suggestion.Suggestions
import com.mojang.brigadier.suggestion.SuggestionsBuilder
import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.registry.BoostRegistry
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import net.minecraft.command.CommandSource
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import java.util.concurrent.CompletableFuture

class BoostsArgumentType(): ArgumentType<Identifier> {

    private val invalidIdException = DynamicCommandExceptionType { id -> AcText.translatable("commands.amethyst_core.failed.invalid_boost_id",id)}

    override fun parse(reader: StringReader): Identifier {
        val i = reader.cursor
        val identifier = Identifier.fromCommandInput(reader)
        return if (BoostRegistry.BOOSTS.containsId(identifier)){
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
        val spellId = context.getArgument("current_spell",Identifier::class.java)
        val spell = Registries.ENCHANTMENT.get(spellId) ?: return CommandSource.suggestIdentifiers(listOf(),builder)
        if (spell !is ScepterAugment) return CommandSource.suggestIdentifiers(listOf(),builder)
        val list = BoostRegistry.BOOSTS.stream().filter { boost -> boost.canAccept(spell) }.map { boost -> boost.id }
        return CommandSource.suggestIdentifiers(list,builder)
    }
}
