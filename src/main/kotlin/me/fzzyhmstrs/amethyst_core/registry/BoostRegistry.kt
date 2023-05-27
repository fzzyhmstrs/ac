package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.AugmentBoost
import me.fzzyhmstrs.amethyst_core.boost.base.*
import net.fabricmc.fabric.api.event.registry.FabricRegistryBuilder
import net.minecraft.item.ItemStack
import net.minecraft.registry.Registry
import net.minecraft.registry.SimpleRegistry
import net.minecraft.util.Identifier

object BoostRegistry {

    val BOOSTS: SimpleRegistry<AugmentBoost> = FabricRegistryBuilder.createSimple(AugmentBoost::class.java, Identifier(AC.MOD_ID,"augment_boosts")).buildAndRegister()
    private val boostStacks: List<ItemStack> by lazy {
        val list: MutableList<ItemStack> = mutableListOf()
        for (boost in BOOSTS){
            list.add(boost.asStack())
        }
        list
    }

    val SHARPNESS = Registry.register(BOOSTS, Identifier(AC.MOD_ID,"sharpness_boost"), SharpnessBoost())
    val SMITE = Registry.register(BOOSTS, Identifier(AC.MOD_ID,"smite_boost"), SmiteBoost())
    val BANE = Registry.register(BOOSTS, Identifier(AC.MOD_ID,"bane_boost"), BaneBoost())
    val LOOTING = Registry.register(BOOSTS, Identifier(AC.MOD_ID,"looting_boost"), LootingBoost())
    val FORTUNE = Registry.register(BOOSTS, Identifier(AC.MOD_ID,"fortune_boost"), FortuneBoost())
    val SILK_TOUCH = Registry.register(BOOSTS, Identifier(AC.MOD_ID,"silk_touch_boost"), SilkTouchBoost())
    val EFFICIENCY = Registry.register(BOOSTS, Identifier(AC.MOD_ID,"efficiency_boost"), EfficiencyBoost())
    val MENDING = Registry.register(BOOSTS, Identifier(AC.MOD_ID,"mending_boost"), MendingBoost())

    fun boostStacks(): List<ItemStack>{
        return boostStacks
    }

    fun findMatch(stack: ItemStack): AugmentBoost?{
        for (boost in BOOSTS){
            if (boost.matches(stack)){
                return boost
            }
        }
        return null
    }

    fun registerAll(){

    }

}