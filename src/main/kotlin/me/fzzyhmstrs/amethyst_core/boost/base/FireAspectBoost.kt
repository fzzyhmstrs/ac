package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.BoostConsumers
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.modifier.AugmentConsumer
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import net.minecraft.enchantment.Enchantments
import net.minecraft.util.Identifier

class FireAspectBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"fire_aspect_boost"), Enchantments.FIRE_ASPECT, 2) {

    companion object{
        val CONSUMER = AugmentConsumer.createAndRegisterConsumer(Identifier(AC.MOD_ID,"fire_aspect_boost_consumer"),BoostConsumers.FIRE_ASPECT_CONSUMER, AugmentConsumer.Type.HARMFUL)
    }

    override val boostEffect: AugmentEffect
        get() = super.boostEffect.withConsumers(CONSUMER)

}