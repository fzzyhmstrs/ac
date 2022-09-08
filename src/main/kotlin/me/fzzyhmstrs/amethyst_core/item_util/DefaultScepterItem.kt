package me.fzzyhmstrs.amethyst_core.item_util

import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.LinkedHashMultimap
import com.google.common.collect.Multimap
import me.fzzyhmstrs.amethyst_core.coding_util.PlayerParticles.scepterParticlePos
import me.fzzyhmstrs.amethyst_core.item_util.interfaces.ParticleEmitting
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterToolMaterial
import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import net.fabricmc.api.EnvType
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.attribute.EntityAttribute
import net.minecraft.entity.attribute.EntityAttributeModifier
import net.minecraft.entity.attribute.EntityAttributes
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import net.minecraft.text.LiteralText
import net.minecraft.text.TranslatableText

/**
 * The default Amethyst Imbuement style scepter.
 *
 * For the most basic implementation, extend this and set the fallback ID; that's it. Define characteristics in the [ScepterToolMaterial] as with any tool, and register! You have your very own AI style scepter fully compatible with Scepter Augments and with all the functionality AIs scepters come with.
 *
 * For more in depth implementations, this scepter is [Modifiable][me.fzzyhmstrs.amethyst_core.item_util.interfaces.Modifiable] and [ParticleEmitting], with all the functionality those interfaces offer.
 */

@Suppress("SameParameterValue", "unused", "USELESS_IS_CHECK")
abstract class DefaultScepterItem(material: ScepterToolMaterial, settings: Settings):
    AugmentScepterItem(material,settings), ParticleEmitting{

    private val attributeModifiers: Multimap<EntityAttribute, EntityAttributeModifier>
    private val damage: Double

    init{
        if(FabricLoader.getInstance().environmentType == EnvType.CLIENT) {
            try {
                ParticleEmitting.registerParticleEmitter(smokePacketId) { client -> doSmoke(client) }
            } catch (e: Exception) {
                println("oops!")
            }
        }
        damage = material.attackDamage.toDouble()
        if (damage > 0.0) {
            val builder = ImmutableMultimap.builder<EntityAttribute, EntityAttributeModifier>()
            builder.put(
                EntityAttributes.GENERIC_ATTACK_DAMAGE,
                EntityAttributeModifier(
                    ATTACK_DAMAGE_MODIFIER_ID,
                    "Weapon modifier",
                    material.attackDamage.toDouble(),
                    EntityAttributeModifier.Operation.ADDITION
                )
            )
            builder.put(
                EntityAttributes.GENERIC_ATTACK_SPEED,
                EntityAttributeModifier(
                    ATTACK_SPEED_MODIFIER_ID,
                    "Weapon modifier",
                    material.getAttackSpeed(),
                    EntityAttributeModifier.Operation.ADDITION
                )
            )
            attributeModifiers = builder.build()
        } else {
            attributeModifiers = LinkedHashMultimap.create()
        }
    }

    override fun getAttributeModifiers(slot: EquipmentSlot): Multimap<EntityAttribute, EntityAttributeModifier> {
        if (slot == EquipmentSlot.MAINHAND && damage > 0) {
            return attributeModifiers
        }
        return super.getAttributeModifiers(slot)
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        super.appendTooltip(stack, world, tooltip, context)
        val nbt = stack.orCreateNbt
        val activeSpell = if (nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())) {
            val activeEnchantId = Nbt.readStringNbt(NbtKeys.ACTIVE_ENCHANT.str(), nbt)
            TranslatableText("enchantment.${Identifier(activeEnchantId).namespace}.${Identifier(activeEnchantId).path}")
        } else {
            TranslatableText("enchantment.amethyst_core.none")
        }
        tooltip.add(TranslatableText("scepter.active_spell").formatted(Formatting.GOLD).append(activeSpell.formatted(Formatting.GOLD)))
        val stats = ScepterHelper.getScepterStats(stack)
        val furyText = TranslatableText("scepter.fury.lvl").string + stats[0].toString() + TranslatableText("scepter.xp").string + ScepterHelper.xpToNextLevel(stats[3],stats[0]).toString()
        tooltip.add(LiteralText(furyText).formatted(SpellType.FURY.fmt()))
        val graceText = TranslatableText("scepter.grace.lvl").string + stats[1].toString() + TranslatableText("scepter.xp").string + ScepterHelper.xpToNextLevel(stats[4],stats[1]).toString()
        tooltip.add(LiteralText(graceText).formatted(SpellType.GRACE.fmt()))
        val witText = TranslatableText("scepter.wit.lvl").string + stats[2].toString() + TranslatableText("scepter.xp").string + ScepterHelper.xpToNextLevel(stats[5],stats[2]).toString()
        tooltip.add(LiteralText(witText).formatted(SpellType.WIT.fmt()))
        addModifierTooltip(stack, tooltip)
    }

    override fun resetCooldown(
        stack: ItemStack,
        world: World,
        user: PlayerEntity,
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
