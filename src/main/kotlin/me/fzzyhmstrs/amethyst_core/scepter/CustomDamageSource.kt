package me.fzzyhmstrs.amethyst_core.scepter

import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.text.Text

open class CustomDamageSource(name: String, internal val source: Entity?, internal val attacker: LivingEntity): DamageSource(name) {

    override fun getSource(): Entity? {
        return source
    }

    override fun getAttacker(): Entity {
        return attacker
    }

    override fun getDeathMessage(entity: LivingEntity?): Text {
        val name = attacker.displayName
        val string = "death.attack." + this.name
        return if (source == null){
            AcText.translatable(string,name)
        } else {
            val name2 = source.displayName
            if (source is LivingEntity){
                AcText.translatable("$string.mob",name2)
            } else {
                AcText.translatable("$string.causal", name, name2)
            }
        }
    }

}