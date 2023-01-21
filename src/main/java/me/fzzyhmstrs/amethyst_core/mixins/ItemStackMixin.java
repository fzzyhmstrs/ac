package me.fzzyhmstrs.amethyst_core.mixins;

import com.google.common.collect.Multimap;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import me.fzzyhmstrs.amethyst_core.interfaces.*;
import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier;
import me.fzzyhmstrs.amethyst_core.modifier_util.EquipmentModifier;
import me.fzzyhmstrs.amethyst_core.modifier_util.EquipmentModifierHelper;
import me.fzzyhmstrs.amethyst_core.trinket_util.TrinketChecker;
import me.fzzyhmstrs.amethyst_core.trinket_util.TrinketUtil;
import net.minecraft.block.BlockState;
import net.minecraft.client.item.TooltipContext;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.attribute.EntityAttribute;
import net.minecraft.entity.attribute.EntityAttributeModifier;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemConvertible;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.text.Text;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(ItemStack.class)
public abstract class ItemStackMixin implements DurabilityTracking {

    @Shadow public abstract Item getItem();

    @Shadow public abstract int getMaxDamage();

    @Shadow public @Nullable abstract NbtCompound getNbt();

    @Shadow public abstract NbtCompound getOrCreateNbt();

    @Unique
    private int amethyst_core_newMaxDamage;

    @Override
    public void evaluateNewMaxDamage(AbstractModifier.CompiledModifiers<EquipmentModifier> compiledModifiers) {
        if (getMaxDamage() != 0) {
            amethyst_core_newMaxDamage = Math.max(compiledModifiers.getCompiledData().modifyDurability(getItem().getMaxDamage()), 1);
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/nbt/NbtCompound;)V", at = @At("TAIL"))
    private void amethyst_core_initializeFromNbt(NbtCompound nbt, CallbackInfo ci){
        if (getItem() == null) return;
        amethyst_core_newMaxDamage = getItem().getMaxDamage();
        if (getItem() instanceof Modifiable modifiableItem){
            modifiableItem.getModifierInitializer().initializeModifiers((ItemStack) (Object) this, getOrCreateNbt(), modifiableItem.defaultModifiers());
        }
    }

    @Inject(method = "<init>(Lnet/minecraft/item/ItemConvertible;I)V", at = @At("TAIL"))
    private void amethyst_core_initializeFromItem(ItemConvertible item, int count, CallbackInfo ci){
        if (getItem() == null) return;
        amethyst_core_newMaxDamage = getItem().getMaxDamage();
        if (getItem() instanceof Modifiable modifiableItem){
            modifiableItem.getModifierInitializer().initializeModifiers((ItemStack) (Object) this, getOrCreateNbt(), modifiableItem.defaultModifiers());
        }
    }

    @WrapOperation(method = "getMaxDamage", at = @At(value = "INVOKE", target = "net/minecraft/item/Item.getMaxDamage ()I"))
    private int amethyst_core_maxDamageFromNewMax(Item instance, Operation<Integer> operation){
        operation.call(instance);
        return amethyst_core_newMaxDamage;
    }

    @ModifyReturnValue(method = "getAttributeModifiers", at = @At("RETURN"))
    private Multimap<EntityAttribute, EntityAttributeModifier> amethyst_core_addModifierModifiersToModifiers(Multimap<EntityAttribute, EntityAttributeModifier> original, EquipmentSlot slot){
        return EquipmentModifierHelper.INSTANCE.getAttributeModifiers((ItemStack) (Object) this, slot, original);
    }

    @Inject(method = "postHit", at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.incrementStat (Lnet/minecraft/stat/Stat;)V"))
    private void amethyst_core_invokePostWearerHit(LivingEntity target, PlayerEntity attacker, CallbackInfo ci){
            if (TrinketChecker.INSTANCE.getTrinketsLoaded()) {
                List<ItemStack> stacks = TrinketUtil.INSTANCE.getTrinketStacks(attacker);
                for (ItemStack stack : stacks) {
                    if (stack.getItem() instanceof HitTracking hitTrackingItem) {
                        hitTrackingItem.postWearerHit(stack, attacker, target);
                    }
                }
            }
            attacker.getArmorItems().forEach(stack -> {
                if (stack.getItem() instanceof HitTracking hitTrackingItem){
                    hitTrackingItem.postWearerHit(stack, attacker, target);
                }
            });
            ItemStack mainhand = attacker.getEquippedStack(EquipmentSlot.MAINHAND);
            if (mainhand.getItem() instanceof HitTracking hitTrackingItem){
                hitTrackingItem.postWearerHit(mainhand, attacker, target);
            }
            ItemStack offhand = attacker.getEquippedStack(EquipmentSlot.OFFHAND);
            if (offhand.getItem() instanceof HitTracking hitTrackingItem){
                hitTrackingItem.postWearerHit(offhand, attacker, target);
            }
    }

    @Inject(method = "postMine", at = @At(value = "INVOKE", target = "net/minecraft/entity/player/PlayerEntity.incrementStat (Lnet/minecraft/stat/Stat;)V"))
    private void amethyst_core_invokePostWearerMine(World world, BlockState state, BlockPos pos, PlayerEntity miner, CallbackInfo ci){
        if (TrinketChecker.INSTANCE.getTrinketsLoaded()) {
            List<ItemStack> stacks = TrinketUtil.INSTANCE.getTrinketStacks(miner);
            for (ItemStack stack : stacks) {
                if (stack.getItem() instanceof MineTracking mineTrackingItem) {
                    mineTrackingItem.postWearerMine(stack, world,state,pos,miner);
                }
            }
        }
        miner.getArmorItems().forEach(stack -> {
            if (stack.getItem() instanceof MineTracking mineTrackingItem) {
                mineTrackingItem.postWearerMine(stack, world,state,pos,miner);
            }
        });
        ItemStack mainhand = miner.getEquippedStack(EquipmentSlot.MAINHAND);
        if (mainhand.getItem() instanceof MineTracking mineTrackingItem) {
            mineTrackingItem.postWearerMine(mainhand, world,state,pos,miner);
        }
        ItemStack offhand = miner.getEquippedStack(EquipmentSlot.OFFHAND);
        if (offhand.getItem() instanceof MineTracking mineTrackingItem) {
            mineTrackingItem.postWearerMine(offhand, world,state,pos,miner);
        }
    }

    @WrapOperation(method = "use", at = @At(value = "INVOKE", target = "net/minecraft/item/Item.use (Lnet/minecraft/world/World;Lnet/minecraft/entity/player/PlayerEntity;Lnet/minecraft/util/Hand;)Lnet/minecraft/util/TypedActionResult;"))
    private TypedActionResult<ItemStack> amethyst_core_invokeOnWearerUse(Item instance, World world, PlayerEntity user, Hand hand, Operation<TypedActionResult<ItemStack>> operation){
        TypedActionResult<ItemStack> useResult = operation.call(instance, world, user, hand);
        if (!useResult.getResult().isAccepted()){
            ItemStack stack = user.getStackInHand(hand);
            if (stack.getItem() instanceof UseTracking useTrackingItem) {
                useTrackingItem.onWearerUse(user.getStackInHand(hand), world, user, hand);
            }
        }
        return useResult;
    }

    @WrapOperation(method = "getTooltip", at = @At(value = "INVOKE", target = "net/minecraft/item/Item.appendTooltip (Lnet/minecraft/item/ItemStack;Lnet/minecraft/world/World;Ljava/util/List;Lnet/minecraft/client/item/TooltipContext;)V"))
    private void amethyst_core_appendModifiersToTooltip(Item instance,ItemStack stack, World world, List<Text> tooltip, TooltipContext context, Operation<Void> operation){
        operation.call(instance,stack, world, tooltip,context);
        if (stack.getItem() instanceof Modifiable modifiable){
            modifiable.addModifierTooltip(stack, tooltip, context);
        }
    }

}
