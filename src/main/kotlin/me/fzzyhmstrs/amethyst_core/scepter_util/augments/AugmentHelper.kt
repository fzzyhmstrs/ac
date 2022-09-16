package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.item_util.AugmentScepterItem
import me.fzzyhmstrs.amethyst_core.scepter_util.LoreTier
import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.loot.function.LootFunction
import net.minecraft.loot.function.SetEnchantmentsLootFunction
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.util.Identifier
import kotlin.math.max

/**
 * helper object for dealing with Scepter Augments. Includes registration methods and data retrieval methods.
 */

object AugmentHelper {

    private val augmentStats: MutableMap<String, AugmentDatapoint> = mutableMapOf()

    fun registerAugmentStat(id: String, dataPoint: AugmentDatapoint, overwrite: Boolean = false){
        if(!augmentStats.containsKey(id) || overwrite){
            augmentStats[id] = dataPoint
            dataPoint.bookOfLoreTier.addToList(id)
        }
    }

    /**
     * typically an augment will be registered with this. Call this registration AFTER registering the augment with the Enchantment Registry.
     */
    fun registerAugmentStat(augment: ScepterAugment){
        val id = EnchantmentHelper.getEnchantmentId(augment)?.toString()?:throw NoSuchElementException("Enchantment ID for ${this.javaClass.canonicalName} not found!")
        val imbueLevel = if (checkAugmentStat(id)){
            getAugmentImbueLevel(id)
        } else {
            1
        }
        registerAugmentStat(id,configAugmentStat(augment,id,imbueLevel),true)
    }

    /**
     * used to check if a registry or other initialization method should consider the provided augment.
     */
    fun checkIfAugmentEnabled(augment: ScepterAugment): Boolean{
        val id = EnchantmentHelper.getEnchantmentId(augment)?.toString()?:throw NoSuchElementException("Enchantment ID for ${this.javaClass.canonicalName} not found!")
        return configAugmentStat(augment, id).enabled
    }

    /**
     * takes a provided ScepterAugment, scrapes it's current stats into an AugmentStat class and then runs that default set of stats through configAugment, which reads or creates a json config file to store and/or alter the base info.
     */
    private fun configAugmentStat(augment: ScepterAugment, id: String, imbueLevel: Int = 1): AugmentDatapoint {
        val stat = augment.augmentStat(imbueLevel)
        val augmentConfig = ScepterAugment.Companion.AugmentStats()
        val type = stat.type
        augmentConfig.id = id
        augmentConfig.enabled = stat.enabled
        augmentConfig.cooldown = stat.cooldown
        augmentConfig.manaCost = stat.manaCost
        augmentConfig.minLvl = stat.minLvl
        val tier = stat.bookOfLoreTier
        val item = stat.keyItem
        val augmentAfterConfig = ScepterAugment.configAugment(augment.javaClass.simpleName + ScepterAugment.augmentVersion +".json", augmentConfig)
        return AugmentDatapoint(type,augmentAfterConfig.cooldown,augmentAfterConfig.manaCost,augmentAfterConfig.minLvl,imbueLevel,tier,item)
    }

    fun checkAugmentStat(id: String): Boolean{
        return augmentStats.containsKey(id)
    }

    fun getAugmentType(id: String): SpellType {
        if(!augmentStats.containsKey(id)) return SpellType.NULL
        return augmentStats[id]?.type?: SpellType.NULL
    }

    fun getAugmentItem(id: String): Item {
        if(!augmentStats.containsKey(id)) return Items.GOLD_INGOT
        return augmentStats[id]?.keyItem?: Items.GOLD_INGOT
    }

    fun getAugmentMinLvl(id: String): Int {
        if(!augmentStats.containsKey(id)) return 1
        return augmentStats[id]?.minLvl?:1
    }
    
    fun getAugmentCurrentLevel(scepterLevel: Int, augmentId: Identifier, augment: ScepterAugment): Int{
        val minLvl = getAugmentMinLvl(augmentId.toString())
        val maxLevel = (augment.getAugmentMaxLevel()) + minLvl - 1
        var testLevel = 1
        if (scepterLevel >= minLvl){
            testLevel = scepterLevel
            if (testLevel > maxLevel) testLevel = maxLevel
            testLevel -= (minLvl - 1)
        }
        return testLevel
    }

    fun getAugmentManaCost(id: String, reduction: Double = 0.0): Int{
        if(!augmentStats.containsKey(id)) return (10 * (100.0 + reduction) / 100.0).toInt()
        val cost = (augmentStats[id]?.manaCost?.times(100.0 + reduction)?.div(100.0))?.toInt() ?: (10 * (100.0 + reduction) / 100.0).toInt()
        return max(0,cost)
    }

    fun getAugmentCooldown(id: String): Int{
        if(!augmentStats.containsKey(id)) return (20)
        val cd = (augmentStats[id]?.cooldown) ?: 20
        return max(1,cd)
    }

    fun getAugmentImbueLevel(id: String): Int{
        if(!augmentStats.containsKey(id)) return (1)
        val cd = (augmentStats[id]?.imbueLevel) ?: 1
        return max(1,cd)
    }

    fun getAugmentTier(id: String): LoreTier {
        if (!augmentStats.containsKey(id)) return (LoreTier.NO_TIER)
        return (augmentStats[id]?.bookOfLoreTier) ?: LoreTier.NO_TIER
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

}
