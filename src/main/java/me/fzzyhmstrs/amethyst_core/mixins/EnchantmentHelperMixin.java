package me.fzzyhmstrs.amethyst_core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fzzyhmstrs.amethyst_core.augments.AugmentHelper;
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(EnchantmentHelper.class)
public abstract class EnchantmentHelperMixin {

    @WrapOperation(method = "getEquipmentLevel", at = @At(value = "INVOKE", target = "net/minecraft/enchantment/EnchantmentHelper.getLevel (Lnet/minecraft/enchantment/Enchantment;Lnet/minecraft/item/ItemStack;)I"))
    private static int amethyst_core_modifyEquipmentLevelWithBoosts(Enchantment enchantment, ItemStack stack, Operation<Integer> operation){
        PairedAugments pair = AugmentHelper.INSTANCE.getPairedAugments(stack);
        int level = operation.call(enchantment,stack);
        if (pair != null){
            return level + pair.provideLevel(enchantment);
        }
        return level;
    }

}
