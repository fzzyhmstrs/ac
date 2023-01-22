package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.fzzy_core.coding_util.PersistentEffectHelper
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * data class for the PersistentEffectHelper that is specialized for usage with Scepter Augments
 */
class AugmentPersistentEffectData(val world: World, val user: LivingEntity,
                                  val blockPos: BlockPos, val entityList: MutableList<Entity>,
                                  val level: Int = 1, val effect: AugmentEffect
): PersistentEffectHelper.PersistentEffectData

/**
 * If your Scepter Augment needs to have a persistent effect, such as a repeated AOE effect that occurs every second for 5 seconds, use this interface. See Amethyst Imbuements Lightning Storm or Fang Barrage augments as an example.
 */
interface AugmentPersistentEffect: PersistentEffectHelper.PersistentEffect {
    override fun persistentEffect(data: PersistentEffectHelper.PersistentEffectData){
        if (data !is AugmentPersistentEffectData) return
        augmentPersistentEffect(
            data.world,
            data.user,
            data.blockPos,
            data.entityList,
            data.level,
            data.effect
        )
    }

    /**
     * override this and include the effects you want to occur over time. Could be as simple as a call back to the primary effect again, or some other secondary stuff.
     */
    fun augmentPersistentEffect(world: World, user: LivingEntity, blockPos: BlockPos, entityList: MutableList<Entity>, level: Int = 1, effect: AugmentEffect)
}