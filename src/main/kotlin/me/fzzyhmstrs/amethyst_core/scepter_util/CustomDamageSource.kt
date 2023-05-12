package me.fzzyhmstrs.amethyst_core.scepter_util

import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.text.Text

open class CustomDamageSource(name: String, private val cause: Entity?, private val shooter: Entity): DamageSource(name) {

    override fun getSource(): Entity? {
        return cause
    }

    override fun getAttacker(): Entity {
        return shooter
    }

    override fun getDeathMessage(entity: LivingEntity?): Text {
        val name = shooter.displayName
        val string = "death.attack." + this.name
        return if (cause == null){
            AcText.translatable(string,name)
        } else {
            val name2 = cause.displayName
            if (cause is LivingEntity){
                AcText.translatable("$string.mob",name2)
            } else {
                AcText.translatable("$string.causal", name, name2)
            }
        }
    }

}