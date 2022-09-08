package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierHelper
import me.fzzyhmstrs.amethyst_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys
import me.fzzyhmstrs.amethyst_core.raycaster_util.RaycasterUtil
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterToolMaterial
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.AugmentHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.registry.Registry
import net.minecraft.world.World
import kotlin.math.max

/**
 * Extended [ModifiableScepterItem] that integrates with the Scepter Augment System. This is the barebones scepter for use with [ScepterAugment]s.
 *
 * Adds builder methods for adding default augments that are applied on craft/initilization.
 *
 * Modifiers are specified to be [AugmentModifier] type in this class, which are codependent with Scepter Augments
 *
 * Adds the main [use] functionality for this style of scepter, including Augment selection, level checking, cooldown checking, and a separate [serverUse] and [clientUse] method for actions to take in those corresponding environments. By default calls the specified Augments [ScepterAugment.applyModifiableTasks] after determining and checking the compiled modifiers relevant to that augment.
 */
@Suppress("SameParameterValue", "unused", "USELESS_IS_CHECK")
abstract class AugmentScepterItem(material: ScepterToolMaterial, settings: Settings):
    ModifiableScepterItem<AugmentModifier>(material, settings){

    var defaultAugments: List<ScepterAugment> = listOf()
    var noFallback: Boolean = false

    fun withAugments(startingAugments: List<ScepterAugment> = listOf()): AugmentScepterItem{
        defaultAugments = startingAugments
        return this
    }

    fun withAugments(startingAugments: List<ScepterAugment> = listOf(), noFallbackAugment: Boolean): AugmentScepterItem{
        defaultAugments = startingAugments
        noFallback = noFallbackAugment
        return this
    }

    /**
     * when called during building, won't add the fallback augment when initializing a scepter. If no default augments are provided, this will result in an empty scepter (requires manually adding spells to function at all)
     */
    fun withNoFallback(): AugmentScepterItem{
        noFallback = true
        return this
    }

    override fun getActiveModifiers(stack: ItemStack): AbstractModifier<AugmentModifier>.CompiledModifiers {
        return ModifierHelper.getActiveModifiers(stack)
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        super.use(world, user, hand)
        val stack = user.getStackInHand(hand)
        val nbt = stack.orCreateNbt
        val activeEnchantId: String = getActiveEnchant(stack)
        val testEnchant: Enchantment = Registry.ENCHANTMENT.get(Identifier(activeEnchantId))?: return resetCooldown(stack,world,user,activeEnchantId)
        if (testEnchant !is ScepterAugment) return resetCooldown(stack,world,user,activeEnchantId)

        //determine the level at which to apply the active augment, from 1 to the maximum level the augment can operate
        val level = ScepterHelper.getScepterStat(nbt,activeEnchantId).first
        val minLvl = AugmentHelper.getAugmentMinLvl(activeEnchantId)
        val maxLevel = (testEnchant.getAugmentMaxLevel()) + minLvl - 1
        var testLevel = 1
        if (level >= minLvl){
            testLevel = level
            if (testLevel > maxLevel) testLevel = maxLevel
            testLevel -= (minLvl - 1)
        }

        val stack2 = if (hand == Hand.MAIN_HAND) {
            user.offHandStack
        } else {
            user.mainHandStack
        }
        if(world.isClient()) {
            if (!stack2.isEmpty) {
                if (stack2.item is BlockItem) {
                    val cht = MinecraftClient.getInstance().crosshairTarget
                    if (cht != null) {
                        if (cht.type == HitResult.Type.BLOCK) {
                            return TypedActionResult.pass(stack)
                        }
                    }
                }
            }
            return clientUse(world, user, hand, stack, activeEnchantId, testEnchant, testLevel)
        } else {
            if (!stack2.isEmpty) {
                if (stack2.item is BlockItem) {
                    val reachDistance = if (user.abilities.creativeMode){
                        5.0
                    } else {
                        4.5
                    }
                    val cht = RaycasterUtil.raycastBlock(distance = reachDistance,entity = user)
                    if (cht != null) {
                        return TypedActionResult.pass(stack)
                    }
                }
            }
            return serverUse(world, user, hand, stack, activeEnchantId, testEnchant, testLevel)
        }
    }

    private fun serverUse(world: World, user: PlayerEntity, hand: Hand, stack: ItemStack,
                          activeEnchantId: String, testEnchant: ScepterAugment, testLevel: Int): TypedActionResult<ItemStack>{

        val modifiers = ModifierHelper.getActiveModifiers(stack)

        val cd : Int? = ScepterHelper.useScepter(activeEnchantId, testEnchant, stack, world, modifiers.compiledData.cooldownModifier)
        return if (cd != null) {
            val manaCost = AugmentHelper.getAugmentManaCost(activeEnchantId,modifiers.compiledData.manaCostModifier)
            if (!checkManaCost(manaCost,stack, world, user)) return resetCooldown(stack,world,user,activeEnchantId)
            val level = max(1,testLevel + modifiers.compiledData.levelModifier)
            if (testEnchant.applyModifiableTasks(world, user, hand, level, modifiers.modifiers, modifiers.compiledData)) {
                applyManaCost(manaCost,stack, world, user)
                ScepterHelper.incrementScepterStats(stack.orCreateNbt, stack, activeEnchantId, modifiers.compiledData.getXpModifiers())
                user.itemCooldownManager.set(stack.item, cd)
                TypedActionResult.success(stack)
            } else {
                resetCooldown(stack,world,user,activeEnchantId)
            }
        } else {
            resetCooldown(stack,world,user,activeEnchantId)
        }
    }
    @Suppress("UNUSED_PARAMETER")
    private fun clientUse(world: World, user: PlayerEntity, hand: Hand, stack: ItemStack,
                          activeEnchantId: String, testEnchant: ScepterAugment, testLevel: Int): TypedActionResult<ItemStack>{
        testEnchant.clientTask(world,user,hand,testLevel)
        return TypedActionResult.pass(stack)
    }

    private fun checkManaCost(cost: Int, stack: ItemStack, world: World, user: PlayerEntity): Boolean{
        return (checkCanUse(stack,world,user, cost))
    }

    private fun applyManaCost(cost: Int, stack: ItemStack, world: World, user: PlayerEntity){
        manaDamage(stack, world, user, cost)
    }

    override fun onCraft(stack: ItemStack, world: World, player: PlayerEntity) {
        super.onCraft(stack, world, player)
        addDefaultEnchantments(stack, stack.orCreateNbt)
    }

    override fun writeDefaultNbt(stack: ItemStack, scepterNbt: NbtCompound) {
        super.writeDefaultNbt(stack, scepterNbt)
        addDefaultEnchantments(stack, scepterNbt)
        activeNbtCheck(scepterNbt)
        ScepterHelper.getScepterStats(stack)
    }

    private fun activeNbtCheck(scepterNbt: NbtCompound){
        if(!scepterNbt.contains(NbtKeys.ACTIVE_ENCHANT.str())){
            val identifier = fallbackId
            Nbt.writeStringNbt(NbtKeys.ACTIVE_ENCHANT.str(), identifier.toString(), scepterNbt)
        }
    }

    override fun needsInitialization(stack: ItemStack, scepterNbt: NbtCompound): Boolean {
        return super.needsInitialization(stack, scepterNbt) || !scepterNbt.contains(NbtKeys.ACTIVE_ENCHANT.str())
    }

    open fun addDefaultEnchantments(stack: ItemStack, scepterNbt: NbtCompound){
        if (scepterNbt.contains(NbtKeys.ENCHANT_INIT.str() + stack.translationKey)) return
        val enchantToAdd = Registry.ENCHANTMENT.get(this.fallbackId)
        if (enchantToAdd != null && !noFallback){
            if (EnchantmentHelper.getLevel(enchantToAdd,stack) == 0){
                stack.addEnchantment(enchantToAdd,1)
            }
        }
        defaultAugments.forEach {
            if (EnchantmentHelper.getLevel(it,stack) == 0){
                stack.addEnchantment(it,1)
            }
        }
        Nbt.writeBoolNbt(NbtKeys.ENCHANT_INIT.str() + stack.translationKey,true,scepterNbt)
    }

    open fun resetCooldown(stack: ItemStack, world: World, user: PlayerEntity, activeEnchant: String): TypedActionResult<ItemStack>{
        world.playSound(null,user.blockPos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS,0.6F,0.8F)
        ScepterHelper.resetCooldown(world, stack, activeEnchant)
        return TypedActionResult.fail(stack)
    }

    fun getActiveEnchant(stack: ItemStack): String{
        val nbt: NbtCompound = stack.orCreateNbt
        return if (nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())){
            Nbt.readStringNbt(NbtKeys.ACTIVE_ENCHANT.str(), nbt)
        } else {
            initializeScepter(stack,nbt)
            Nbt.readStringNbt(NbtKeys.ACTIVE_ENCHANT.str(), nbt)
        }
    }
}
