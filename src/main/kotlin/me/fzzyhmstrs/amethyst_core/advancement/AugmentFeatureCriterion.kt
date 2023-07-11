package me.fzzyhmstrs.amethyst_core.advancement

import com.google.gson.JsonObject
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.advancement.criterion.AbstractCriterionConditions
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer
import net.minecraft.predicate.entity.LootContextPredicate
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

class AugmentFeatureCriterion(private val id: Identifier): AbstractCriterion<AugmentFeatureCriterion.FeatureConditions>() {

    override fun getId(): Identifier {
        return id
    }

    override fun conditionsFromJson(
        obj: JsonObject,
        playerPredicate: LootContextPredicate,
        predicateDeserializer: AdvancementEntityPredicateDeserializer
    ): FeatureConditions {
        if (obj.has("feature")){
            val el = obj.get("feature")
            if (el.isJsonPrimitive){
                val str = el.asString
                val feature = Identifier.tryParse(str)?:throw IllegalStateException("Spell $str not found in enchantment registry.")
                return FeatureConditions(this.id, feature,playerPredicate)
            } else {
                throw IllegalStateException("Spell Criterion not properly formatted in json object: ${obj.asString}")
            }
        } else {
            throw IllegalStateException("Spell Criterion not properly formatted in json object: ${obj.asString}")
        }
    }

    fun trigger(player: ServerPlayerEntity, spell: Identifier){
        this.trigger(player) { condition -> condition.test(spell) }
    }

    class FeatureConditions(id: Identifier, private val feature: Identifier, predicate: LootContextPredicate): AbstractCriterionConditions(id, predicate){
        fun test(id: Identifier): Boolean{
            return id == this.feature
        }
    }
}