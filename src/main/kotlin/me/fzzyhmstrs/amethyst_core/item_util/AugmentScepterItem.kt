package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierHelper
import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterToolMaterial
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable
import me.fzzyhmstrs.fzzy_core.item_util.CustomFlavorToolItem
import me.fzzyhmstrs.fzzy_core.mana_util.ManaHelper
import me.fzzyhmstrs.fzzy_core.mana_util.ManaItem
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType
import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import me.fzzyhmstrs.fzzy_core.nbt_util.NbtKeys
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.client.MinecraftClient
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.UseAction
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.MathHelper
import net.minecraft.world.World

/**
 * Adds builder methods for adding default augments that are applied on craft/initialization.
 *
 * Modifiers are specified to be [AugmentModifier] type in this class, which are codependent with Scepter Augments
 *
 * Adds the main [use] functionality for this style of scepter, including Augment selection, level checking, cooldown checking, and a separate [serverUse] and [clientUse] method for actions to take in those corresponding environments. By default, this calls the specified Augments [ScepterAugment.applyModifiableTasks] after determining and checking the compiled modifiers relevant to that augment.
 */
@Suppress("SameParameterValue", "unused")
abstract class AugmentScepterItem(
    private val material: ScepterToolMaterial,
    settings: Settings)
    :
    CustomFlavorToolItem(material, settings),
    SpellCasting, ScepterLike, Modifiable, ManaItem
{

    var defaultAugments: List<ScepterAugment> = listOf()
    val defaultModifiers: MutableList<Identifier> = mutableListOf()
    var noFallback: Boolean = false
    private val tickerManaRepair: Int = material.healCooldown().toInt()
    
    override fun getTier(): Int{
        return material.scepterTier()
    }

    fun withModifiers(defaultMods: List<AugmentModifier> = listOf()): AugmentScepterItem{
        defaultMods.forEach {
            defaultModifiers.add(it.modifierId)
        }
        return this
    }
    
    fun withAugments(startingAugments: List<ScepterAugment> = listOf()): AugmentScepterItem{
        defaultAugments = startingAugments
        return this
    }

    fun withAugments(startingAugments: List<ScepterAugment> = listOf(), noFallbackAugment: Boolean): AugmentScepterItem{
        defaultAugments = startingAugments
        noFallback = noFallbackAugment
        return this
    }
    
    override fun defaultModifiers(type: ModifierHelperType): MutableList<Identifier> {
        return if (canBeModifiedBy(type)) defaultModifiers else mutableListOf()
    }

    /**
     * when called during building, won't add the fallback augment when initializing a scepter. If no default augments are provided, this will result in an empty scepter (requires manually adding spells to function at all)
     */
    fun withNoFallback(): AugmentScepterItem{
        noFallback = true
        return this
    }

    override fun use(world: World, user: PlayerEntity, hand: Hand): TypedActionResult<ItemStack> {
        super.use(world, user, hand)
        val stack = user.getStackInHand(hand)
        val nbt = stack.orCreateNbt
        if (needsInitialization(stack, nbt) && !world.isClient){
            initializeScepter(stack, nbt)
        }
        val activeEnchantId: String = ScepterHelper.getActiveEnchant(stack)
        val testEnchant: Enchantment = Registries.ENCHANTMENT.get(Identifier(activeEnchantId))?: return resetCooldown(stack,world,user,activeEnchantId)
        if (testEnchant !is ScepterAugment) return resetCooldown(stack,world,user,activeEnchantId)

        //determine the level at which to apply the active augment, from 1 to the maximum level the augment can operate
        val testLevel = ScepterHelper.getTestLevel(nbt,activeEnchantId, testEnchant)

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

    override fun <T> serverUse(world: World, user: T, hand: Hand, stack: ItemStack,
        activeEnchantId: String,spell: ScepterAugment,testLevel: Int)
    : 
    TypedActionResult<ItemStack> where T: LivingEntity, T: SpellCastingEntity {
        return ScepterHelper.castSpell(world,user,hand,stack,spell,activeEnchantId,testLevel,this)
    }
    
    @Suppress("UNUSED_PARAMETER")
    override fun clientUse(world: World, user: LivingEntity, hand: Hand, stack: ItemStack,
        activeEnchantId: String, testEnchant: ScepterAugment, testLevel: Int)
    : 
    TypedActionResult<ItemStack>{
        testEnchant.clientTask(world,user,hand,testLevel)
        return TypedActionResult.pass(stack)
    }

    override fun checkManaCost(cost: Int, stack: ItemStack, world: World, user: LivingEntity): Boolean{
        return checkCanUse(stack,world,user, cost)
    }

    override fun applyManaCost(cost: Int, stack: ItemStack, world: World, user: LivingEntity){
        manaDamage(stack, world, user, cost)
    }
    
    override fun resetCooldown(stack: ItemStack, world: World, user: LivingEntity, activeEnchant: String): TypedActionResult<ItemStack>{
        world.playSound(null,user.blockPos, SoundEvents.BLOCK_FIRE_EXTINGUISH, SoundCategory.PLAYERS,0.6F,0.8F)
        ScepterHelper.resetCooldown(world, stack, user, activeEnchant)
        return TypedActionResult.fail(stack)
    }
    
    override fun onCraft(stack: ItemStack, world: World, player: PlayerEntity) {
        if (!world.isClient) {
            val nbt = stack.orCreateNbt
            initializeScepter(stack, nbt)
        }
        addDefaultEnchantments(stack, stack.orCreateNbt)
    }
    
    open fun addDefaultEnchantments(stack: ItemStack, scepterNbt: NbtCompound){
        if (scepterNbt.contains(me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys.ENCHANT_INIT.str() + stack.translationKey)) return
        val enchantToAdd = Registries.ENCHANTMENT.get(this.fallbackId)
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
        scepterNbt.putBoolean(me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys.ENCHANT_INIT.str() + stack.translationKey,true)
    }

    
    override fun needsInitialization(stack: ItemStack, scepterNbt: NbtCompound): Boolean {
        return ManaHelper.needsInitialization(stack) || Nbt.getItemStackId(scepterNbt) == -1L || !scepterNbt.contains(NbtKeys.ACTIVE_ENCHANT.str())
    }
    
    override fun initializeScepter(stack: ItemStack, scepterNbt: NbtCompound) {
        writeDefaultNbt(stack, scepterNbt)
        ManaHelper.initializeManaItem(stack)
        ModifierHelper.gatherActiveModifiers(stack)
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
            scepterNbt.putString(NbtKeys.ACTIVE_ENCHANT.str(), identifier.toString())
        }
    }
    
    override fun canBeModifiedBy(type: ModifierHelperType): Boolean {
        return (type == ModifierRegistry.MODIFIER_TYPE)
    }
    
    override fun getRepairTime(): Int{
        return tickerManaRepair
    }

    override fun isFireproof(): Boolean {
        return true
    }

    override fun getItemBarColor(stack: ItemStack): Int {
        return MathHelper.hsvToRgb(0.66f,1.0f,1.0f)
    }

    override fun getUseAction(stack: ItemStack): UseAction {
        return UseAction.BLOCK
    }
    
    override fun inventoryTick(stack: ItemStack, world: World, entity: Entity, slot: Int, selected: Boolean) {
        if (world.isClient) return
        val nbt = stack.orCreateNbt
        if (needsInitialization(stack, nbt) && !world.isClient){
            initializeScepter(stack, nbt)
        }
        //slowly heal damage over time
        if (ManaHelper.tickHeal(stack)){
            healDamage(1,stack)
        }
    }
}
