package me.fzzyhmstrs.amethyst_core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.text.Text;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(ItemStack.class)
public class ItemStackMixin {

    private static ItemStack itemStack;

    @Inject(method = "getTooltip", at = @At(value = "INVOKE", target = "net/minecraft/item/ItemStack.appendEnchantments (Ljava/util/List;Lnet/minecraft/nbt/NbtList;)V"))
    private void amethyst_core_getItemStackForAddAugmentName(@Nullable PlayerEntity player, TooltipContext context, CallbackInfoReturnable<List<Text>> cir){
        itemStack = (ItemStack) (Object) this;
    }

    @WrapOperation(method = "method_17869", at = @At(value = "INVOKE", target = "net/minecraft/enchantment/Enchantment.getName (I)Lnet/minecraft/text/Text;"))
    private static Text amethyst_core_addAugmentName(Enchantment enchantment, int level, Operation<Text> operation){
        if(enchantment instanceof ScepterAugment){
            return ((ScepterAugment)enchantment).augmentName(itemStack,level);
        }
        return operation.call(level);
    }
}
