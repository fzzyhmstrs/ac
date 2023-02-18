package me.fzzyhmstrs.amethyst_core.mixins;

import com.mojang.authlib.GameProfile;
import me.fzzyhmstrs.amethyst_core.interfaces.SyncedRandomProviding;
import me.fzzyhmstrs.amethyst_core.item_util.AbstractAugmentBookItem;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@SuppressWarnings("ConstantConditions")
@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements SyncedRandomProviding {

    @Unique
    private AbstractAugmentBookItem.SyncedRandomProvider<?> provider;

    @Inject(method = "<init>",at = @At("TAIL"))
    private void amethyst_core_injectSyncedRandomProvidingInterface(World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci){
        if (((PlayerEntity)(Object)this instanceof ServerPlayerEntity)){
            provider = new AbstractAugmentBookItem.ServerSyncedRandomProvider();
        } else if (((PlayerEntity)(Object)this instanceof ClientPlayerEntity)) {
            provider = new AbstractAugmentBookItem.ClientSyncedRandomProvider();
        }
    }

    @Override
    public AbstractAugmentBookItem.SyncedRandomProvider<?> getProvider() {
        return provider;
    }
}
