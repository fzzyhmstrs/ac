package me.fzzyhmstrs.amethyst_core.mixins;

import me.fzzyhmstrs.amethyst_core.interfaces.AugmentModifying;
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier;
import me.fzzyhmstrs.gear_core.modifier_util.EquipmentModifier;
import org.spongepowered.asm.mixin.Mixin;

import java.util.Arrays;

@Mixin(EquipmentModifier.class)
public class EquipmentModifierMixin implements AugmentModifying<EquipmentModifier> {

    @Override
    public EquipmentModifier withAugmentModifiers(AugmentModifier... modifier) {
        if (modifier.length > 0) {
            getAugmentModifiers().addAll(Arrays.stream(modifier).toList());
        }
        return (EquipmentModifier) (Object) this;
    }
}
