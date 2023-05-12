package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import eu.pb4.common.protection.api.CommonProtection
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterTier
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.ProcessContext
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.*
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

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
        if (user !is ServerPlayerEntity) return FAIL
        val hit = RaycasterUtil.raycastHit(effects.range(level),entity = user)
        if (hit != null && hit is BlockHitResult && CommonProtection.canPlaceBlock(world,hit.blockPos,user.gameProfile,user)){
            val list = spells.processSingleBlockHit(hit,world,null,user, hand, level, effects)
            return if (list.isNotEmpty()){
                actionResult(ActionResult.SUCCESS,*list.toTypedArray())
            } else {
                FAIL
            }
        }
        return FAIL
    }
    
    override fun onBlockHit(
        blockHitResult: BlockHitResult,
        context: ProcessContext,
        world: World,
        source: Entity?,
        user: LivingEntity,
        hand: Hand,
        level: Int,
        effects: AugmentEffect,
        othersType: AugmentType,
        spells: PairedAugments
    ): TypedActionResult<List<Identifier>> {
        if (othersType == AugmentType.BLOCK_TARGET) return customItemPlaceOnBlockHit(blockHitResult, world, source, user, hand, level, effects, othersType, spells)
        if (user !is ServerPlayerEntity) return FAIL
        when (item) {
            is BlockItem -> {
                val stack = itemToPlace(world,user)
                if (!item.place(ItemPlacementContext(user, hand, stack, blockHitResult)).isAccepted) return FAIL
                hitSoundEvent(world, blockHitResult.blockPos)
                //sendItemPacket(user, stack, hand, hit)
                return actionResult(ActionResult.SUCCESS, AugmentHelper.BLOCK_PLACED)
            }
            is BucketItem -> {
                if (!item.placeFluid(user,world,blockHitResult.blockPos,blockHitResult)) return FAIL
                hitSoundEvent(world, blockHitResult.blockPos)
                return actionResult(ActionResult.SUCCESS, AugmentHelper.BLOCK_PLACED)
            }
            else -> {
                return FAIL
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
            world.playSound(null,blockPos,SoundEvents.BLOCK_WOOD_PLACE,SoundCategory.BLOCKS,1.0f,1.0f)
        }
    }

    open fun itemToPlace(world: World, user: LivingEntity): ItemStack {
        return ItemStack(item)
    }
}
