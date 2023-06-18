package me.fzzyhmstrs.amethyst_core.mixins;

import me.fzzyhmstrs.amethyst_core.item_util.ScepterLike;
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.Hand;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerInventory.class)
public abstract class PlayerInventoryMixin {
    @Shadow @Final
    public PlayerEntity player;


    //@Shadow public abstract void scrollInHotbar(double scrollAmount);

    @Inject(at = @At("HEAD"), method = "scrollInHotbar", cancellable = true)
    private void amethyst_core_scrollInHotbar(double scrollAmount, CallbackInfo ci) {
        //System.out.println(player.getStackInHand(Hand.MAIN_HAND).getItem().toString());
        if (player.getStackInHand(Hand.MAIN_HAND).getItem() instanceof ScepterLike && player.getWorld().isClient){
            ClientPlayerEntity entity = (ClientPlayerEntity) player;
            if (entity.input.sneaking){
                ScepterHelper.INSTANCE.sendScepterUpdateFromClient(scrollAmount < 0.0D);
                ci.cancel();
            }
        }

    }


}
