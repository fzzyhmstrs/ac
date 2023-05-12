package me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired

import me.fzzyhmstrs.amethyst_core.scepter_util.CustomDamageSources
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity

class DamageSourceBuilder(private val attacker: LivingEntity, private val source: Entity? = null){
    
    private var damageSource: DamageSource = CustomDamageSources.GenericDamageSource(source, attacker)

    fun set(source: DamageSource): DamageSourceBuilder{
        damageSource = source
        return this
    }

    fun modify(modification: Modifier): DamageSourceBuilder {
        damageSource = modification.modify(damageSource,attacker,source)
        return this
    }

    fun soul(): DamageSourceBuilder{
        damageSource.setBypassesArmor().setBypassesProtection().setUsesMagic()
        return this
    }
    fun magic(): DamageSourceBuilder{
        damageSource.setBypassesArmor().setUsesMagic()
        return this
    }
    fun fire(): DamageSourceBuilder{
        damageSource.setBypassesArmor().setFire()
        return this
    }
    fun projectile(): DamageSourceBuilder{
        damageSource.setProjectile()
        return this
    }
    fun explosive(): DamageSourceBuilder{
        damageSource.setExplosive()
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
