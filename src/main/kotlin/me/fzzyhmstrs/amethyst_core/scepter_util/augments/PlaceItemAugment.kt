package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import eu.pb4.common.protection.api.CommonProtection
import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentConsumer
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterTier
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.*
import net.minecraft.network.PacketByteBuf
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World

/**
 * Simple template that places a block item into the world. can be implemented in an Item Registry with no extension by defining the [_item] in the constructor.
 */
abstract class PlaceItemAugment(tier: ScepterTier, maxLvl: Int,item: Item): ScepterAugment(tier,maxLvl){
    constructor(tier: ScepterTier, item: Item): this(tier,1, item)

    private val _item = item

    override val baseEffect: AugmentEffect
        get() = super.baseEffect.withRange(4.5)

    override fun applyTasks(world: World, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect): Boolean {
        if (user !is ServerPlayerEntity) return false
        val hit = RaycasterUtil.raycastHit(effects.range(level),entity = user)
        val bl = (hit != null && hit is BlockHitResult && CommonProtection.canPlaceBlock(world,hit.blockPos,user.gameProfile,user))

        if (bl){
            return blockPlacing(hit as BlockHitResult,world, user, hand, level, effects)
        }
        return bl
    }

    open fun blockPlacing(hit: BlockHitResult, world: World, user: ServerPlayerEntity, hand: Hand, level: Int, effects: AugmentEffect): Boolean{
        val stack = itemToPlace(world,user)
        when (val testItem = stack.item) {
            is BlockItem -> {
                if (!testItem.place(ItemPlacementContext(user, hand, ItemStack(testItem),hit)).isAccepted) return false
                val group = testItem.block.defaultState.soundGroup
                val sound = group.placeSound
                world.playSound(null,hit.blockPos,sound,SoundCategory.BLOCKS,(group.volume + 1.0f)/2.0f,group.pitch * 0.8f)
                //sendItemPacket(user, stack, hand, hit)
                effects.accept(user, AugmentConsumer.Type.BENEFICIAL)
                return true
            }
            is BucketItem -> {
                if (!testItem.placeFluid(user,world,hit.blockPos,hit)) return false
                world.playSound(null,hit.blockPos,soundEvent(),SoundCategory.BLOCKS,1.0f,1.0f)
                effects.accept(user, AugmentConsumer.Type.BENEFICIAL)
                return true
            }
            else -> {
                return false
            }
        }
    }

    protected fun sendItemPacket(user: ServerPlayerEntity,stack: ItemStack,hand: Hand,hit: BlockHitResult){
        ServerPlayNetworking.send(user,PLACE_ITEM_PACKET,placeItemPacket(stack,hand,hit))
    }

    private fun placeItemPacket(itemStack: ItemStack, hand: Hand, hit: BlockHitResult): PacketByteBuf{
        val buf = PacketByteBufs.create()
        buf.writeItemStack(itemStack)
        buf.writeEnumConstant(hand)
        buf.writeBlockHitResult(hit)
        return buf
    }

    open fun itemToPlace(world: World, user: LivingEntity): ItemStack {
        return ItemStack(_item)
    }

    companion object{

        val PLACE_ITEM_PACKET = Identifier(AC.MOD_ID,"place_item_packet")

        fun registerClient(){
            ClientPlayNetworking.registerGlobalReceiver(PLACE_ITEM_PACKET)
            { client,_,packetByteBuf,_ ->
                val player = client.player?:return@registerGlobalReceiver
                val stack = packetByteBuf.readItemStack()
                val hand = packetByteBuf.readEnumConstant(Hand::class.java)
                val hit = packetByteBuf.readBlockHitResult()
                placeItem(player.world,player,stack, hand, hit)
            }
        }

        private fun placeItem(world: World, user: PlayerEntity, itemStack: ItemStack, hand: Hand, hit: HitResult){
            when (val testItem = itemStack.item) {
                is BlockItem -> {
                    testItem.place(ItemPlacementContext(user, hand, ItemStack(testItem),hit as BlockHitResult))
                }
                is BucketItem -> {
                    testItem.placeFluid(user,world,(hit as BlockHitResult).blockPos,hit)
                }
                else -> {
                    return
                }
            }
        }
    }

}