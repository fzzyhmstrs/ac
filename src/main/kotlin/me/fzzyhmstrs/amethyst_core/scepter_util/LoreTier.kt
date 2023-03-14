package me.fzzyhmstrs.amethyst_core.scepter_util

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
        val LOW_TIER = object: LoreTier() {
            private val bookOfLoreListT1: MutableList<String> =  mutableListOf()

            override fun addToList(string: String) {
                bookOfLoreListT1.addIfDistinct(string)
            }
            override fun list(): List<String> {
                return bookOfLoreListT1
            }
        }
        val HIGH_TIER = object: LoreTier() {
            private val bookOfLoreListT2: MutableList<String> =  mutableListOf()

            override fun addToList(string: String) {
                bookOfLoreListT2.addIfDistinct(string)
            }
            override fun list(): List<String> {
                return bookOfLoreListT2
            }
        }
        val EXTREME_TIER = object: LoreTier() {
            private val bookOfLoreListT3: MutableList<String> =  mutableListOf()

            override fun addToList(string: String) {
                bookOfLoreListT3.addIfDistinct(string)
            }
            override fun list(): List<String> {
                return bookOfLoreListT3
            }
        }
        val ANY_TIER = object: LoreTier() {
            private val bookOfLoreListT12: MutableList<String> =  mutableListOf()

            override fun addToList(string: String) {
                bookOfLoreListT12.addIfDistinct(string)
            }
            override fun list(): List<String> {
                return bookOfLoreListT12
            }

        }
        val NO_TIER = object: LoreTier() {
            override fun addToList(string: String) {
            }

            override fun list(): List<String> {
                return listOf()
            }
        }
    }

    abstract fun addToList(string: String)
    abstract fun list(): List<String>
}

fun <T> MutableList<T>.addIfDistinct(element: T) {
    if (!this.contains(element)){
        this.add(element)
    }
}