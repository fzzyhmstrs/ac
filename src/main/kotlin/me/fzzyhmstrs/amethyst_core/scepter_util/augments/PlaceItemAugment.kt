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
abstract class PlaceItemAugment(
    tier: ScepterTier, 
    maxLvl: Int,
    private val item: Item,
    augmentData: AugmentDatapoint,
    augmentType: AugmentType = AugmentType.BLOCK_TARGET)
: 
ScepterAugment(
    tier,
    maxLvl,
    augmentData,
    augmentType)
{    
    override val baseEffect: AugmentEffect
        get() = super.baseEffect.withRange(4.5)

    override fun applyTasks(world: World,user: LivingEntity,hand: Hand,level: Int,effects: AugmentEffect,spells: PairedAugments): TypedActionResult<List<Identifier>> {
        if (user !is ServerPlayerEntity) return actionResult(ActionResult.FAIL)
        val hit = RaycasterUtil.raycastHit(effects.range(level),entity = user)
        val bl = (hit != null && hit is BlockHitResult && CommonProtection.canPlaceBlock(world,hit.blockPos,user.gameProfile,user))

        if (bl){
            return blockPlacing(hit as BlockHitResult,world, user, hand, level, effects)
        }
        return bl
    }
    
    override fun onBlockHit(blockHitResult: BlockHitResult, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments): TypedActionResult<List<Identifier>>{
        if (othersType == AugmentType.BLOCK_TARGET) return customItemPlaceOnBlockHit(blockHitResult, world, source, user, hand, level, effects, othersType, spells)
        when (item) {
            is BlockItem -> {
                val stack = itemToPlace(world,user)
                if (!testItem.place(ItemPlacementContext(user, hand, stack, blockHitResult)).isAccepted) return actionResult(ActionResult.FAIL)
                hitSoundEvent(world, blockHitResult.blockPos)
                //sendItemPacket(user, stack, hand, hit)
                return actionResult(ActionResult.SUCCESS, AugmentHelper.BLOCK_PLACED)
            }
            is BucketItem -> {
                if (!testItem.placeFluid(user,world,hit.blockPos,hit)) return false
                hitSoundEvent(world, blockHitResult.blockPos)
                return actionResult(ActionResult.SUCCESS, AugmentHelper.BLOCK_PLACED)
            }
            else -> {
                return actionResult(ActionResult.FAIL)
            }
        }
    }
    
    open fun customItemPlaceOnBlockHit(blockHitResult: BlockHitResult, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments): TypedActionResult<List<Identifier>>{
        return actionResult(ActionResult.PASS)
    }
    
    override fun hitSoundEvent(world: World, blockPos: BlockPos){
        if (item is BlockItem){
            val group = item.block.defaultState.soundGroup
            val sound = group.placeSound
            world.playSound(null,blockPos,sound,SoundCategory.BLOCKS,(group.volume + 1.0f)/2.0f,group.pitch * 0.8f)
        } else {
            world.playSound(null,blockPos,soundEvent(),SoundCategory.BLOCKS,1.0f,1.0f)
        }
    }

    open fun itemToPlace(world: World, user: LivingEntity): ItemStack {
        return ItemStack(item)
    }
}
