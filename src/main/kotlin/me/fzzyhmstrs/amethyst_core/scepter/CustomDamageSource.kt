package me.fzzyhmstrs.amethyst_core.scepter

import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageType
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import java.util.function.Predicate

open class CustomDamageSource(
    type: RegistryEntry<DamageType>,
    source: Entity?,
    attacker: LivingEntity?,
    private val exclusions: Predicate<TagKey<DamageType>> = Predicate {_ -> false},
    private val additions: Predicate<TagKey<DamageType>> = Predicate {_ -> false})
    :
    DamageSource(type, source, attacker)
{

    override fun isIn(tag: TagKey<DamageType>): Boolean {
        return (super.isIn(tag) || additions.test(tag)) && !exclusions.test(tag)
    }

}