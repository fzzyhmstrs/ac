package me.fzzyhmstrs.amethyst_core.mixins;

import dev.emi.trinkets.api.TrinketItem;
import me.fzzyhmstrs.amethyst_core.interfaces.AugmentTracking;
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier;
import me.fzzyhmstrs.amethyst_core.modifier_util.GcCompat;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;

import java.util.List;

@Pseudo
@Mixin(TrinketItem.class)
public class TrinketItemMixin implements AugmentTracking {


    @Override
    public List<AugmentModifier> getModifiers(ItemStack stack) {
        return GcCompat.INSTANCE.returnAugmentModifiers(stack);
    }

}
