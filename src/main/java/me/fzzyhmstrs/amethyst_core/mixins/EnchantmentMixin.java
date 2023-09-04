package me.fzzyhmstrs.amethyst_core.mixins;

import me.fzzyhmstrs.amethyst_core.scepter_util.augments.AugmentHelper;
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment;
import me.fzzyhmstrs.fzzy_core.coding_util.AcText;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.Registries;
import net.minecraft.text.MutableText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class EnchantmentMixin{

    @Shadow protected abstract String getOrCreateTranslationKey();

    @Shadow public abstract int getMaxLevel();

    @Inject(method = "getName", at = @At(value = "HEAD"), cancellable = true)
    private void amethyst_core_disabledAugmentName(int level, CallbackInfoReturnable<Text> cir){
        Enchantment enchant = (Enchantment)(Object)this;
        if (enchant instanceof ScepterAugment) {
            Identifier id = Registries.ENCHANTMENT.getId(enchant);
            if (id != null){
                if(!AugmentHelper.INSTANCE.getAugmentEnabled(id.toString())){
                    MutableText mutableText = AcText.INSTANCE.translatable(getOrCreateTranslationKey());
                    if (level != 1 || this.getMaxLevel() != 1) {
                        mutableText.append(" ").append(AcText.INSTANCE.translatable("enchantment.level." + level));
                    }
                    mutableText.append(AcText.INSTANCE.translatable("scepter.augment.disabled"));
                    mutableText.formatted(Formatting.DARK_RED).formatted(Formatting.STRIKETHROUGH);
                    cir.setReturnValue(mutableText);
                }
            }
        }
    }
}
