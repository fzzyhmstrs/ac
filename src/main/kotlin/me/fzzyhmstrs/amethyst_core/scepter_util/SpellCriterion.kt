package me.fzzyhmstrs.amethyst_core.scepter_util

import com.google.gson.JsonObject
import me.fzzyhmstrs.amethyst_core.AC
import net.minecraft.advancement.criterion.AbstractCriterion
import net.minecraft.advancement.criterion.AbstractCriterionConditions
import net.minecraft.predicate.entity.AdvancementEntityPredicateDeserializer
import net.minecraft.predicate.entity.LootContextPredicate
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.Identifier

class SpellCriterion(private val id: Identifier): AbstractCriterion<SpellCriterion.SpellConditions>() {

    companion object{
        private val anySpell = Identifier(AC.MOD_ID,"any")
    }
    
    override fun getId(): Identifier {
        return id
    }

    override fun conditionsFromJson(
        obj: JsonObject,
        playerPredicate: LootContextPredicate,
        predicateDeserializer: AdvancementEntityPredicateDeserializer
    ): SpellConditions {
        if (obj.has("spell")){
            val el = obj.get("spell")
            if (el.isJsonPrimitive){
                val str = el.asString
                val spell = Identifier.tryParse(str)?:throw IllegalStateException("Spell $str not found in enchantment registry.")
                return SpellConditions(id, spell,playerPredicate)
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

    class SpellConditions(id: Identifier, private val spell: Identifier, predicate: LootContextPredicate): AbstractCriterionConditions(id, predicate){
        fun test(spell: Identifier): Boolean{
            if (this.spell == anySpell) return true 
            return spell == this.spell
        }
    }
}
