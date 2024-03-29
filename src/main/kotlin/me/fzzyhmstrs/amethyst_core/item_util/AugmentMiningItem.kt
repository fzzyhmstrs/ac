package me.fzzyhmstrs.amethyst_core.item_util

import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterToolMaterial
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable
import me.fzzyhmstrs.fzzy_core.item_util.FlavorHelper
import me.fzzyhmstrs.fzzy_core.mana_util.ManaHelper
import me.fzzyhmstrs.fzzy_core.mana_util.ManaItem
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType
import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import me.fzzyhmstrs.fzzy_core.nbt_util.NbtKeys
import me.fzzyhmstrs.fzzy_core.raycaster_util.RaycasterUtil
import net.minecraft.block.Block
import net.minecraft.client.MinecraftClient
import net.minecraft.client.item.TooltipContext
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.BlockItem
import net.minecraft.item.ItemStack
import net.minecraft.item.MiningToolItem
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.tag.TagKey
import net.minecraft.sound.SoundCategory
import net.minecraft.sound.SoundEvents
import net.minecraft.text.MutableText
import net.minecraft.text.Text
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
abstract class AugmentMiningItem(
    private val material: ScepterToolMaterial,
    damage: Float,
    attackSpeed: Float,
    effectiveBlocks: TagKey<Block>,
    settings: Settings)
    :
    MiningToolItem(damage,attackSpeed,material,effectiveBlocks, settings),
    SpellCasting, ScepterLike, Modifiable, ManaItem
{

    var defaultAugments: List<ScepterAugment> = listOf()
    val defaultModifiers: MutableList<Identifier> = mutableListOf()
    override var noFallback: Boolean = false
    private val tickerManaRepair: Int = material.healCooldown().toInt()

    private val flavorText: MutableText by lazy{
        FlavorHelper.makeFlavorText(this)
    }

    private val flavorTextDesc: MutableText by lazy{
        FlavorHelper.makeFlavorTextDesc(this)
    }

    override fun getTier(): Int{
        return material.scepterTier()
    }

    fun withModifiers(defaultMods: List<AugmentModifier> = listOf()): AugmentMiningItem{
        defaultMods.forEach {
            defaultModifiers.add(it.modifierId)
        }
        return this
    }

    fun withAugments(startingAugments: List<ScepterAugment> = listOf()): AugmentMiningItem{
        defaultAugments = startingAugments
        return this
    }

    fun withAugments(startingAugments: List<ScepterAugment> = listOf(), noFallbackAugment: Boolean): AugmentMiningItem{
        defaultAugments = startingAugments
        noFallback = noFallbackAugment
        return this
    }

    override fun defaultAugments(): List<ScepterAugment>{
        return defaultAugments
    }

    override fun defaultModifiers(type: ModifierHelperType<*>): MutableList<Identifier> {
        return if (canBeModifiedBy(type)) defaultModifiers else mutableListOf()
    }

    override fun modifierObjectPredicate(livingEntity: LivingEntity, stack: ItemStack): Identifier{
        val activeEnchantId: String = getActiveEnchant(stack)
        return Identifier(activeEnchantId)
    }

    /**
     * when called during building, won't add the fallback augment when initializing a scepter. If no default augments are provided, this will result in an empty scepter (requires manually adding spells to function at all)
     */
    fun withNoFallback(): AugmentMiningItem{
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
        val activeEnchantId: String = getActiveEnchant(stack)
        val testEnchant: ScepterAugment = FzzyPort.ENCHANTMENT.get(Identifier(activeEnchantId)) as? ScepterAugment ?: return resetCooldown(stack,world,user,activeEnchantId)
        //if (testEnchant !is ScepterAugment) return resetCooldown(stack,world,user,activeEnchantId)

        //determine the level at which to apply the active augment, from 1 to the maximum level the augment can operate
        val testLevel = ScepterHelper.getTestLevel(nbt, testEnchant)

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

    override fun checkCanUse(
        stack: ItemStack,
        world: World,
        entity: LivingEntity,
        amount: Int,
        message: Text
    ): Boolean {
        val damage = stack.damage
        val maxDamage = stack.maxDamage
        val damageLeft = maxDamage - damage
        return if (damageLeft >= amount && damageLeft > 1) {
            true
        } else {
            if (message.string != "") {
                world.playSound(
                    null,
                    entity.blockPos,
                    SoundEvents.BLOCK_BEACON_DEACTIVATE,
                    SoundCategory.NEUTRAL,
                    1.0F,
                    1.0F
                )
                if (entity is PlayerEntity)
                    entity.sendMessage(message,true)
            }
            false
        }
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

    override fun needsInitialization(stack: ItemStack, scepterNbt: NbtCompound): Boolean {
        return ManaHelper.needsInitialization(stack) || Nbt.getItemStackId(scepterNbt) == -1L || !scepterNbt.contains(NbtKeys.ACTIVE_ENCHANT.str())
    }

    override fun initializeScepter(stack: ItemStack, scepterNbt: NbtCompound) {
        writeDefaultNbt(stack, scepterNbt)
        ManaHelper.initializeManaItem(stack)
        //ModifierHelper.gatherActiveModifiers(stack)
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

    override fun canBeModifiedBy(type: ModifierHelperType<*>): Boolean {
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

    override fun appendTooltip(stack: ItemStack, world: World?, tooltip: MutableList<Text>, context: TooltipContext) {
        super.appendTooltip(stack, world, tooltip, context)
            FlavorHelper.addFlavorText(tooltip, context, flavorText, flavorTextDesc)
    }
}