package me.fzzyhmstrs.amethyst_core.mixins;

import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
abstract class LivingEntityMixin {

    @Inject(
            method = "createLivingAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;",
            require = 1, allow = 1, at = @At("RETURN"))
    private static void addAttributes(final CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        info.getReturnValue()
                .add(RegisterAttribute.INSTANCE.getSPELL_LEVEL())
                .add(RegisterAttribute.INSTANCE.getSPELL_COOLDOWN())
                .add(RegisterAttribute.INSTANCE.getSPELL_MANA_COST())
                .add(RegisterAttribute.INSTANCE.getSPELL_DAMAGE())
                .add(RegisterAttribute.INSTANCE.getSPELL_AMPLIFIER())
                .add(RegisterAttribute.INSTANCE.getSPELL_DURATION())
                .add(RegisterAttribute.INSTANCE.getSPELL_RANGE())
                .add(RegisterAttribute.INSTANCE.getENCHANTABILITY());
    }
}