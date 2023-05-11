package me.fzzyhmstrs.amethyst_core.entity_util

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.PairedAugments
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

abstract class PlayerItemEntity: ThrownItemEntity, ModifiableEffectEntity {

    constructor (entityType: EntityType<out PlayerItemEntity?>?, world: World?):
            super(entityType, world)

    constructor(entityType: EntityType<out PlayerItemEntity?>?,world: World?, owner: LivingEntity?,augments: PairedAugments):
            super(entityType, owner, world){
                augment = augments
            }

    var augment: PairedAugments = PairedAugments()
    override var entityEffects: AugmentEffect = AugmentEffect()
    override var level: Int = 0

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        onItemEntityHit(entityHitResult)
        discard()
    }

    open fun onItemEntityHit(entityHitResult: EntityHitResult){
        val entity = owner
        if (entity is LivingEntity) {
            augment.processSingleEntityHit(entityHitResult,world,this,entity, Hand.MAIN_HAND,level,entityEffects)
            if (!entityHitResult.entity.isAlive){
                augment.processOnKill(entityHitResult,world,this,entity,Hand.MAIN_HAND,level,entityEffects)
            }
        }
    }

    override fun onBlockHit(blockHitResult: BlockHitResult) {
        super.onBlockHit(blockHitResult)
        onItemBlockHit(blockHitResult)
        discard()
    }

    open fun onItemBlockHit(blockHitResult: BlockHitResult){
        val entity = owner
        if (entity is LivingEntity) {
            augment.processSingleBlockHit(blockHitResult,world,this,entity, Hand.MAIN_HAND,level,entityEffects)
        }
    }

}