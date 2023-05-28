package me.fzzyhmstrs.amethyst_core.boost

import net.minecraft.entity.LivingEntity
import java.util.function.Consumer

object BoostConsumers {

    val FIRE_ASPECT_CONSUMER = Consumer { list: List<LivingEntity> -> fireAspectConsumer(list) }
    private fun fireAspectConsumer(list: List<LivingEntity>){
        list.forEach {
            it.setOnFireFor(5)
        }
    }
}
