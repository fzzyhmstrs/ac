package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import com.google.common.collect.Sets
import com.mojang.datafixers.util.Pair
import it.unimi.dsi.fastutil.objects.ObjectArrayList
import net.minecraft.block.AbstractFireBlock
import net.minecraft.block.Block
import net.minecraft.block.Blocks
import net.minecraft.enchantment.ProtectionEnchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.ItemEntity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.TntEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameters
import net.minecraft.particle.ParticleTypes
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Util
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Box
import net.minecraft.util.math.MathHelper
import net.minecraft.util.math.Vec3d
import net.minecraft.util.math.random.Random
import net.minecraft.world.World
import net.minecraft.world.event.GameEvent
import net.minecraft.world.explosion.Explosion
import net.minecraft.world.explosion.ExplosionBehavior
import java.util.function.Consumer
import kotlin.math.sqrt

open class CustomExplosion(
    private val world: World,
    entity: Entity?,
    damageSource: DamageSource?,
    private val behavior: ExplosionBehavior,
    private val x: Double,
    private val y: Double,
    private val z: Double,
    private val power: Float,
    private val createFire: Boolean,
    private val destructionType: DestructionType,
    private val customExplosionBehavior: CustomExplosionBehavior = CustomExplosionBehavior())
    :
    Explosion(world, entity, damageSource, behavior, x, y, z, power, createFire, destructionType)
{

    private val random = Random.create()

    override fun collectBlocksAndDamageEntities() {
        var l: Int
        var k: Int
        world.emitGameEvent(entity, GameEvent.EXPLODE, Vec3d(x, y, z))
        val set = Sets.newHashSet<BlockPos>()
        val i = 16
        for (j in 0..15) {
            k = 0
            while (k < 16) {
                l = 0
                block2@ while (l < 16) {
                    if (j != 0 && j != 15 && k != 0 && k != 15 && l != 0 && l != 15) {
                        ++l
                        continue
                    }
                    var d = (j.toFloat() / 15.0f * 2.0f - 1.0f).toDouble()
                    var e = (k.toFloat() / 15.0f * 2.0f - 1.0f).toDouble()
                    var f = (l.toFloat() / 15.0f * 2.0f - 1.0f).toDouble()
                    val g = sqrt(d * d + e * e + f * f)
                    d /= g
                    e /= g
                    f /= g
                    var m = x
                    var n = y
                    var o = z
                    val p = 0.3f
                    var h = power * (0.7f + world.random.nextFloat() * 0.6f)
                    while (h > 0.0f) {
                        val blockPos = BlockPos(m, n, o)
                        val blockState = world.getBlockState(blockPos)
                        val fluidState = world.getFluidState(blockPos)
                        if (!world.isInBuildLimit(blockPos)) {
                            ++l
                            continue@block2
                        }
                        val optional = behavior.getBlastResistance(this, world, blockPos, blockState, fluidState)
                        if (optional.isPresent) {
                            h -= (optional.get() + 0.3f) * 0.3f
                        }
                        if (h > 0.0f && behavior.canDestroyBlock(this, world, blockPos, blockState, h)) {
                            set.add(blockPos)
                        }
                        m += d * 0.3
                        n += e * 0.3
                        o += f * 0.3
                        h -= 0.22500001f
                    }
                    ++l
                }
                ++k
            }
        }
        affectedBlocks.addAll((set as Collection<BlockPos>))
        val q = power * 2.0
        k = MathHelper.floor(x - q - 1.0)
        l = MathHelper.floor(x + q + 1.0)
        val r = MathHelper.floor(y - q - 1.0)
        val s = MathHelper.floor(y + q + 1.0)
        val t = MathHelper.floor(z - q - 1.0)
        val u = MathHelper.floor(z + q + 1.0)
        val list = world.getOtherEntities(
            entity, Box(
                k.toDouble(), r.toDouble(), t.toDouble(), l.toDouble(), s.toDouble(), u.toDouble()
            )
        )
        val vec3d = Vec3d(x, y, z)
        for (v in list.indices) {
            val entity = list[v]
            var z: Double = (entity.z - this.z)
            var y: Double = (((entity as? TntEntity)?.y ?: entity.eyeY) - this.y)
            var x: Double = (entity.x - this.x)
            val aa: Double = sqrt(x * x + y * y + z * z)
            val w: Double = sqrt(entity.squaredDistanceTo(vec3d)) / q
            if (entity.isImmuneToExplosion || w > 1.0 || aa == 0.0) continue
            x /= aa
            y /= aa
            z /= aa
            val ab = getExposure(vec3d, entity).toDouble()
            val ac = (1.0 - w) * ab
            entity.damage(damageSource, ((ac * ac + ac) / 2.0 * 7.0 * q + 1.0).toInt().toFloat())
            customExplosionBehavior.affectEntity(entity)
            var ad = ac
            if (entity is LivingEntity) {
                ad = ProtectionEnchantment.transformExplosionKnockback(entity, ac)
            }
            entity.velocity = entity.velocity.add(x * ad, y * ad, z * ad)
            val playerEntity = if (entity is PlayerEntity) entity else continue
            if (playerEntity.isSpectator || playerEntity.isCreative && playerEntity.abilities.flying) continue
            affectedPlayers[playerEntity] = Vec3d(x * ac, y * ac, z * ac)
        }
    }

    /**
     * @param particles whether this explosion should emit explosion or explosion emitter particles around the source of the explosion
     */
    override fun affectWorld(particles: Boolean) {
        if (world.isClient) {
            world.playSound(
                x,
                y,
                z,
                SoundEvents.ENTITY_GENERIC_EXPLODE,
                SoundCategory.BLOCKS,
                4.0f,
                (1.0f + (world.random.nextFloat() - world.random.nextFloat()) * 0.2f) * 0.7f,
                false
            )
        }
        val bl = shouldDestroy()
        if (particles) {
            if (power < 2.0f || !bl) {
                world.addParticle(ParticleTypes.EXPLOSION, x, y, z, 1.0, 0.0, 0.0)
            } else {
                world.addParticle(ParticleTypes.EXPLOSION_EMITTER, x, y, z, 1.0, 0.0, 0.0)
            }
        }
        if (bl) {
            val objectArrayList: ObjectArrayList<Pair<ItemStack,BlockPos>> = ObjectArrayList()
            val bl2 = this.causingEntity is PlayerEntity
            Util.shuffle(affectedBlocks as ObjectArrayList<BlockPos>, world.random)
            for (blockPos in affectedBlocks) {
                val blockState = this.world.getBlockState(blockPos)
                val block = blockState.block
                if (blockState.isAir) continue
                val blockPos2 = blockPos.toImmutable()
                this.world.profiler.push("explosion_blocks")
                if (block.shouldDropItemsOnExplosion(this) && this.world is ServerWorld) {
                    val serverWorld = world
                    val blockEntity = if (blockState.hasBlockEntity()) this.world.getBlockEntity(blockPos) else null
                    val builder = LootContext.Builder(serverWorld).random(this.world.random)
                        .parameter(LootContextParameters.ORIGIN, Vec3d.ofCenter(blockPos))
                        .parameter(LootContextParameters.TOOL, ItemStack.EMPTY)
                        .optionalParameter(LootContextParameters.BLOCK_ENTITY, blockEntity).optionalParameter(
                            LootContextParameters.THIS_ENTITY,
                            entity
                        )
                    if (destructionType == DestructionType.DESTROY_WITH_DECAY) {
                        builder.parameter(LootContextParameters.EXPLOSION_RADIUS, java.lang.Float.valueOf(power))
                    }
                    blockState.onStacksDropped(serverWorld, blockPos, ItemStack.EMPTY, bl2)
                    blockState.getDroppedStacks(builder).forEach(Consumer { stack: ItemStack ->
                        tryMergeStack(
                            objectArrayList,
                            stack,
                            blockPos2
                        )
                    })
                }
                this.world.setBlockState(blockPos, Blocks.AIR.defaultState, Block.NOTIFY_ALL)
                block.onDestroyedByExplosion(this.world, blockPos, this)
                this.world.profiler.pop()
            }
            for (pair in objectArrayList) {
                Block.dropStack(world, pair.second, customExplosionBehavior.affectBlockDropStack(pair.first))
            }
        }
        if (createFire) {
            for (blockPos3 in affectedBlocks) {
                if (random.nextInt(3) != 0
                    || !world.getBlockState(blockPos3).isAir
                    || !world.getBlockState(blockPos3.down()).isOpaqueFullCube(world, blockPos3.down())
                ) continue
                customExplosionBehavior.setFireBlockState(world,blockPos3)
            }
        }
    }

    private fun tryMergeStack(
        stacks: ObjectArrayList<Pair<ItemStack, BlockPos>>,
        stack: ItemStack,
        pos: BlockPos
    ) {
        val i = stacks.size
        for (j in 0 until i) {
            val pair = stacks[j]
            val itemStack = pair.first
            if (!ItemEntity.canMerge(itemStack, stack)) continue
            val itemStack2 = ItemEntity.merge(itemStack, stack, 16)
            stacks[j] = Pair.of(itemStack2, pair.second)
            if (!stack.isEmpty) continue
            return
        }
        stacks.add(Pair.of(stack, pos))
    }

    open class CustomExplosionBehavior{
        open fun affectEntity(entity: Entity){}
        open fun setFireBlockState(world: World,pos: BlockPos){
            world.setBlockState(pos,AbstractFireBlock.getState(world, pos))
        }
        open fun affectBlockDropStack(stack: ItemStack): ItemStack{return stack}
    }

}