package me.fzzyhmstrs.amethyst_core.interfaces;

import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier;
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier;

import java.util.ArrayList;
import java.util.List;

public interface AugmentModifying<T extends AbstractModifier<T>> {

    default List<AugmentModifier> getAugmentModifiers(){
        return new ArrayList<>();
    }

    T withAugmentModifiers(AugmentModifier...modifier);
}
