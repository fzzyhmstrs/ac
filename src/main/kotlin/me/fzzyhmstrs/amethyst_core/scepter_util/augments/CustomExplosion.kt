package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import net.minecraft.world.explosion.Explosion
import net.minecraft.world.explosion.ExplosionBehavior

open class CustomExplosion(
    world: World?,
    entity: Entity?,
    damageSource: DamageSource?,
    behavior: ExplosionBehavior?,
    x: Double,
    y: Double,
    z: Double,
    power: Float,
    createFire: Boolean,
    destructionType: DestructionType?,
    customExplosionBehavior: CustomExplosionBehavior = CustomExplosionBehavior())
    :
    Explosion(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType)
{



    open class CustomExplosionBehavior{
        open fun affectEntity(entity: Entity){}
        open fun setFireBlockState(pos: BlockPos){}
    }

}