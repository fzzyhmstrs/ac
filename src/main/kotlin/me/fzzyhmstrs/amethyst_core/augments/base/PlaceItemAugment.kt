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
import me.fzzyhmstrs.fzzy_core.entity_util.PlayerCreatable
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.Tameable
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

    override fun <T> applyTasks(world: World, context: ProcessContext, user: T, hand: Hand, level: Int, effects: AugmentEffect, spells: PairedAugments)
    : 
    SpellActionResult
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val onCastResults = spells.processOnCast(context,world,null,user, hand, level, effects)
        if (!onCastResults.success()) return  FAIL
        if (onCastResults.overwrite()) return onCastResults
        val owner = if (user is ServerPlayerEntity){
            user
        } else if (user is Tameable && user.owner is ServerPlayerEntity) {
            user.owner as ServerPlayerEntity
        }else if (user is PlayerCreatable && user.entityOwner is ServerPlayerEntity){
            user.entityOwner as ServerPlayerEntity
        } else {
            return FAIL
        }
        val hit = RaycasterUtil.raycastHit(effects.range(level),entity = user)
        if (hit is BlockHitResult && CommonProtection.canPlaceBlock(world,hit.blockPos,owner.gameProfile,owner)){
            val list = spells.processSingleBlockHit(hit,context,world,null,user, hand, level, effects)
            list.addAll(onCastResults.results())
            spells.castSoundEvents(world, user.blockPos, context)
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
        if (!othersType.empty) return SUCCESSFUL_PASS
        if (user !is ServerPlayerEntity) return FAIL
        when (val itemToPlace = (spells.paired() as? PlaceItemAugment)?.customItemPlaceOnBlockHit(item, blockHitResult,context, world, source, user, hand, level, effects, augmentType, spells)?:item) {
            is BlockItem -> {
                val stack = ItemStack(itemToPlace)
                if (!itemToPlace.place(ItemPlacementContext(user, hand, stack, blockHitResult)).isAccepted) return FAIL
                spells.hitSoundEvents(world, blockHitResult.blockPos,context)
                return SpellActionResult.success(AugmentHelper.BLOCK_PLACED)
            }
            is BucketItem -> {
                if (!itemToPlace.placeFluid(user,world,blockHitResult.blockPos,blockHitResult)) return FAIL
                spells.hitSoundEvents(world, blockHitResult.blockPos, context)
                return SpellActionResult.success(AugmentHelper.BLOCK_PLACED)
            }
            else -> {
                return FAIL
            }
        }
    }

    /**
     * Used by a spell when it's PAIRED to define the custom item it might pass to the primary.
     *
     * The primary's item is passed into startItem
     */
    open fun <T> customItemPlaceOnBlockHit(startItem: Item, blockHitResult: BlockHitResult, context: ProcessContext, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    : 
    Item
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return startItem
    }
    
    override fun hitSoundEvent(world: World, blockPos: BlockPos, context: ProcessContext){
        if (item is BlockItem){
            val group = item.block.defaultState.soundGroup
            val sound = group.placeSound
            world.playSound(null,blockPos,sound,SoundCategory.BLOCKS,(group.volume + 1.0f)/2.0f,group.pitch * 0.8f)
        } else {
            world.playSound(null,blockPos,SoundEvents.BLOCK_WOOD_PLACE,SoundCategory.BLOCKS,1.0f,1.0f)
        }
    }

    fun item(): Item{
        return this.item
    }
}
