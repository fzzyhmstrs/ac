package me.fzzyhmstrs.amethyst_core.entity

import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.augments.paired.ProcessContext
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.item.Item
import net.minecraft.nbt.NbtCompound
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World
import java.util.concurrent.ConcurrentLinkedQueue

open class PlayerItemEntity: ThrownItemEntity, ModifiableEffectEntity {

    constructor (entityType: EntityType<out PlayerItemEntity?>?, world: World?, item: Item):
            super(entityType, world){
                this.item = item
            }

    constructor(entityType: EntityType<out PlayerItemEntity?>?,world: World?, owner: LivingEntity?, item: Item):
            super(entityType, owner, world){
                this.item = item
            }

    override var entityEffects: AugmentEffect = AugmentEffect()
    override var level: Int = 0
    override var spells: PairedAugments = PairedAugments()
    override var modifiableEffects = ModifiableEffectContainer()
    override var processContext: ProcessContext = ProcessContext.EMPTY_CONTEXT
    private val item: Item

    override fun tick() {
        super.tick()
        tickTickEffects(this, owner, processContext)
    }

    override fun onCollision(hitResult: HitResult) {
        super.onCollision(hitResult)
        discard()
    }

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        if (world.isClient) {
            return
        }
        onItemEntityHit(entityHitResult)
    }

    open fun onItemEntityHit(entityHitResult: EntityHitResult){
        val entity = owner
        if (entity is LivingEntity){
            if (entity is SpellCastingEntity && !spells.empty()) {
                runEffect(ModifiableEffectEntity.DAMAGE,this,entity,processContext)
                spells.processSingleEntityHit(entityHitResult,processContext,world,this,entity,Hand.MAIN_HAND,level,entityEffects)
                if (!entityHitResult.entity.isAlive){
                    runEffect(ModifiableEffectEntity.KILL,this,entity,processContext)
                    spells.processOnKill(entityHitResult,processContext,world,this,entity,Hand.MAIN_HAND,level,entityEffects)
                }
            } else {
                val bl = entityHitResult.entity.damage(this.damageSources.mobProjectile(this,entity),entityEffects.damage(0))
                if (bl){
                    entity.applyDamageEffects(entity,entityHitResult.entity)
                }
            }
        }
    }

    override fun onBlockHit(blockHitResult: BlockHitResult) {
        super.onBlockHit(blockHitResult)
        if (world.isClient) {
            return
        }
        onItemBlockHit(blockHitResult)
    }

    open fun onItemBlockHit(blockHitResult: BlockHitResult){
        val entity = owner
        if (entity is LivingEntity && entity is SpellCastingEntity) {
            processContext.beforeRemoval()
            spells.processSingleBlockHit(blockHitResult,processContext,world,this,entity, Hand.MAIN_HAND,level,entityEffects)
        }
    }

    override fun readCustomDataFromNbt(nbt: NbtCompound) {
        readModifiableNbt(nbt)
        super.readCustomDataFromNbt(nbt)
    }

    override fun writeCustomDataToNbt(nbt: NbtCompound) {
        writeModifiableNbt(nbt)
        super.writeCustomDataToNbt(nbt)
    }

    override fun getDefaultItem(): Item {
        return item
    }
}