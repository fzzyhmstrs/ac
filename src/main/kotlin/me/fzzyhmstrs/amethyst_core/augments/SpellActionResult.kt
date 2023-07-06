package me.fzzyhmstrs.amethyst_core.augments

import net.minecraft.enchantment.Enchantment
import net.minecraft.util.Identifier

class SpellActionResult private constructor (private val type: Type, list: MutableList<Identifier> = mutableListOf()) {

    private val results: MutableList<Identifier>

    fun success(): Boolean{
        return type.succeeds
    }

    fun results(): List<Identifier>{
        return results
    }
    
    fun withResults(vararg results: Identifier){
        this.results.addAll(results)
        return this
    }
    fun withResults(results: List<Identifier>){
        this.results.addAll(results)
        return this
    }
    
    companion object{
        fun success(vararg results: Identifier){
            return SpellActionResult(Type.SUCCESS,results.asMutableList())
        }
        fun success(results: List<Identifier>){
            return SpellActionResult(Type.SUCCESS,results.asMutableList())
        }
        fun success(previous: SpellActionResult){
            return SpellActionResult(Type.SUCCESS,other.results.asMutableList())
        }
        fun pass(vararg results: Identifier){
            return SpellActionResult(Type.PASS,results.asMutableList())
        }
        fun pass(results: List<Identifier>){
            return SpellActionResult(Type.PASS,results.asMutableList())
        }
        fun pass(previous: SpellActionResult){
            return SpellActionResult(Type.PASS,other.results.asMutableList())
        }
        fun overwrite(vararg results: Identifier){
            return SpellActionResult(Type.OVERWRITE,results.asMutableList())
        }
        fun overwrite(results: List<Identifier>){
            return SpellActionResult(Type.OVERWRITE,results.asMutableList())
        }
        fun overwrite(previous: SpellActionResult){
            return SpellActionResult(Type.OVERWRITE,other.results.asMutableList())
        }
        fun fail(vararg results: Identifier){
            return SpellActionResult(Type.PASS,results.asMutableList())
        }
        fun fail(results: List<Identifier>){
            return SpellActionResult(Type.PASS,results.asMutableList())
        }
        fun fail(previous: SpellActionResult){
            return SpellActionResult(Type.PASS,other.results.asMutableList())
        }
    }
    
    enum class Type(private val succeeds: Boolean){
        SUCCESS(true),
        PASS(true),
        FAIL(false),
        OVERWRITE(true)
    }
}
