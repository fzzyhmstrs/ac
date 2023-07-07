package me.fzzyhmstrs.amethyst_core.entity

import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import net.minecraft.entity.EntityType
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.projectile.thrown.ThrownItemEntity
import net.minecraft.util.Hand
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

abstract class PlayerItemEntity: ThrownItemEntity, ModifiableEffectEntity<PlayerItemEntity> {

    constructor (entityType: EntityType<out PlayerItemEntity?>?, world: World?):
            super(entityType, world)

    constructor(entityType: EntityType<out PlayerItemEntity?>?,world: World?, owner: LivingEntity?):
            super(entityType, owner, world)

    override var spells: PairedAugments = PairedAugments()
    override var entityEffects: AugmentEffect = AugmentEffect()
    override var level: Int = 0

    override fun onEntityHit(entityHitResult: EntityHitResult) {
        super.onEntityHit(entityHitResult)
        onItemEntityHit(entityHitResult)
        discard()
    }

    open fun onItemEntityHit(entityHitResult: EntityHitResult){
        val entity = owner
        if (entity is LivingEntity && entity is SpellCastingEntity) {
            spells.processSingleEntityHit(entityHitResult,world,this,entity, Hand.MAIN_HAND,level,entityEffects)
            if (!entityHitResult.entity.isAlive){
                spells.processOnKill(entityHitResult,world,this,entity,Hand.MAIN_HAND,level,entityEffects)
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
        if (entity is LivingEntity && entity is SpellCastingEntity) {
            spells.processSingleBlockHit(blockHitResult,world,this,entity, Hand.MAIN_HAND,level,entityEffects)
        }
    }

}