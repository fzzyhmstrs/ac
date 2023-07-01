package me.fzzyhmstrs.amethyst_core.modifier

import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.LivingEntity

object GcChecker {

    val gearCoreLoaded: Boolean by lazy{
        FabricLoader.getInstance().isModLoaded("gear_core")
    }
    
    fun markDirty(entity: LivingEntity){
        if (gearCoreLoaded){
            GcCompat.markDirty(entity)
        }
    }

    fun registerProcessor(){
        if (gearCoreLoaded){
            GcCompat.registerAugmentModifierProcessor()
        }
    }

}
