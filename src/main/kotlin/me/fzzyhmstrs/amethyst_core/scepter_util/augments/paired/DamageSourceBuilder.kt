package me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity

class DamageSourceBuilder(private val attacker: LivingEntity, private val source: Entity? = null){
    
    private var damageSource = if(attacker is PlayerEntity) DamageSource.player(attacker) else DamageSource.mob(attacker)
    
    fun modify(modification: Modifier): DamageSourceBuilder {
        damageSource = modification.modify(damageSource,attacker,source)
        return this
    }
    
    fun build(): DamageSource{
        return damageSource
    }
    
    @FunctionalInterface
    fun interface Modifier{
        fun modify(damageSource: DamageSource, attacker: LivingEntity, source: Entity?): DamageSource
    }
}
