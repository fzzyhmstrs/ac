package me.fzzyhmstrs.amethyst_core.scepter.augments.paired

import me.fzzyhmstrs.amethyst_core.AC
import net.minecraft.util.Identifier

interface ProcessContext {
    fun getType(): Identifier
    companion object{
        val EMPTY_ID = Identifier(AC.MOD_ID,"empty_context")
        val EMPTY = object : ProcessContext{
            override fun getType(): Identifier {
                return EMPTY_ID
            }
        }
    }
}