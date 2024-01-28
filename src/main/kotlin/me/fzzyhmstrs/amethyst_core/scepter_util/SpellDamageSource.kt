package me.fzzyhmstrs.amethyst_core.scepter_util

import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.minecraft.entity.damage.DamageSource

/**
* Simple wrapper of a damage source that pairs a source with the spell that the damage is coming from.
*/
class SpellDamageSource(source: DamageSource, private val spell: ScepterAugment)
: 
DamageSource(
  source.typeRegistryEntry,
  source.source,
  source.attacker
)
{
    @Suppress("unused")
    fun getSpell(): ScepterAugment {
        return this.spell
    }
}
