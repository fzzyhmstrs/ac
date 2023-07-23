package me.fzzyhmstrs.amethyst_core.advancement

import me.fzzyhmstrs.amethyst_core.AC
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.util.Identifier

object FeatureCriteria {

    val PAIRED_FEATURE = AugmentFeatureCriterion(AC.identity("paired_feature"))

    fun registerServer(){
        Criteria.register(PAIRED_FEATURE)
    }
}