package me.fzzyhmstrs.amethyst_core.mixins;

import me.fzzyhmstrs.amethyst_core.client.ClientAdvancementContainer;
import net.minecraft.advancement.Advancement;
import net.minecraft.advancement.AdvancementManager;
import net.minecraft.advancement.AdvancementProgress;
import net.minecraft.client.network.ClientAdvancementManager;
import net.minecraft.network.packet.s2c.play.AdvancementUpdateS2CPacket;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(ClientAdvancementManager.class)
public abstract class ClientAdvancementManagerMixin {

    @Shadow @Final
    private  Map<Advancement, AdvancementProgress> advancementProgresses;

    @Shadow
    public AdvancementManager getManager() {
        return null;
    }

    @Inject(at = @At("RETURN"), method = "onAdvancements")
    public void amethyst_core_onAdvancementSync(AdvancementUpdateS2CPacket packet, CallbackInfo info) {
        ClientAdvancementContainer.INSTANCE.onAdvancementPacket(advancementProgresses,getManager().getAdvancements());
    }
}
