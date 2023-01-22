package me.fzzyhmstrs.amethyst_core.interfaces;

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier;
import net.minecraft.item.ItemStack;

import java.util.List;

public interface AugmentTracking {
    default List<AugmentModifier> getModifiers(ItemStack stack){
        return List.of();
    }
}
