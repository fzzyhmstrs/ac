package me.fzzyhmstrs.amethyst_core.coding_util


import net.minecraft.text.Text
import net.minecraft.text.MutableText

object AcText{

    fun translatable(key: String, vararg args: Any): MutableText{
        return Text.translatable(key, *args)
    }
    
    fun literal(text: String): MutableText{
        return Text.literal(text)
    }
    
    fun empty(): MutableText{
        return Text.empty()
    }

    fun appended(baseText: MutableText, vararg appended: Text): MutableText {
        appended.forEach {
            baseText.append(it)
        }
        return baseText
    }
}