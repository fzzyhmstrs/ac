package me.fzzyhmstrs.amethyst_core.compat.spell_power

import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.fabricmc.loader.api.FabricLoader
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack

object SpChecker {

    val spellPowerLoaded: Boolean by lazy{
        FabricLoader.getInstance().isModLoaded("spell_power")
    }

    fun getHaste(user: LivingEntity, stack: ItemStack): Double{
        if (spellPowerLoaded)
            return SpCompat.getHaste(user,stack)
        return 1.0
    }

    fun getModFromPower(user: LivingEntity, spell: ScepterAugment): Double{
        if (spellPowerLoaded)
            return SpCompat.getModFromPower(user, spell)
        return 1.0
    }
}