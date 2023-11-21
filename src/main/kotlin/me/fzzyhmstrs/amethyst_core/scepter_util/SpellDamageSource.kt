package me.fzzyhmstrs.amethyst_core.scepter_util

import net.minecraft.entity.Entity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.damage.DamageTypes
import net.minecraft.world.World
/**
* Simple wrapper of a damage source that pairs a source with the spell that the damage is coming from.
*/
class SpellDamageSource(source: DamageSource, private val spell: ScepterAugment)
: 
DamageSource(
  source.getTypeRegistryEntry(),
  source.getSource(),
  source.getAttacker(),
  source.getStoredPosition())
{
    fun getSpell(): ScepterAugment {
        return this.spell
    }
}
