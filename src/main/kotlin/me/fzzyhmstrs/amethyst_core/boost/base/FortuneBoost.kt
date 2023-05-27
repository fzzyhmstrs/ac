package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.AugmentType
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.ProcessContext
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.item.ItemStack
import net.minecraft.loot.LootTable
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.world.World

class FortuneBoost: EnchantmentAugmentBoost(Enchantments.FORTUNE,3) {

    override fun provideLevel(enchantment: Enchantment): Int {
        return if (enchantment == Enchantments.FORTUNE) 3 else 0
    }

    override fun appendDescription(description: MutableList<Text>) {
        description.add(AcText.translatable("boost.amethyst_core.fortune"))
    }
}