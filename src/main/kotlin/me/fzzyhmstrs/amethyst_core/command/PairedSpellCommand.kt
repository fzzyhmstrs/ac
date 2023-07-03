package me.fzzyhmstrs.amethyst_core.command

import com.mojang.brigadier.CommandDispatcher
import com.mojang.brigadier.context.CommandContext
import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import net.fabricmc.fabric.api.command.v2.ArgumentTypeRegistry
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback
import net.minecraft.command.argument.serialize.ConstantArgumentSerializer
import net.minecraft.registry.Registries
import net.minecraft.server.command.CommandManager
import net.minecraft.server.command.ServerCommandSource
import net.minecraft.text.Text
import net.minecraft.util.Identifier
import java.util.function.Supplier

object PairedSpellCommand {

    fun registerAll(){
        ArgumentTypeRegistry.registerArgumentType(
            Identifier(AC.MOD_ID,"current_spell_argument"),
            CurrentSpellsArgumentType::class.java, ConstantArgumentSerializer.of(
                Supplier {
                    CurrentSpellsArgumentType()
                }
            )
        )
        ArgumentTypeRegistry.registerArgumentType(
            Identifier(AC.MOD_ID,"all_spell_argument"),
            AllSpellsArgumentType::class.java, ConstantArgumentSerializer.of(
                Supplier {
                    AllSpellsArgumentType()
                }
            )
        )
        ArgumentTypeRegistry.registerArgumentType(
            Identifier(AC.MOD_ID,"boost_argument"),
            BoostsArgumentType::class.java, ConstantArgumentSerializer.of(
                Supplier {
                    BoostsArgumentType()
                }
            )
        )
        CommandRegistrationCallback.EVENT.register { commandDispatcher, _, _ -> register(commandDispatcher)}
    }


    private fun register(commandDispatcher: CommandDispatcher<ServerCommandSource>){
        commandDispatcher.register(
            CommandManager.literal("spell_pair")
                .requires { source -> source.hasPermissionLevel(2) }
                .then(CommandManager.argument("current_spell",CurrentSpellsArgumentType())
                    .then(CommandManager.argument("paired_spell",AllSpellsArgumentType())
                        .then(CommandManager.argument("boost", BoostsArgumentType())
                            .then(CommandManager.literal("description")
                                .executes {context -> executeDescribed(context) }
                            )
                            .executes {context -> executeFull(context)}
                        )
                        .executes {context -> executePaired(context) }
                    )
                    .then(CommandManager.argument("boost", BoostsArgumentType())
                        .executes {context -> executeBoost(context) }
                    )
                )
        )
    }

    private fun error(context: CommandContext<ServerCommandSource>, key: String): Int{
        context.source.sendError(AcText.translatable(key))
        return 0
    }

    private fun executeDescribed(context: CommandContext<ServerCommandSource>): Int{
        val spellId = context.getArgument("current_spell", Identifier::class.java)
        val spell = Registries.ENCHANTMENT.get(spellId) ?: return error(context,"commands.amethyst_core.failed.not_a_spell")
        if (spell !is ScepterAugment) return error(context,"commands.amethyst_core.failed.not_a_spell")
        val pairedId = context.getArgument("paired_spell", Identifier::class.java)
        val paired = Registries.ENCHANTMENT.get(pairedId)
        val pairedSpell = if (paired is ScepterAugment) paired else null
        val boostId = context.getArgument("boost", Identifier::class.java)
        val player = context.source.player ?: return error(context,"commands.amethyst_core.failed.no_player")
        val stack1 = player.mainHandStack
        return if (stack1.isEmpty){
            context.source.sendError(AcText.translatable("commands.gearifiers.failed.no_stacks"))
            0
        } else {
            val pairedAugments = AugmentHelper.getOrCreatePairedAugments(spellId.toString(), pairedId?.toString(), boostId?.toString(), spell, pairedSpell)
            val list: List<Text> = pairedAugments.provideDescription()
            for(desc in list){
                player.sendMessage(desc)
            }
            AugmentHelper.writePairedAugments(stack1,pairedAugments)
            1
        }
    }
    private fun executeFull(context: CommandContext<ServerCommandSource>): Int {
        val spellId = context.getArgument("current_spell", Identifier::class.java)
        val spell = Registries.ENCHANTMENT.get(spellId) ?: return error(context,"commands.amethyst_core.failed.not_a_spell")
        if (spell !is ScepterAugment) return error(context,"commands.amethyst_core.failed.not_a_spell")
        val pairedId = context.getArgument("paired_spell", Identifier::class.java)
        val paired = Registries.ENCHANTMENT.get(pairedId)
        val pairedSpell = if (paired is ScepterAugment) paired else null
        val boostId = context.getArgument("boost", Identifier::class.java)
        return checkAndApplyPairedSpellsToStack(context,spell,spellId,pairedSpell, pairedId, boostId)
    }

    private fun executePaired(context: CommandContext<ServerCommandSource>): Int{
        val spellId = context.getArgument("current_spell", Identifier::class.java)
        val spell = Registries.ENCHANTMENT.get(spellId) ?: return error(context,"commands.amethyst_core.failed.not_a_spell")
        if (spell !is ScepterAugment) return error(context,"commands.amethyst_core.failed.not_a_spell")
        val pairedId = context.getArgument("paired_spell",Identifier::class.java)
        val paired = Registries.ENCHANTMENT.get(pairedId)
        val pairedSpell = if (paired is ScepterAugment) paired else null
        return checkAndApplyPairedSpellsToStack(context,spell,spellId,pairedSpell, pairedId, null)
    }

    private fun executeBoost(context: CommandContext<ServerCommandSource>): Int{
        val spellId = context.getArgument("current_spell", Identifier::class.java)
        val spell = Registries.ENCHANTMENT.get(spellId) ?: return error(context,"commands.amethyst_core.failed.not_a_spell")
        if (spell !is ScepterAugment) return error(context,"commands.amethyst_core.failed.not_a_spell")
        val boostId = context.getArgument("boost",Identifier::class.java)
        return checkAndApplyPairedSpellsToStack(context,spell,spellId,null, null, boostId)
    }

    private fun checkAndApplyPairedSpellsToStack(context: CommandContext<ServerCommandSource>, spell: ScepterAugment, spellId: Identifier, pairedSpell: ScepterAugment?, pairedSpellId: Identifier?, boostId: Identifier?): Int{
        val player = context.source.player ?: return error(context,"commands.amethyst_core.failed.no_player")
        val stack1 = player.mainHandStack
        return if (stack1.isEmpty){
            context.source.sendError(AcText.translatable("commands.gearifiers.failed.no_stacks"))
            0
        } else {
            val pairedAugments = AugmentHelper.getOrCreatePairedAugments(spellId.toString(), pairedSpellId?.toString(), boostId?.toString(), spell, pairedSpell)
            AugmentHelper.writePairedAugments(stack1,pairedAugments)
            1
        }
    }

}