package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentConsumer
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.sound.SoundCategory
import net.minecraft.util.Hand
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * template for summoning 1 or more entities into the world at a location. This template will not successfully cast if the player doesn't target a block, so if you want to summon entities in a more general way, consider a [MiscAugment]
 *
 * [placeEntity] is the primary method to override for this template. Ensure you have a super call so the sound event and Modifier Consumers can be applied.
 */
abstract class SummonEntityAugment(tier: Int, maxLvl: Int, vararg slot: EquipmentSlot): ScepterAugment(tier,maxLvl,EnchantmentTarget.WEAPON, *slot) {

    override val baseEffect: AugmentEffect
        get() = AugmentEffect().withRange(3.0,0.0,0.0)

    override fun applyTasks(
        world: World,
        user: LivingEntity,
        hand: Hand,
        level: Int,
        effects: AugmentEffect
    ): Boolean {
        if (user !is PlayerEntity) return false
        val hit = RaycasterUtil.raycastHit(
            distance = effects.range(level),
            user,
            includeFluids = true
        ) ?: return false
        if (hit.type != HitResult.Type.BLOCK) return false
        return placeEntity(world, user, hit, level, effects)
    }

    open fun placeEntity(world: World, user: PlayerEntity, hit: HitResult, level: Int, effects: AugmentEffect): Boolean{
        effects.accept(user, AugmentConsumer.Type.BENEFICIAL)
        world.playSound(null, user.blockPos, soundEvent(), SoundCategory.PLAYERS, 1.0F, 1.0F)
        return true
    }

    open fun findSpawnPos(world: World,startPos: BlockPos, radius: Int, heightNeeded: Int, blockNeeded: Block = Blocks.AIR, tries: Int = 8): BlockPos{
        for (i in 1..tries){
            val xPos = startPos.x + world.random.nextBetween(-radius,radius)
            val yPos = startPos.up().y
            val zPos = startPos.z + world.random.nextBetween(-radius,radius)
            for (j in searchArray){
                val testPos = BlockPos(xPos,yPos + j,zPos)
                if (world.getBlockState(testPos).isOf(blockNeeded)){
                    if (heightNeeded > 1){
                        var found2 = true
                        for (k in 1 until heightNeeded){
                            if (!world.getBlockState(testPos.up(k)).isOf(blockNeeded)){
                                found2 = false
                                break
                            }
                        }
                        if (!found2) continue
                    }

                }
                return testPos
            }
        }
        return BlockPos.ORIGIN
    }

    companion object{
        private val searchArray = intArrayOf(0,1,-1,2,-2,3,-3)
    }

}