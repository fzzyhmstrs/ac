package me.fzzyhmstrs.amethyst_core.item_util

import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterToolMaterial
import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.AugmentHelper
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.coding_util.PlayerParticlesV2.scepterParticlePos
import me.fzzyhmstrs.fzzy_core.item_util.interfaces.ParticleEmitting
import me.fzzyhmstrs.fzzy_core.nbt_util.NbtKeys
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

/**
 * The default Amethyst Imbuement style scepter.
 *
 * For the most basic implementation, extend this and set the fallback ID; that's it. Define characteristics in the [ScepterToolMaterial] as with any tool, and register! You have your very own AI style scepter fully compatible with Scepter Augments and with all the functionality AIs scepters come with.
 *
 * For more in depth implementations, this scepter is [Modifiable][me.fzzyhmstrs.fzzy_core.interfaces.Modifiable] and [ParticleEmitting], with all the functionality those interfaces offer.
 */

@Suppress("SameParameterValue", "unused")
abstract class DefaultScepterItem(material: ScepterToolMaterial, settings: Settings):
    AugmentScepterItem(material,settings), ParticleEmitting{

    init{
        if(FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            try {
                ParticleEmitting.registerParticleEmitter(smokePacketId) { client -> doSmoke(client) }
            } catch (e: Exception) {
                println("oops!")
            }
        }
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        val nbt = stack.orCreateNbt
        val activeSpell = if (!nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())) {
            AcText.translatable("enchantment.amethyst_core.none")
        } else if (nbt.getString(NbtKeys.ACTIVE_ENCHANT.str()) == "none") {
            AcText.translatable("enchantment.amethyst_core.none")
        } else {
            val activeEnchantId = nbt.getString(NbtKeys.ACTIVE_ENCHANT.str())
            val text = AcText.translatable("enchantment.${Identifier(activeEnchantId).namespace}.${Identifier(activeEnchantId).path}")
            if(!AugmentHelper.getAugmentEnabled(activeEnchantId)){
                text.formatted(Formatting.DARK_RED).formatted(Formatting.STRIKETHROUGH)
            } else {
                text.formatted(Formatting.GOLD)
            }
        }

        tooltip.add(AcText.translatable("scepter.active_spell").formatted(Formatting.GOLD).append(activeSpell))
        val stats = ScepterHelper.getScepterStats(stack)
        val furyText = AcText.translatable("scepter.fury.lvl").string + stats[0].toString() + AcText.translatable("scepter.xp").string + ScepterHelper.xpToNextLevel(stats[3],stats[0]).toString()
        tooltip.add(AcText.literal(furyText).formatted(SpellType.FURY.fmt()))
        val graceText = AcText.translatable("scepter.grace.lvl").string + stats[1].toString() + AcText.translatable("scepter.xp").string + ScepterHelper.xpToNextLevel(stats[4],stats[1]).toString()
        tooltip.add(AcText.literal(graceText).formatted(SpellType.GRACE.fmt()))
        val witText = AcText.translatable("scepter.wit.lvl").string + stats[2].toString() + AcText.translatable("scepter.xp").string + ScepterHelper.xpToNextLevel(stats[5],stats[2]).toString()
        tooltip.add(AcText.literal(witText).formatted(SpellType.WIT.fmt()))
    }

    override fun resetCooldown(
        stack: ItemStack,
        world: World,
        user: LivingEntity,
        activeEnchant: String
    ): TypedActionResult<ItemStack> {
        if (user is ServerPlayerEntity) {
            sendParticlePacket(user, smokePacketId)
        }
        return super.resetCooldown(stack, world, user, activeEnchant)
    }

    companion object{
        private const val smokePacketId = "scepter_smoke_emitter"

        private fun doSmoke(client: MinecraftClient){
            val world = client.world
            val entity = client.player
            if (world != null && entity != null){
                doSmoke(world,client,entity)
            }
        }

        private fun doSmoke(world: World, client: MinecraftClient, user: LivingEntity){
            val particlePos = scepterParticlePos(client, user)
            world.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE,particlePos.x,particlePos.y,particlePos.z,user.velocity.x,user.velocity.y + 0.5,user.velocity.z)
        }
    }
}
