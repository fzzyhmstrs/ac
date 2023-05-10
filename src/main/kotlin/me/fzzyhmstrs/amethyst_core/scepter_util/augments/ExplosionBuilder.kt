package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.event.AfterSpellEvent
import me.fzzyhmstrs.amethyst_core.modifier_util.*
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterTier
import me.fzzyhmstrs.fzzy_core.coding_util.*
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.gson
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.readOrCreateUpdated
import me.fzzyhmstrs.fzzy_core.registry.SyncedConfigRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.world.ServerWorld
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

class ExplosionBuilder(val source: Entity, val pos: Vec3d){

    private var behavior: ExplosionBehavior = EntityExplosionBehavior(source)
    private var damageSource: DamageSourceBuilder = DamageSourceBuilder()
    private var power: Float = 1f
    private var bl: Boolean = false
    private var type: World.ExplosionSourceType = World.ExplosionSourceType.NONE
    
    fun withBehavior(bevahior: ExplosionBehavior): ExplosionBehavior{
        this.behavior = behavior
        return this
    }
    fun modifyDamageSource(modification: Consumer<DamageSourceBuilder>): ExplosionBehavior{
        modification.accept(damageSource)
        return this
    }
    fun withPower(power: Float): ExplosionBuilder{
        this.power = power
        return this
    }
    fun modifyPower(modification: Function<Float,Float>): ExplosionBuilder{
        this.power = modification.apply(power)
        return this
    }
    fun withBl(bl: Boolean): ExplosionBuilder{
        this.bl = bl
        return this
    }
    fun withType(type: World.ExplosionSourceType): ExplosionBuilder{
        this.type = type
        return this
    }
    fun getPower(): Float{
        return this.power
    }
    
    fun explode(world: World){
    }
  
}
