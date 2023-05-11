package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterTier
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.PairedAugments
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.Direction
import net.minecraft.world.World

abstract class SummonAugment(
    tier: ScepterTier, 
    maxLvl: Int,
    augmentData: AugmentDatapoint,
    augmentType: AugmentType = AugmentType.SUMMON)
: 
ScepterAugment(
    tier,
    maxLvl,
    augmentData,
    augmentType) {

    override val baseEffect: AugmentEffect
        get() = AugmentEffect().withRange(3.0)

    override fun applyTasks(world: World,user: LivingEntity,hand: Hand,level: Int,effects: AugmentEffect,spells: PairedAugments): TypedActionResult<List<Identifier>> {
        val hit = RaycasterUtil.raycastHit(
            distance = effects.range(level),
            user,
            includeFluids = true
        ) ?: BlockHitResult(user.pos,Direction.UP,user.blockPos,false)
        if (hit !is BlockHitResult) return FAIL
        val list = spells.processSingleBlockHit(hit,world,null,user,hand,level, effects)
        return if (list.isEmpty()) FAIL else actionResult(ActionResult.SUCCESS,*list.toTypedArray())
    }

    override fun onBlockHit(
        blockHitResult: BlockHitResult,
        world: World,
        source: Entity?,
        user: LivingEntity,
        hand: Hand,
        level: Int,
        effects: AugmentEffect,
        othersType: AugmentType,
        spells: PairedAugments
    ): TypedActionResult<List<Identifier>> {
        if (othersType.empty){
            val list = spells.provideSummons(entitiesToSpawn(world,user,blockHitResult,level,effects),this,user, world, hand, level, effects)
            var successes = 0
            for (entity in list){
                if (world.spawnEntity(entity)) successes++
            }
            return if (successes > 0) {
                castSoundEvent(world,user.blockPos)
                actionResult(ActionResult.SUCCESS, AugmentHelper.SUMMONED_MOB)
            } else {
                FAIL
            }
        }
        return actionResult(ActionResult.PASS)
    }

    open fun entitiesToSpawn(world: World, user: LivingEntity, hit: HitResult, level: Int, effects: AugmentEffect): List<Entity>{
        return listOf()
    }

    open fun findSpawnPos(world: World, startPos: BlockPos, boundingBox: Box, radius: Int = 3, tries: Int = 8): BlockPos{
        for (i in 1..tries){
            val xPos = startPos.x + world.random.nextBetween(-radius,radius)
            val yPos = startPos.up().y
            val zPos = startPos.z + world.random.nextBetween(-radius,radius)
            for (j in searchArray){
                val testPos = BlockPos(xPos,yPos + j,zPos)
                if (world.isSpaceEmpty(boundingBox))
                    return testPos
            }
        }
        return BlockPos.ORIGIN
    }

    companion object{
        private val searchArray = intArrayOf(0,1,-1,2,-2,3,-3)
    }

}
