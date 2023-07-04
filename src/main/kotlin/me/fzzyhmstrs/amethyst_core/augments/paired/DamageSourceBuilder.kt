package me.fzzyhmstrs.amethyst_core.augments.paired

import me.fzzyhmstrs.amethyst_core.scepter.CustomDamageSource
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageType
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.registry.RegistryKey
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.entry.RegistryEntry
import net.minecraft.registry.tag.TagKey
import net.minecraft.world.World
import java.util.function.Predicate

class DamageSourceBuilder(world: World, private val attacker: LivingEntity, private val source: Entity? = null){

    private val registry = world.registryManager.get(RegistryKeys.DAMAGE_TYPE)

    private var type: RegistryEntry<DamageType> = registry.entryOf(DamageTypes.PLAYER_ATTACK)
    private val additions: MutableList<RegistryEntry<DamageType>> = mutableListOf()
    private val exclusions: MutableList<RegistryEntry<DamageType>> = mutableListOf()

    fun set(type: RegistryKey<DamageType>): DamageSourceBuilder {
        this.type = registry.entryOf(type)
        return this
    }

    fun add(type: RegistryKey<DamageType>): DamageSourceBuilder {
        additions.add(registry.entryOf(type))
        return this
    }

    fun exclude(type: RegistryKey<DamageType>): DamageSourceBuilder {
        exclusions.add(registry.entryOf(type))
        return this
    }
    
    fun build(): DamageSource{
        val excludePredicate = Predicate<TagKey<DamageType>> {tag -> exclusions.forEach { if (it.isIn(tag)) return@Predicate true }; return@Predicate false}
        val addPredicate = Predicate<TagKey<DamageType>> {tag -> additions.forEach { if (it.isIn(tag)) return@Predicate true }; return@Predicate false}
        return CustomDamageSource(type,source, attacker,excludePredicate,addPredicate)
    }
}
