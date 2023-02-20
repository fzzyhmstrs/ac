package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.interfaces.SyncedRandomProviding
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.scepter_util.LoreTier
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import me.fzzyhmstrs.fzzy_core.coding_util.Dustbin
import me.fzzyhmstrs.fzzy_core.item_util.CustomFlavorItem
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.client.network.AbstractClientPlayerEntity
import net.minecraft.client.network.ClientPlayNetworkHandler
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.item.Items
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import kotlin.random.Random

/**
 * used to define the augment books like Amethyst Imbuements Book of Lore. For all Augment tiers except Extreme Tier, there is a pre-existing book in Amethyst Imbuement.
 *
 * If you don't want to rely on Amethyst Imbuement, implementing your own augment books is as simple as extending this class and overriding [loreTier] with the proper tier.
 */
abstract class AbstractAugmentBookItem(settings: Settings) : CustomFlavorItem(settings) {

    abstract val loreTier: LoreTier

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        val nbt = stack.orCreateNbt
        if (nbt.contains(NbtKeys.LORE_KEY.str())){
            val bola = Identifier(nbt.getString(NbtKeys.LORE_KEY.str())).toString()
            tooltip.add(AcText.translatable("lore_book.augment",AcText.translatable("enchantment.${Identifier(bola).namespace}.${Identifier(bola).path}")).formatted(Formatting.GOLD))
            //tooltip.add(AcText.translatable("lore_book.augment").formatted(Formatting.GOLD).append(AcText.translatable("enchantment.${Identifier(bola).namespace}.${Identifier(bola).path}").formatted(Formatting.GOLD)))
            tooltip.add(AcText.translatable("enchantment.${Identifier(bola).namespace}.${Identifier(bola).path}.desc").formatted(Formatting.WHITE))
            val type = AugmentHelper.getAugmentType(bola)
            if (type == SpellType.NULL){
                tooltip.add(AcText.translatable("lore_book.${type.str()}").formatted(type.fmt()))
            } else {
                val lvl = AugmentHelper.getAugmentMinLvl(bola)
                tooltip.add(AcText.translatable("lore_book.${type.str()}",lvl.toString()).formatted(type.fmt()))
                //tooltip.add(AcText.translatable("lore_book.${type.str()}").formatted(type.fmt()).append(AcText.literal(lvl.toString())))
            }
            val item = AugmentHelper.getAugmentItem(bola)
            if (item != Items.AIR) {
                val itemText = item.name.copyContentOnly().formatted(Formatting.WHITE)
                tooltip.add(AcText.translatable("lore_book.key_item",itemText).formatted(Formatting.WHITE))
                //tooltip.add(AcText.translatable("lore_book.key_item").formatted(Formatting.WHITE).append(itemText))
            }
            val xpLevels = AugmentHelper.getAugmentImbueLevel(bola)
            tooltip.add(AcText.translatable("lore_book.xp_level", xpLevels.toString()).formatted(Formatting.WHITE))
            //tooltip.add(AcText.translatable("lore_book.xp_level").formatted(Formatting.WHITE).append(xpLevels.toString()))
            val cooldown = AugmentHelper.getAugmentCooldown(bola).toFloat() / 20.0F
            tooltip.add(AcText.translatable("lore_book.cooldown",cooldown.toString()).formatted(Formatting.WHITE))
            //tooltip.add(AcText.translatable("lore_book.cooldown").formatted(Formatting.WHITE).append(AcText.literal(cooldown.toString())).append(AcText.translatable("lore_book.cooldown1").formatted(Formatting.WHITE)))
            val manaCost = AugmentHelper.getAugmentManaCost(bola)
            tooltip.add(AcText.translatable("lore_book.mana_cost",manaCost.toString()).formatted(Formatting.WHITE))
            //tooltip.add(AcText.translatable("lore_book.mana_cost").formatted(Formatting.WHITE).append(AcText.literal(manaCost.toString())))
            val bole = Registry.ENCHANTMENT.get(Identifier(bola))
            if (bole is ScepterAugment) {
                val spellTier = bole.getTier()
                tooltip.add(
                    AcText.translatable("lore_book.tier",spellTier.toString()).formatted(Formatting.WHITE)
                )//AcText.translatable("lore_book.tier").formatted(Formatting.WHITE).append(AcText.literal(spellTier.toString()))
            }
        } else {
            addFlavorText(tooltip, context)
        }
    }

    override fun hasGlint(stack: ItemStack): Boolean {
        return if (glint) {
            true
        } else {
            super.hasGlint(stack)
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        val item = stack.item
        if (item !is AbstractAugmentBookItem) return TypedActionResult.fail(stack)
        //if (world !is ServerWorld) return TypedActionResult.fail(stack)
        do {
            val shouldOffer: Boolean
            val stack2 = if (stack.count > 1) {
                shouldOffer = true
                stack.split(1)
            } else {
                shouldOffer = false
                stack
            }
            val nbt = stack2.orCreateNbt
            if (!nbt.contains(NbtKeys.LORE_KEY.str())) {
                val aug = getRandomBookAugment(loreTier.list(), user, hand)
                nbt.putString(NbtKeys.LORE_KEY.str(), aug)
                val bola = Identifier(nbt.getString(NbtKeys.LORE_KEY.str())).toString()
                val type = AugmentHelper.getAugmentType(bola)
                if (type != SpellType.NULL) {
                    nbt.putString(NbtKeys.LORE_TYPE.str(), type.str())
                }
                world.playSound(null, user.blockPos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.NEUTRAL, 0.7f, 1.0f)
                if (shouldOffer){
                    user.inventory.offerOrDrop(stack2)
                }
            } else if (Identifier(nbt.getString(NbtKeys.LORE_KEY.str())).namespace == "minecraft") {
                val aug = getRandomBookAugment(loreTier.list(), user, hand)
                nbt.putString(NbtKeys.LORE_KEY.str(), aug)
                val bola = Identifier(nbt.getString(NbtKeys.LORE_KEY.str())).toString()
                val type = AugmentHelper.getAugmentType(bola)
                if (type != SpellType.NULL) {
                    nbt.putString(NbtKeys.LORE_TYPE.str(), type.str())
                }
                world.playSound(null, user.blockPos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.NEUTRAL, 0.7f, 1.0f)
                if (shouldOffer){
                    user.inventory.offerOrDrop(stack2)
                }
            }
            if (user is ServerPlayerEntity) {
                ScepterHelper.USED_KNOWLEDGE_BOOK.trigger(user)
            }
        } while (stack.count > 1)
        user.itemCooldownManager.set(stack.item, 10)
        return useAfterWriting(stack, world, user, hand)
    }

    open fun useAfterWriting(stack: ItemStack, world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack>{
        return TypedActionResult.pass(stack)
    }

    open fun getRandomBookAugment(list: List<String>, user: PlayerEntity, hand: Hand): String{
        val rnd = (user as SyncedRandomProviding).provider
        if (list.isEmpty()) return AC.fallbackId.toString()
        val rndIndex = rnd.getRandom().nextInt(list.size)
        return list[rndIndex]
    }

    companion object{

        private var preSync = 0L
        val PRE_SYNC_CHANNEL = Identifier(AC.MOD_ID,"book_rand_pre_sync")

        fun registerClient(){
            ClientPlayNetworking.registerGlobalReceiver(PRE_SYNC_CHANNEL){_,_,b,_ ->
                preSync = b.readLong()
                println("Synced from server with seed: $preSync")
            }
        }

        fun registerServer(){
            ServerPlayConnectionEvents.JOIN.register {handler, _, _ ->
                val player = handler.player
                (player as SyncedRandomProviding).provider.sync(player)
            }
        }

        fun addLoreKeyForREI(stack: ItemStack,augment: String){
            val nbt = stack.orCreateNbt
            if(!nbt.contains(NbtKeys.LORE_KEY.str())) {
                nbt.putString(NbtKeys.LORE_KEY.str(),augment)
            }
        }
    }

    internal class SyncedRandomProvider {

        private var random: Random

        init{
            random = Random(preSync)
        }

        fun getRandom(): Random{
            return random
        }

        fun sync(player: ServerPlayerEntity) {
            val seed = System.currentTimeMillis()
            /*thisSeed = seed
            lastSyncCheck = seed*/
            random = Random(seed)
            val buf = PacketByteBufs.create()
            buf.writeLong(seed)
            ServerPlayNetworking.send(player,PRE_SYNC_CHANNEL,buf)
        }

    }

}
