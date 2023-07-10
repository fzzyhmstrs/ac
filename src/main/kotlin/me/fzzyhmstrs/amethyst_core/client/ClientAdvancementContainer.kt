package me.fzzyhmstrs.amethyst_core.client

import net.minecraft.advancement.Advancement
import net.minecraft.advancement.AdvancementProgress
import net.minecraft.util.Identifier


object ClientAdvancementContainer {

    private val doneAdvancements: MutableSet<Identifier> = mutableSetOf()

    fun onAdvancementPacket(progressMap: Map<Advancement, AdvancementProgress>, advancements: Collection<Advancement>){
        doneAdvancements.clear()
        for (advancement in advancements){
            if(progressMap[advancement]?.isDone == true){
                doneAdvancements.add(advancement.id)
            }
        }
    }

    fun isDone(advancement: Identifier): Boolean{
        return doneAdvancements.contains(advancement)
    }
}