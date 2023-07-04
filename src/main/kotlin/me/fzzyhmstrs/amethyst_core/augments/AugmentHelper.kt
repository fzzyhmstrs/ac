package me.fzzyhmstrs.amethyst_core.augments

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.data.AugmentDatapoint
import me.fzzyhmstrs.amethyst_core.augments.paired.PairedAugments
import me.fzzyhmstrs.amethyst_core.item.AugmentScepterItem
import me.fzzyhmstrs.amethyst_core.registry.BoostRegistry
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute
import me.fzzyhmstrs.amethyst_core.scepter.SpellType
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import me.fzzyhmstrs.fzzy_core.nbt_util.NbtKeys
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.LivingEntity
import net.minecraft.item.ItemStack
import net.minecraft.loot.function.LootFunction
import net.minecraft.loot.function.SetEnchantmentsLootFunction
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World
import kotlin.math.max
import me.fzzyhmstrs.amethyst_core.nbt.NbtKeys as NbtKeys1

/**
 * helper object for dealing with Scepter Augments. Includes registration methods and data retrieval methods.
 */

object AugmentHelper {

    private val augmentStats: MutableMap<String, AugmentDatapoint> = mutableMapOf()
    private val PAIRED_SPELL_CACHE: MutableMap<String, PairedAugments> = mutableMapOf()
    private val searchArray = intArrayOf(0,1,-1,2,-2,3,-3)

    val PROJECTILE_FIRED = Identifier(AC.MOD_ID,"projectile_fired")
    val PROJECTILE_HIT = Identifier(AC.MOD_ID,"projectile_hit")
    val APPLIED_POSITIVE_EFFECTS = Identifier(AC.MOD_ID,"applied_positive_effects")
    val APPLIED_NEGATIVE_EFFECTS = Identifier(AC.MOD_ID,"applied_negative_effects")
    val SUMMONED_MOB = Identifier(AC.MOD_ID,"summoned_mob")
    val SLASHED = Identifier(AC.MOD_ID,"slashed")
    val DAMAGED_MOB = Identifier(AC.MOD_ID,"damaged_mob")
    val KILLED_MOB = Identifier(AC.MOD_ID,"killed_mob")
    val BLOCK_HIT = Identifier(AC.MOD_ID,"block_hit")
    val BLOCK_BROKE = Identifier(AC.MOD_ID,"block_broke")
    val BLOCK_PLACED = Identifier(AC.MOD_ID,"block_placed")
    val DRY_FIRED = Identifier(AC.MOD_ID,"dry_fired")

    fun getOrCreatePairedAugments(activeEnchantId: String, activeEnchant: ScepterAugment, stack: ItemStack): PairedAugments {
        val pairedEnchantId: String? = getPairedEnchantId(stack,activeEnchantId)
        val pairedEnchant = if (pairedEnchantId != null) {
            val pairedEnchantTest = Registries.ENCHANTMENT.get(Identifier(pairedEnchantId))
            if (pairedEnchantTest is ScepterAugment) {
                pairedEnchantTest
            } else {
                null
            }
        } else {
            null
        }
        val pairedBoostId: String? = getPairedBoostId(stack, activeEnchantId)
        return getOrCreatePairedAugments(activeEnchantId, pairedEnchantId, pairedBoostId, activeEnchant, pairedEnchant)
    }

    fun getOrCreatePairedAugments(activeEnchantId: String, pairedEnchantId: String?, pairedBoostId: String?, spell: ScepterAugment, pairedSpell: ScepterAugment?): PairedAugments {
        val key = if (pairedEnchantId == null){
            if (pairedBoostId == null) {
                activeEnchantId
            } else {
                activeEnchantId + pairedBoostId
            }
        } else {
            if (pairedBoostId == null) {
                activeEnchantId + pairedEnchantId
            } else {
                activeEnchantId + pairedEnchantId + pairedBoostId
            }
        }
        val boost = if(pairedBoostId != null) BoostRegistry.BOOSTS.get(Identifier(pairedBoostId)) else null
        return PAIRED_SPELL_CACHE.getOrPut(key) { PairedAugments(spell, pairedSpell, boost) }
    }
    
    //used to read from sub-NBT passed from an entity, not from a stack
    fun getOrCreatePairedAugmentsFromNbt(nbt: NbtCompound): PairedAugments {
        val enchantId = nbt.getString(NbtKeys.ACTIVE_ENCHANT.str()).takeIf{ it.isNotEmpty() } ?: return PairedAugments()
        val enchant = Registries.ENCHANTMENT.get(Identifier(enchantId)).takeIf { it is ScepterAugment } ?: return PairedAugments()
        val pairedId = nbt.getString(NbtKeys1.PAIRED_ENCHANT)
        val pairedEnchant = if(pairedId.isNotEmpty()){
            Registries.ENCHANTMENT.get(Identifier(pairedId)).takeIf { it is ScepterAugment }
        } else {
            null
        }
        val boostId = nbt.getString(NbtKeys1.PAIRED_BOOST).takeIf{ it.isNotEmpty() }
        return getOrCreatePairedAugments(enchantId, pairedId, boostId, enchant as ScepterAugment, pairedEnchant as ScepterAugment)
    }

    fun getPairedAugments(activeEnchantId: String, stack: ItemStack): PairedAugments?{
        val pairedEnchantId = getPairedEnchantId(stack, activeEnchantId)
        val pairedBoostId = getPairedBoostId(stack, activeEnchantId)
        val key = if (pairedEnchantId == null){
            if (pairedBoostId == null) {
                activeEnchantId
            } else {
                activeEnchantId + pairedBoostId
            }
        } else {
            if (pairedBoostId == null) {
                activeEnchantId + pairedEnchantId
            } else {
                activeEnchantId + pairedEnchantId + pairedBoostId
            }
        }
        return PAIRED_SPELL_CACHE[key]
    }

    fun getPairedAugments(stack: ItemStack): PairedAugments?{
        val nbt: NbtCompound = stack.nbt?:return null
        val activeEnchantId = if (nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())){
            nbt.getString(NbtKeys.ACTIVE_ENCHANT.str())
        } else {
            return null
        }
        return getPairedAugments(activeEnchantId, stack)
    }

    fun createTemporaryPairedAugments(augment: ScepterAugment, pairStack: ItemStack = ItemStack.EMPTY, boostStack: ItemStack = ItemStack.EMPTY): PairedAugments {
        val enchants = EnchantmentHelper.get(pairStack)
        var pairedSpell: ScepterAugment? = null
        for (entry in enchants){
            val enchant = entry.key
            if (enchant is ScepterAugment){
                pairedSpell = enchant
                break
            }
        }
        val boost = BoostRegistry.findMatch(boostStack,augment)
        return PairedAugments(augment,pairedSpell,boost)
    }

    fun writePairedAugments(stack: ItemStack, pairedAugments: PairedAugments){
        val enchants = EnchantmentHelper.get(stack)
        val augment = pairedAugments.primary()?:return
        //return if we are trying to write a paired augment for a primary augment that doesn't exist on the item
        if (!enchants.containsKey(augment)) return
        val pairedEnchantData = NbtCompound()
        var success = false
        val pairedAugment = pairedAugments.paired()
        if (pairedAugment != null){
            val pairedAugmentId = Registries.ENCHANTMENT.getId(pairedAugment)
            if (pairedAugmentId != null){
                pairedEnchantData.putString(NbtKeys1.PAIRED_ENCHANT,pairedAugmentId.toString())
                success = true
            }
        }
        val boost = pairedAugments.boost()
        if (boost != null) {
            val boostId = BoostRegistry.BOOSTS.getId(boost)
            if (boostId != null){
                pairedEnchantData.putString(NbtKeys1.PAIRED_BOOST,boostId.toString())
                success = true
            }
        }
        if (success){
            val stackNbt = stack.orCreateNbt
            val pairedEnchantsNbt = if (stackNbt.contains(NbtKeys1.PAIRED_ENCHANTS)){
                stackNbt.getCompound(NbtKeys1.PAIRED_ENCHANTS)
            } else {
                NbtCompound()
            }
            val augmentId = Registries.ENCHANTMENT.getId(augment)?:return
            pairedEnchantsNbt.put(augmentId.toString(),pairedEnchantData)
            stackNbt.put(NbtKeys1.PAIRED_ENCHANTS,pairedEnchantsNbt)
        }
    }
    
    fun writePairedAugmentsToNbt(spells: PairedAugments): NbtCompound{
        val nbt = NbtCompound()
        val spell = spells.primary()?:return nbt
        val spellId = Registries.ENCHANTMENT.getId(spell)?.toString()?:return nbt
        nbt.putString(NbtKeys.ACTIVE_ENCHANT.str(),spellId)
        val paired = spells.paired()
        if (paired != null){
            val pairedId = Registries.ENCHANTMENT.getId(paired)?.toString()
            if (pairedId != null){
                nbt.putString(NbtKeys1.PAIRED_ENCHANT,pairedId)
            }
        }
        val boost = spells.boost()
        if (boost != null){
            val boostId = BoostRegistry.BOOSTS.getId(boost)?.toString()
            if(boostId != null){
                nbt.putString(NbtKeys1.PAIRED_BOOST,boostId)
            }
        }
        return nbt
    }

    fun readPairedAugmentsFromStack(stack: ItemStack): Map<ScepterAugment, PairedAugments>{
        val map: MutableMap<ScepterAugment, PairedAugments> = mutableMapOf()
        val enchants = EnchantmentHelper.get(stack)
        for (entry in enchants){
            val enchant = entry.key
            if (enchant !is ScepterAugment) continue
            val enchantId = Registries.ENCHANTMENT.getId(enchant)?:continue
            map[enchant] = getOrCreatePairedAugments(enchantId.toString(),enchant,stack)
        }
        return map
    }

    fun getPairedEnchantId(stack: ItemStack, activeEnchantId: String): String?{
        val nbt: NbtCompound = stack.orCreateNbt
        return getPairedEnchantId(nbt, activeEnchantId)
    }

    private fun getPairedEnchantId(nbt: NbtCompound, activeEnchantId: String): String? {
        return if (nbt.contains(NbtKeys1.PAIRED_ENCHANTS)) {
            val pairedEnchants = nbt.getCompound(NbtKeys1.PAIRED_ENCHANTS)
            if (pairedEnchants.contains(activeEnchantId)) {
                val pairedEnchantData = pairedEnchants.getCompound(activeEnchantId)
                pairedEnchantData.getString(NbtKeys1.PAIRED_ENCHANT)
            } else {
                null
            }
        } else {
            null
        }
    }

    fun getPairedBoostId(stack: ItemStack, activeEnchantId: String): String?{
        val nbt: NbtCompound = stack.orCreateNbt
        return getPairedBoostId(nbt, activeEnchantId)
    }

    private fun getPairedBoostId(nbt: NbtCompound, activeEnchantId: String): String? {
        return if (nbt.contains(NbtKeys1.PAIRED_ENCHANTS)) {
            val pairedEnchants = nbt.getCompound(NbtKeys1.PAIRED_ENCHANTS)
            if (pairedEnchants.contains(activeEnchantId)) {
                val pairedEnchantData = pairedEnchants.getCompound(activeEnchantId)
                pairedEnchantData.getString(NbtKeys1.PAIRED_BOOST)
            } else {
                null
            }
        } else {
            null
        }
    }

    fun getPairedSpell(nbt: NbtCompound, activeEnchantId: String): ScepterAugment?{
        val str = getPairedEnchantId(nbt, activeEnchantId) ?: return null
        val spell = Registries.ENCHANTMENT.get(Identifier(str))
        if (spell !is ScepterAugment) return null
        return spell
    }

    /**
     * used to check if a registry or other initialization method should consider the provided augment.
     */
    fun checkIfAugmentEnabled(augment: ScepterAugment, id: Identifier): Boolean{
        return augment.augmentData.enabled
    }

    /**
     * This method should be used to register Scpeter Augments, not a plain registration method, to capture initialization tasks
     */
    fun registerAugment(augment: ScepterAugment, id: Identifier){
        Registry.register(Registries.ENCHANTMENT,id,augment)
    }
    
    fun getScepterAugment(id: String): ScepterAugment?{
        return getScepterAugment(Identifier(id))
    }
    
    fun getScepterAugment(id: Identifier): ScepterAugment?{
        val enchant = Registries.ENCHANTMENT.get(id)
        return if(enchant is ScepterAugment){
            enchant
        } else {
            null
        }
    }

    fun getAugmentType(id: String): SpellType {
        return getScepterAugment(id)?.augmentData?.type?: SpellType.NULL
    }
    fun getAugmentType(id: Identifier): SpellType {
        return getScepterAugment(id)?.augmentData?.type?: SpellType.NULL
    }

    private val DEFAULT_COOLDOWN = PerLvlI(20)
    fun getAugmentCooldown(id: String): PerLvlI{
        return getScepterAugment(id)?.augmentData?.cooldown?: DEFAULT_COOLDOWN
    }

    fun getAugmentManaCost(id: String, reduction: Double = 1.0): Int{
        val cost = (getScepterAugment(id)?.augmentData?.manaCost?.times(reduction))?.toInt() ?: (10 * reduction).toInt()
        return max(0,cost)
    }

    fun getAugmentCurrentLevel(scepterLevel: Int, augment: ScepterAugment): Int{
        val minLvl = augment.augmentData.minLvl
        val maxLevel = (augment.getAugmentMaxLevel()) + minLvl - 1
        var testLevel = 1
        if (scepterLevel >= minLvl){
            testLevel = scepterLevel
            if (testLevel > maxLevel) testLevel = maxLevel
            testLevel -= (minLvl - 1)
        }
        return testLevel
    }

    fun getAugmentImbueLevel(id: Identifier, multiplier: Float = 1f): Int{
        return getScepterAugment(id)?.augmentData?.imbueLevel?.times(multiplier)?.toInt() ?: 1
    }

    fun getAugmentImbueLevel(id: String, multiplier: Float = 1f): Int{
        return getScepterAugment(id)?.augmentData?.imbueLevel?.times(multiplier)?.toInt() ?: 1
    }
    
    fun getAugmentEnabled(id: String): Boolean {
        return getScepterAugment(id)?.augmentData?.enabled ?: false
    }

    fun getAugmentDatapoint(id: String): AugmentDatapoint? {
        return getScepterAugment(id)?.augmentData
    }

    fun getEffectiveManaCost(pairedAugments: PairedAugments, manaCostModifier: Double, level: Int, user: LivingEntity): Int{
        val manaCost = pairedAugments.provideManaCost(level)
        return (manaCost * ((manaCostModifier + 100.0)/100.0) * user.getAttributeValue(RegisterAttribute.SPELL_MANA_COST)).toInt()
    }

    fun getEffectiveCooldown(pairedAugments: PairedAugments, cooldownModifier: Double, level: Int, user: LivingEntity): Int{
        val cooldown = pairedAugments.provideCooldown(level)
        return (cooldown * ((cooldownModifier + 100.0)/100.0) * user.getAttributeValue(RegisterAttribute.SPELL_COOLDOWN)).toInt()
    }

    fun getEffectiveCooldown(activeEnchantId: String, activeEnchant: ScepterAugment, stack: ItemStack, cooldownModifier: Double, level: Int, user: LivingEntity): Int{
        val pairedAugments = getOrCreatePairedAugments(activeEnchantId,activeEnchant,stack)
        return getEffectiveCooldown(pairedAugments, cooldownModifier, level, user)
    }

    /**
     * A [LootFunction.Builder] that can be used in a loot pool builder to apply default augments to a scepter, the provided list of augments, or both.
     */
    fun augmentsLootFunctionBuilder(item: AugmentScepterItem, augments: List<ScepterAugment> = listOf()): LootFunction.Builder{
        var builder = SetEnchantmentsLootFunction.Builder()
        if (item.defaultAugments.isEmpty() && augments.isEmpty()){
            return builder
        } else {
            item.defaultAugments.forEach {
                builder = builder.enchantment(it, ConstantLootNumberProvider.create(1.0F))
            }
            augments.forEach {
                builder = builder.enchantment(it, ConstantLootNumberProvider.create(1.0F))
            }
        }
        return builder
    }

    fun findSpawnPos(world: World, startPos: BlockPos, entity: LivingEntity, radius: Int = 3, tries: Int = 8, pitch: Float = 0f, yaw: Float = 0f): Boolean{
        entity.refreshPositionAndAngles(startPos,yaw, pitch)
        val boundingBoxReset = entity.boundingBox
        var boundingBox = entity.boundingBox
        for (i in 1..tries){
            val xPos = world.random.nextBetween(-radius,radius)
            val yPos = 1
            val zPos = world.random.nextBetween(-radius,radius)
            for (j in searchArray){
                boundingBox = boundingBox.offset(xPos.toDouble(),(yPos + j).toDouble(),zPos.toDouble())
                if (world.isSpaceEmpty(boundingBox)){
                    entity.refreshPositionAndAngles(BlockPos(startPos.x + xPos,startPos.y + yPos,startPos.z + zPos), yaw, pitch)
                    return true
                }
                boundingBox = boundingBoxReset
            }
        }
        return false
    }

}