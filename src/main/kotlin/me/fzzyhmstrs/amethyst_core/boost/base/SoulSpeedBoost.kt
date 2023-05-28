package me.fzzyhmstrs.amethyst_core.boost.base

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.boost.EnchantmentAugmentBoost
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.Enchantments
import net.minecraft.item.ItemStack
import net.minecraft.registry.RegistryKeys
import net.minecraft.registry.tag.TagKey
import net.minecraft.text.Text
import net.minecraft.util.Identifier

class SoulSpeedBoost: EnchantmentAugmentBoost(Identifier(AC.MOD_ID,"soul_speed_boost"), Enchantments.SOUL_SPEED, 3) {

    private val SOUL_AUGMENTS: TagKey<Enchantment> = TagKey.of(RegistryKeys.ENCHANTMENT, Identifier(AC.MOD_ID,"soul_augments"))

    override val cooldownModifier: PerLvlI
        get() = PerLvlI(0,0,-20)

    override fun matches(stack: ItemStack, augment: ScepterAugment): Boolean {
        return super.matches(stack, augment) && isInTag(augment,SOUL_AUGMENTS)
    }
}