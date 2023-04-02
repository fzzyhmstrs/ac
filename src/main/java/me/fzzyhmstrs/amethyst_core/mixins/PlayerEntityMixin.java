package me.fzzyhmstrs.amethyst_core.mixins;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReceiver;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalIntRef;
import com.mojang.authlib.GameProfile;
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity;
import me.fzzyhmstrs.amethyst_core.interfaces.SyncedRandomProviding;
import me.fzzyhmstrs.amethyst_core.item_util.AbstractAugmentBookItem;
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.network.encryption.PlayerPublicKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements SyncedRandomProviding, SpellCastingEntity {

    @Unique
    private AbstractAugmentBookItem.SyncedRandomProvider provider;

    @Inject(method = "<init>",at = @At("TAIL"))
    private void amethyst_core_injectSyncedRandomProvidingInterface(World world, BlockPos pos, float yaw, GameProfile gameProfile, PlayerPublicKey publicKey, CallbackInfo ci){
        provider = new AbstractAugmentBookItem.SyncedRandomProvider();
    }

    @Override
    public AbstractAugmentBookItem.SyncedRandomProvider getProvider() {
        return provider;
    }

    @ModifyVariable(method = "addExperience", at = @At("LOAD"), argsOnly = true)
    private int amethyst_core_modifyXpUsingAttribute(int original){
        int newXp = original;
        double bonus = (int)((PlayerEntity)(Object) this).getAttributeValue(RegisterAttribute.INSTANCE.getPLAYER_EXPERIENCE());
        while (bonus > 1.0){
            newXp += 1;
            bonus -= 1.0;
        }
        if (bonus > 0.0){
            newXp += ((PlayerEntity)(Object) this).world.random.nextDouble() < bonus ? 1 : 0;
        }
        return newXp;
    }
}
