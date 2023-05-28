package me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired

import me.fzzyhmstrs.amethyst_core.scepter_util.CustomDamageSources
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource

class DamageSourceBuilder(private val attacker: LivingEntity, private val source: Entity? = null){
    
    private var damageSource: DamageSource = CustomDamageSources.GenericDamageSource(source, attacker)
    private var bypassesArmor = false
    private var bypassesProtection = false
    private var magic = false
    private var fire = false
    private var projectile = false
    private var explosive = false

    fun set(source: DamageSource): DamageSourceBuilder{
        damageSource = source
        return this
    }

    fun modify(modification: Modifier): DamageSourceBuilder {
        damageSource = modification.modify(damageSource,attacker,source)
        return this
    }

    fun bypassArmor(): DamageSourceBuilder{
        bypassesArmor = true
        return this
    }
    fun soul(): DamageSourceBuilder{
        bypassesArmor = true
        bypassesProtection = true
        magic = true
        return this
    }
    fun magic(): DamageSourceBuilder{
        bypassesArmor = true
        magic = true
        return this
    }
    fun fire(bypass: Boolean = true): DamageSourceBuilder{
        bypassesArmor = bypass
        fire = true
        return this
    }
    fun projectile(): DamageSourceBuilder{
        projectile = true
        return this
    }
    fun explosive(): DamageSourceBuilder{
        explosive = true
        return this
    }
    
    fun build(): DamageSource{
        if(bypassesArmor) damageSource.setBypassesArmor()
        if(bypassesProtection) damageSource.setBypassesProtection()
        if(magic) damageSource.setUsesMagic()
        if(fire) damageSource.setFire()
        if (projectile) damageSource.setProjectile()
        if (explosive) damageSource.setExplosive()
        return damageSource
    }
    
    @FunctionalInterface
    fun interface Modifier{
        fun modify(damageSource: DamageSource, attacker: LivingEntity, source: Entity?): DamageSource
    }
}
