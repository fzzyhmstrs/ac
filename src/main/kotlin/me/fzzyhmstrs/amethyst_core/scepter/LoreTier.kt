package me.fzzyhmstrs.amethyst_core.scepter

import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import net.minecraft.registry.Registries

/**
 * Enum that defines and stores spells of various tiers. Spells defined in a certain tier will randomly appear in Knowledge Books of the corresponding tier for use in crafting or otherwise.
 *
 * Recommendation for tier usage:
 *
 * [NO_TIER]: Beginner spells. Typically spells provided with a non-book recipe that the players can apply to their scepter from the start. The very basic missile and other base spells like Shine. Can be cast by any scepter
 *
 * [LOW_TIER]: Advanced Beginner spells. The first advanced tier of spells for players to be able to find and apply to their scepters. Can typcailly be cast by Tier 1 or Tier 2 (and greater) scepters.
 *
 * [HIGH_TIER]: Powerful Mid-End game spells. Spells that are found in the End, Nether, and Stronghold, or other mid-end game locations. Powerful effects like Lightning Storms. Cast by tier 3 or greater scepters.
 *
 * [EXTREME_TIER]: Godly spells for modded late-late game. Extremely powerful spells that rain destruction on opponents, fully heal the caster, or other such hyperbolic effects. Cast by tier 4 scepters.
 */
abstract class LoreTier {

    companion object{

        private var checked = false

        val LOW_TIER = object: LoreTier(){}
        val HIGH_TIER = object: LoreTier(){}
        val EXTREME_TIER = object: LoreTier(){}
        val ANY_TIER = object: LoreTier(){}
        val NO_TIER = object: LoreTier(){
            override fun addToList(string: String) {
            }
            override fun list(): Set<String> {
                return setOf()
            }
            override fun availableForAnyTier(): Boolean {
                return false
            }
        }
    }

    private val bookSet: MutableSet<String> = mutableSetOf()
    open fun addToList(string: String){
        bookSet.add(string)
    }
    open fun list(): Set<String>{
        check()
        return bookSet
    }
    open fun availableForAnyTier(): Boolean{
        return true
    }
    private fun check(){
        if (checked) return
        for (enchant in Registries.ENCHANTMENT){
            if (enchant is ScepterAugment){
                enchant.augmentData.bookOfLoreTier.addToList(enchant.id.toString())
                if (enchant.augmentData.bookOfLoreTier.availableForAnyTier())
                    ANY_TIER.addToList(enchant.id.toString())
            }
        }
    }
}

fun <T> MutableList<T>.addIfDistinct(element: T) {
    if (!this.contains(element)){
        this.add(element)
    }
}