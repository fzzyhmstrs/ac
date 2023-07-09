package me.fzzyhmstrs.amethyst_core.augments.paired

import me.fzzyhmstrs.amethyst_core.augments.CustomExplosion
import net.minecraft.entity.Entity
import net.minecraft.util.math.Vec3d
import net.minecraft.world.GameRules
import net.minecraft.world.GameRules.BooleanRule
import net.minecraft.world.World
import net.minecraft.world.World.ExplosionSourceType
import net.minecraft.world.explosion.EntityExplosionBehavior
import net.minecraft.world.explosion.Explosion
import net.minecraft.world.explosion.Explosion.DestructionType
import net.minecraft.world.explosion.ExplosionBehavior
import java.util.function.Consumer
import java.util.function.Function

class ExplosionBuilder(damageSourceBuilder: DamageSourceBuilder, val source: Entity?, val pos: Vec3d){

    private var behavior: ExplosionBehavior = if(source != null) EntityExplosionBehavior(source) else ExplosionBehavior()
    private var damageSource: DamageSourceBuilder = damageSourceBuilder
    private var power: Float = 1f
    private var createFire: Boolean = false
    private var type: World.ExplosionSourceType = World.ExplosionSourceType.NONE
    private var customBehavior: CustomExplosion.CustomExplosionBehavior = CustomExplosion.CustomExplosionBehavior()
    
    fun withBehavior(behavior: ExplosionBehavior): ExplosionBuilder {
        this.behavior = behavior
        return this
    }
    fun modifyDamageSource(modification: Consumer<DamageSourceBuilder>): ExplosionBuilder {
        modification.accept(damageSource)
        return this
    }
    fun withPower(power: Float): ExplosionBuilder {
        this.power = power
        return this
    }
    fun modifyPower(modification: Function<Float,Float>): ExplosionBuilder {
        this.power = modification.apply(power)
        return this
    }
    fun withCreateFire(createFire: Boolean): ExplosionBuilder {
        this.createFire = createFire
        return this
    }
    fun withType(type: World.ExplosionSourceType): ExplosionBuilder {
        this.type = type
        return this
    }
    fun withCustomBehavior(customBehavior: CustomExplosion.CustomExplosionBehavior): ExplosionBuilder {
        this.customBehavior = customBehavior
        return this
    }
    fun getPower(): Float{
        return this.power
    }
    fun explode(world: World){
        val destructionType = when (type) {
            ExplosionSourceType.NONE -> DestructionType.KEEP
            ExplosionSourceType.BLOCK -> this.getDestructionType(world,GameRules.BLOCK_EXPLOSION_DROP_DECAY)
            ExplosionSourceType.MOB -> {
                if (world.gameRules.getBoolean(GameRules.DO_MOB_GRIEFING)) {
                    this.getDestructionType(world,GameRules.MOB_EXPLOSION_DROP_DECAY)
                }
                DestructionType.KEEP
            }
            ExplosionSourceType.TNT -> this.getDestructionType(world,GameRules.TNT_EXPLOSION_DROP_DECAY)
            else -> throw IncompatibleClassChangeError()
        }
        val explosion = CustomExplosion(world, source, damageSource.build(), behavior, pos.x, pos.y, pos.z, power, createFire, destructionType, customBehavior)
        explosion.collectBlocksAndDamageEntities()
        explosion.affectWorld(true)
    }

    private fun getDestructionType(world: World,gameRuleKey: GameRules.Key<BooleanRule>): DestructionType {
        return if (world.gameRules.getBoolean(gameRuleKey)) DestructionType.DESTROY_WITH_DECAY else DestructionType.DESTROY
    }

}
