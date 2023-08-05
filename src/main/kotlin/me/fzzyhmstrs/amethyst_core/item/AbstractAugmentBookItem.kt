package me.fzzyhmstrs.amethyst_core.item

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.interfaces.SyncedRandomProviding
import me.fzzyhmstrs.amethyst_core.nbt.NbtKeys
import me.fzzyhmstrs.amethyst_core.scepter.LoreTier
import me.fzzyhmstrs.amethyst_core.scepter.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter.SpellType
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.item_util.CustomFlavorItem
import me.fzzyhmstrs.fzzy_core.item_util.FlavorHelper
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import kotlin.math.abs
import kotlin.random.Random

/**
 * used to define the augment books like Amethyst Imbuements Book of Lore. For all Augment tiers except Extreme Tier, there is a pre-existing book in Amethyst Imbuement.
 *
 * If you don't want to rely on Amethyst Imbuement, implementing your own augment books is as simple as extending this class and overriding [loreTier] with the proper tier.
 */
abstract class AbstractAugmentBookItem(settings: Settings) : CustomFlavorItem(settings) {

    abstract val loreTier: LoreTier
    
    open fun multiplier(): Float{
        return 1f
    }

    override fun appendTooltip(
        stack: ItemStack,
        world: World?,
        tooltip: MutableList<Text>,
        context: TooltipContext
    ) {
        val nbt = stack.orCreateNbt
        if (nbt.contains(NbtKeys.LORE_KEY)){
            val bola = Identifier(nbt.getString(NbtKeys.LORE_KEY)).toString()
            val aug = AugmentHelper.getScepterAugment(bola)?:return
            //name and desc
            tooltip.add(AcText.translatable("lore_book.augment",AcText.translatable("enchantment.${Identifier(bola).namespace}.${Identifier(bola).path}")).formatted(Formatting.GOLD))
            tooltip.add(AcText.translatable("enchantment.${Identifier(bola).namespace}.${Identifier(bola).path}.desc").formatted(Formatting.WHITE))
            //spell type
            val type = aug.augmentData.type
            if (type == SpellType.NULL){
                tooltip.add(AcText.translatable("lore_book.${type.str()}").formatted(type.fmt()))
            } else {
                val lvl = aug.augmentData.minLvl
                tooltip.add(AcText.translatable("lore_book.${type.str()}",lvl.toString()).formatted(type.fmt()))
            }
            //key crafting item
            val item = aug.augmentData.keyItem
            val itemText = item.name.copyContentOnly().formatted(Formatting.WHITE)
            tooltip.add(AcText.translatable("lore_book.key_item",itemText).formatted(Formatting.WHITE))
            //xp levels needed to imbue
            val xpLevels = AugmentHelper.getAugmentImbueLevel(bola, multiplier())
            tooltip.add(AcText.translatable("lore_book.xp_level", xpLevels.toString()).formatted(Formatting.WHITE))
            //cooldown
            val cooldown = aug.augmentData.cooldown
            val cooldownBase = cooldown.base / 20f
            val cooldownPerLvl = cooldown.perLevel / 20f
            val cooldownKey = if(cooldownPerLvl < 0){
                "lore_book.cooldown.minus"
            } else if (cooldownPerLvl == 0f){
                "lore_book.cooldown.basic"
            } else {
                "lore_book.cooldown.plus"
            }
            tooltip.add(AcText.translatable(cooldownKey,cooldownBase.toString(), abs(cooldownPerLvl).toString()).formatted(Formatting.WHITE))
            //mana cost
            val manaCost = aug.augmentData.manaCost
            tooltip.add(AcText.translatable("lore_book.mana_cost",manaCost.toString()).formatted(Formatting.WHITE))
            //tier
            val spellTier = aug.getTier()
            tooltip.add(
                AcText.translatable("lore_book.tier",spellTier.toString()).formatted(Formatting.WHITE)
            )
            //scpeter XP gained
            val castXp = aug.augmentData.castXp
            tooltip.add(AcText.translatable("lore_book.cast_xp",castXp.toString()).formatted(Formatting.WHITE))
        } else {
            super.appendTooltip(stack, world, tooltip, context)
        }
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        val stack = user.getStackInHand(hand)
        val item = stack.item
        if (item !is AbstractAugmentBookItem) return TypedActionResult.fail(stack)
        if (stack.hasNbt() && stack.nbt?.contains(NbtKeys.LORE_KEY) == true){
            return useAfterWriting(stack, world, user, hand)
        }
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
            if (!nbt.contains(NbtKeys.LORE_KEY)) {
                val aug = getRandomBookAugment(loreTier.list(), user, hand)
                nbt.putString(NbtKeys.LORE_KEY, aug)
                val bola = Identifier(nbt.getString(NbtKeys.LORE_KEY)).toString()
                val type = AugmentHelper.getAugmentType(bola)
                if (type != SpellType.NULL) {
                    nbt.putString(NbtKeys.LORE_TYPE, type.str())
                }
                world.playSound(null, user.blockPos, SoundEvents.ITEM_BOOK_PAGE_TURN, SoundCategory.NEUTRAL, 0.7f, 1.0f)
                if (shouldOffer){
                    user.inventory.offerOrDrop(stack2)
                }
            } else if (Identifier(nbt.getString(NbtKeys.LORE_KEY)).namespace == "minecraft") {
                val aug = getRandomBookAugment(loreTier.list(), user, hand)
                nbt.putString(NbtKeys.LORE_KEY, aug)
                val bola = Identifier(nbt.getString(NbtKeys.LORE_KEY)).toString()
                val type = AugmentHelper.getAugmentType(bola)
                if (type != SpellType.NULL) {
                    nbt.putString(NbtKeys.LORE_TYPE, type.str())
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
        return TypedActionResult.success(stack)
    }

    open fun getRandomBookAugment(list: List<String>, user: PlayerEntity, hand: Hand): String{
        val rnd = (user as SyncedRandomProviding).provider
        if (list.isEmpty()) return AC.fallbackId.toString()
        val rndIndex = rnd.getRandom().nextInt(list.size)
        return list[rndIndex]
    }

    companion object{

        private var preSync = 0L
        val PRE_SYNC_CHANNEL = AC.identity("book_rand_pre_sync")

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
            ServerPlayerEvents.AFTER_RESPAWN.register {_,newPlayer,_ ->
                (newPlayer as SyncedRandomProviding).provider.sync(newPlayer)
            }
        }

        fun addLoreKeyForREI(stack: ItemStack,augment: String){
            val nbt = stack.orCreateNbt
            if(!nbt.contains(NbtKeys.LORE_KEY)) {
                nbt.putString(NbtKeys.LORE_KEY,augment)
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
