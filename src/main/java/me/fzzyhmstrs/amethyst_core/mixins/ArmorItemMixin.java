package me.fzzyhmstrs.amethyst_core.mixins;

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier;
import me.fzzyhmstrs.amethyst_core.modifier_util.GcCompat;
import me.fzzyhmstrs.amethyst_core.interfaces.*;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

import java.util.List;

@Mixin(ArmorItem.class)
public class ArmorItemMixin implements AugmentTracking {

    @Override
    public List<AugmentModifier> getModifiers(ItemStack stack) {
        return GcCompat.INSTANCE.returnAugmentModifiers(stack);
    }
}
