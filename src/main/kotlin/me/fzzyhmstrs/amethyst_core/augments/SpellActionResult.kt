package me.fzzyhmstrs.amethyst_core.augments

import net.minecraft.util.Identifier

class SpellActionResult private constructor (private val type: Type, list: MutableList<Identifier> = mutableListOf()) {

    private val results: MutableList<Identifier> = list

    fun success(): Boolean{
        return type.succeeds
    }

    fun results(): List<Identifier>{
        return results
    }

    fun overwrite(): Boolean{
        return type == Type.OVERWRITE
    }

    fun acted(): Boolean{
        return results.isNotEmpty()
    }
    
    fun withResults(vararg results: Identifier): SpellActionResult{
        this.results.addAll(results)
        return this
    }
    fun withResults(results: List<Identifier>): SpellActionResult{
        this.results.addAll(results)
        return this
    }

    fun copyTypeAndAddResults(previous: SpellActionResult): SpellActionResult{
        return SpellActionResult(previous.type,previous.results).withResults(this.results)
    }
    
    companion object{
        fun success(vararg results: Identifier): SpellActionResult{
            return SpellActionResult(Type.SUCCESS,results.toMutableList())
        }
        fun success(results: List<Identifier>): SpellActionResult{
            return SpellActionResult(Type.SUCCESS,results.toMutableList())
        }
        fun success(previous: SpellActionResult): SpellActionResult{
            return SpellActionResult(Type.SUCCESS,previous.results.toMutableList())
        }
        fun pass(vararg results: Identifier): SpellActionResult{
            return SpellActionResult(Type.PASS,results.toMutableList())
        }
        fun pass(results: List<Identifier>): SpellActionResult{
            return SpellActionResult(Type.PASS,results.toMutableList())
        }
        fun pass(previous: SpellActionResult): SpellActionResult{
            return SpellActionResult(Type.PASS,previous.results.toMutableList())
        }
        fun overwrite(vararg results: Identifier): SpellActionResult{
            return SpellActionResult(Type.OVERWRITE,results.toMutableList())
        }
        fun overwrite(results: List<Identifier>): SpellActionResult{
            return SpellActionResult(Type.OVERWRITE,results.toMutableList())
        }
        fun overwrite(previous: SpellActionResult): SpellActionResult{
            return SpellActionResult(Type.OVERWRITE,previous.results.toMutableList())
        }
        fun fail(vararg results: Identifier): SpellActionResult{
            return SpellActionResult(Type.PASS,results.toMutableList())
        }
        fun fail(results: List<Identifier>): SpellActionResult{
            return SpellActionResult(Type.PASS,results.toMutableList())
        }
        fun fail(previous: SpellActionResult): SpellActionResult{
            return SpellActionResult(Type.PASS,previous.results.toMutableList())
        }
    }
    
    enum class Type(internal val succeeds: Boolean){
        SUCCESS(true),
        PASS(true),
        FAIL(false),
        OVERWRITE(true)
    }
}
