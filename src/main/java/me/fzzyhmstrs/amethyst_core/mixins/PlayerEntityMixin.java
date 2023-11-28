package me.fzzyhmstrs.amethyst_core.mixins;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import me.fzzyhmstrs.amethyst_core.AC;
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity;
import me.fzzyhmstrs.amethyst_core.interfaces.SyncedRandomProviding;
import me.fzzyhmstrs.amethyst_core.item_util.AbstractAugmentBookItem;
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute;
import me.fzzyhmstrs.fzzy_core.mana_util.ManaHelper;
import me.fzzyhmstrs.fzzy_core.registry.EventRegistry;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(PlayerEntity.class)
public class PlayerEntityMixin implements SyncedRandomProviding, SpellCastingEntity {

    @Unique
    private AbstractAugmentBookItem.SyncedRandomProvider provider;

    @Inject(method = "<init>",at = @At("TAIL"))
    private void amethyst_core_injectSyncedRandomProvidingInterface(World world, BlockPos pos, float yaw, GameProfile gameProfile, CallbackInfo ci){
        provider = new AbstractAugmentBookItem.SyncedRandomProvider();
    }
    
    @Override
    public Vec3d getRotationVec3d(){
        return ((PlayerEntity)(Object)this).getRotationVector();
    }

    @Override
    public AbstractAugmentBookItem.SyncedRandomProvider getProvider() {
        return provider;
    }

    @ModifyVariable(method = "addExperience", at = @At(value = "LOAD", ordinal = 0), argsOnly = true)
    private int amethyst_core_modifyXpUsingAttribute(int original){
        if (original == 0) return original;
        int newXp = original;
        if (original < 0){
            double bonus = ((PlayerEntity)(Object) this).getAttributeValue(RegisterAttribute.INSTANCE.getPLAYER_EXPERIENCE()) * -1.0;
            while (bonus <= -1.0){
                newXp -= 1;
                bonus += 1.0;
            }
            if (bonus < 0.0){
                newXp -= AC.INSTANCE.getAcRandom().nextDouble() < (bonus * -1.0) ? 1 : 0;
            } else {
                while (bonus >= 1.0){
                    newXp += 1;
                    bonus -= 1.0;
                }
                if (bonus > 0.0){
                    newXp += AC.INSTANCE.getAcRandom().nextDouble() < bonus ? 1 : 0;
                }
            }
            return newXp;
        }
        double bonus = ((PlayerEntity)(Object) this).getAttributeValue(RegisterAttribute.INSTANCE.getPLAYER_EXPERIENCE());
        while (bonus >= 1.0){
            newXp += 1;
            bonus -= 1.0;
        }
        if (bonus > 0.0){
            newXp += AC.INSTANCE.getAcRandom().nextDouble() < bonus ? 1 : 0;
        } else {
            while (bonus <= -1.0){
                newXp -= 1;
                bonus += 1.0;
            }
            if (bonus < 0.0){
                newXp -= AC.INSTANCE.getAcRandom().nextDouble() < (bonus * -1.0) ? 1 : 0;
            }
        }
        return newXp;
    }

    @WrapOperation(method = "applyDamage", at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.applyArmorToDamage (Lnet/minecraft/entity/damage/DamageSource;F)F"))
    private float amethyst_core_applyMultiplicationAttributeToDamage(PlayerEntity instance, DamageSource source, float amount, Operation<Float> operation){
        return operation.call(instance,source,amount * ((float)instance.getAttributeValue(RegisterAttribute.INSTANCE.getDAMAGE_MULTIPLICATION()))) ;
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void tickManaRegen(CallbackInfo ci){
        if (EventRegistry.INSTANCE.getTicker_20().isReady()){
            double manaRegen = ((LivingEntity)(Object)this).getAttributeValue(RegisterAttribute.INSTANCE.getMANA_REGENERATION());
            if (manaRegen > 0.0){
                int newXp = 0;
                while (manaRegen >= 1.0){
                    newXp += 1;
                    manaRegen -= 1.0;
                }
                if (manaRegen > 0.0) {
                    newXp += AC.INSTANCE.getAcRandom().nextDouble() < manaRegen ? 1 : 0;
                }
                if (newXp > 0){
                    List<ItemStack> stacks = ManaHelper.INSTANCE.getManaItems((PlayerEntity) (Object) this);
                    ManaHelper.INSTANCE.manaHealItems(stacks,((LivingEntity)(Object)this).getWorld(),newXp);
                }
            }
        }
    }
}
