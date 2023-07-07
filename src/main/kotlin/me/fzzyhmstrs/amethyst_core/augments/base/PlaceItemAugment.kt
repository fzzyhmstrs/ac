package me.fzzyhmstrs.amethyst_core.augments.base

import eu.pb4.common.protection.api.CommonProtection
import me.fzzyhmstrs.amethyst_core.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.augments.SpellActionResult
import me.fzzyhmstrs.amethyst_core.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter.ScepterTier
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.item.*
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

abstract class PlaceItemAugment(
    tier: ScepterTier,
    private val item: Item,
    augmentType: AugmentType = AugmentType.BLOCK_TARGET)
    :
    ScepterAugment(tier, augmentType)
{    
    override val baseEffect: AugmentEffect
        get() = super.baseEffect.withRange(4.5)

    override fun <T> applyTasks(world: World, user: T, hand: Hand, level: Int, effects: AugmentEffect, spells: PairedAugments)
    : 
    SpellActionResult
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        if (user !is ServerPlayerEntity) return FAIL
        val hit = RaycasterUtil.raycastHit(effects.range(level),entity = user)
        if (hit != null && hit is BlockHitResult && CommonProtection.canPlaceBlock(world,hit.blockPos,user.gameProfile,user)){
            val list = spells.processSingleBlockHit(hit,world,null,user, hand, level, effects)
            list.addAll(spells.processOnCast(world,null,user, hand, level, effects))
            castSoundEvent(world, user.blockPos)
            return if (list.isNotEmpty()){
                SpellActionResult.success(list)
            } else {
                FAIL
            }
        }
        return FAIL
    }
    
    override fun <T> onBlockHit(
        blockHitResult: BlockHitResult,
        context: ProcessContext,
        world: World,
        source: Entity?,
        user: T,
        hand: Hand,
        level: Int,
        effects: AugmentEffect,
        othersType: AugmentType,
        spells: PairedAugments
    )
    : 
    SpellActionResult 
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        if (othersType == AugmentType.BLOCK_TARGET) return customItemPlaceOnBlockHit(blockHitResult, world, source, user, hand, level, effects, othersType, spells)
        if (user !is ServerPlayerEntity) return FAIL
        when (item) {
            is BlockItem -> {
                val stack = itemToPlace(world,user)
                if (!item.place(ItemPlacementContext(user, hand, stack, blockHitResult)).isAccepted) return FAIL
                hitSoundEvent(world, blockHitResult.blockPos)
                //sendItemPacket(user, stack, hand, hit)
                return SpellActionResult.success(AugmentHelper.BLOCK_PLACED)
            }
            is BucketItem -> {
                if (!item.placeFluid(user,world,blockHitResult.blockPos,blockHitResult)) return FAIL
                hitSoundEvent(world, blockHitResult.blockPos)
                return SpellActionResult.success(AugmentHelper.BLOCK_PLACED)
            }
            else -> {
                return FAIL
            }
        }
    }
    
    open fun <T> customItemPlaceOnBlockHit(blockHitResult: BlockHitResult, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    : 
    SpellActionResult
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return SUCCESSFUL_PASS
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

    open fun <T> itemToPlace(world: World, user: T)
    : 
    ItemStack 
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return ItemStack(item)
    }
}
