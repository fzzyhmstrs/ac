package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
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
            list.addAll(boost.asStacks())
        }
        list
    }

    ////////////////////////////////////////////

    val BANE = register(BaneBoost())
    val EFFICIENCY = register(EfficiencyBoost())
    val FIRE_ASPECT = register(FireAspectBoost())
    val FLAME = register(FlameBoost())
    val FORTUNE = register(FortuneBoost())
    val IMPALING = register(ImpalingBoost())
    val LOOTING = register(LootingBoost())
    val MENDING = register(MendingBoost())
    val POWER = register(PowerBoost())
    val SHARPNESS = register(SharpnessBoost())
    val SILK_TOUCH = register(SilkTouchBoost())
    val SMITE = register(SmiteBoost())
    val QUICK_CHARGE = register(QuickChargeBoost())

    /////////////////////////////////////////////

    fun register(boost: AugmentBoost): AugmentBoost{
        return Registry.register(BOOSTS, boost.id, boost)
    }

    fun boostStacks(): List<ItemStack>{
        return boostStacks
    }

    fun findMatch(stack: ItemStack, augment: ScepterAugment): AugmentBoost?{
        for (boost in BOOSTS){
            if (boost.matches(stack) && boost.canAccept(augment)){
                return boost
            }
        }
        return null
    }

    fun findMatches(stack: ItemStack, augment: ScepterAugment): Set<AugmentBoost>{
        val boosts: MutableSet<AugmentBoost> = mutableSetOf()
        for (boost in BOOSTS){
            if (boost.matches(stack) && boost.canAccept(augment)){
                boosts.add(boost)
            }
        }
        return boosts
    }

    fun registerAll(){

    }

}
