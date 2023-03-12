package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World

interface SpellCasting {
    fun <T> serverUse(world: World, user: T, hand: Hand, stack: ItemStack, activeEnchantId: String, spell: ScepterAugment, testLevel: Int)
    : TypedActionResult<ItemStack>
    where T: LivingEntity,
    T: SpellCastingEntity
    {
        return ScepterHelper.castSpell(world,user,hand,stack,spell,activeEnchantId,testLevel,this)
    }
    fun clientUse(world: World, user: LivingEntity, hand: Hand, stack: ItemStack,
                  activeEnchantId: String, testEnchant: ScepterAugment, testLevel: Int): TypedActionResult<ItemStack>{
        testEnchant.clientTask(world,user,hand,testLevel)
        return TypedActionResult.pass(stack)
    }
    fun resetCooldown(stack: ItemStack, world: World, user: LivingEntity, activeEnchant: String): TypedActionResult<ItemStack>
    fun checkManaCost(cost: Int, stack: ItemStack, world: World, user: LivingEntity): Boolean
    fun applyManaCost(cost: Int, stack: ItemStack, world: World, user: LivingEntity)
}