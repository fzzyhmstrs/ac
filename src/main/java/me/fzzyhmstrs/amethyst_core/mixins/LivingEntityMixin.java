package me.fzzyhmstrs.amethyst_core.mixins;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fzzyhmstrs.amethyst_core.modifier_util.GcChecker;
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.DefaultAttributeContainer;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LivingEntity.class, priority = 100)
abstract class LivingEntityMixin {

    @Shadow public abstract double getAttributeValue(EntityAttribute attribute);

    @Inject(
            method = "createLivingAttributes()Lnet/minecraft/entity/attribute/DefaultAttributeContainer$Builder;",
            require = 1, allow = 1, at = @At("RETURN"))
    private static void amethyst_core_addAttributes(final CallbackInfoReturnable<DefaultAttributeContainer.Builder> info) {
        info.getReturnValue()
                .add(RegisterAttribute.INSTANCE.getSPELL_LEVEL())
                .add(RegisterAttribute.INSTANCE.getSPELL_COOLDOWN())
                .add(RegisterAttribute.INSTANCE.getSPELL_MANA_COST())
                .add(RegisterAttribute.INSTANCE.getSPELL_DAMAGE())
                .add(RegisterAttribute.INSTANCE.getSPELL_AMPLIFIER())
                .add(RegisterAttribute.INSTANCE.getSPELL_DURATION())
                .add(RegisterAttribute.INSTANCE.getSPELL_RANGE())
                .add(RegisterAttribute.INSTANCE.getSPELL_EXPERIENCE())
                .add(RegisterAttribute.INSTANCE.getDAMAGE_MULTIPLICATION())
                .add(RegisterAttribute.INSTANCE.getSHIELDING())
                .add(RegisterAttribute.INSTANCE.getMAGIC_RESISTANCE())
                .add(RegisterAttribute.INSTANCE.getPLAYER_EXPERIENCE());
    }

    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE", target = "net/minecraft/entity/LivingEntity.applyArmorToDamage (Lnet/minecraft/entity/damage/DamageSource;F)F"))
    private float amethyst_core_applyMultiplicationAttributeToDamage(LivingEntity instance, DamageSource source, float amount, Operation<Float> operation){
        return operation.call(instance,source,amount * ((float)this.getAttributeValue(RegisterAttribute.INSTANCE.getDAMAGE_MULTIPLICATION()))) ;
    }
    
    @WrapOperation(method = "getEquipmentChanges", at = @At(value = "INVOKE", target = "net/minecraft/item/ItemStack.getAttributeModifiers (Lnet/minecraft/entity/EquipmentSlot;)Lcom/google/common/collect/Multimap;"))
    private Multimap<EntityAttribute, EntityAttributeModifier> amethyst_core_markModifiersDirtyOnEquipChange(ItemStack instance, EquipmentSlot slot, Operation<Multimap<EntityAttribute, EntityAttributeModifier>> operation){
        GcChecker.INSTANCE.markDirty((LivingEntity)(Object) this);
        return operation.call(instance, slot);

    }
}
