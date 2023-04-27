package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.item_util.AugmentScepterItem
import me.fzzyhmstrs.amethyst_core.scepter_util.LoreTier
import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
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

    val PROJECTILE_FIRED = Identifier(AC.MOD_ID,"projectile_fired")
    val APPLIED_POSITIVE_EFFECTS = Identifier(AC.MOD_ID,"applied_positive_effects")
    val APPLIED_NEGATIVE_EFFECTS = Identifier(AC.MOD_ID,"applied_negative_effects")
    val SUMMONED_MOB = Identifier(AC.MOD_ID,"summoned_mob")
    val SLASHED = Identifier(AC.MOD_ID,"slashed")
    val BROKE_BLOCK = Identifier(AC.MOD_ID,"broke_block")
    val PLACED_BLOCK = Identifier(AC.MOD_ID,"broke_block")

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
    fun checkIfAugmentEnabled(augment: ScepterAugment, id: Identifier): Boolean{
        val augmentConfig = ScepterAugment.Companion.AugmentStats()
        augmentConfig.id = id.toString()
        val augmentAfterConfig = ScepterAugment.configAugment(augment.javaClass.simpleName + ScepterAugment.augmentVersion +".json", augmentConfig)
        return augmentAfterConfig.enabled
    }

    /**
     * takes a provided ScepterAugment, scrapes its current stats into an AugmentStat class and then runs that default set of stats through configAugment, which reads or creates a json config file to store and/or alter the base info.
     */
    private fun configAugmentStat(augment: ScepterAugment, id: String, imbueLevel: Int = 1): AugmentDatapoint {
        val stat = augment.augmentData
        val augmentConfig = ScepterAugment.Companion.AugmentStats()
        val type = stat.type
        augmentConfig.id = id
        augmentConfig.enabled = stat.enabled
        augmentConfig.pvpMode = stat.pvpMode
        augmentConfig.setCooldown(stat.cooldown)
        augmentConfig.manaCost = stat.manaCost
        augmentConfig.minLvl = stat.minLvl
        augmentConfig.castXp = stat.castXp
        val tier = stat.bookOfLoreTier
        val item = stat.keyItem
        val augmentAfterConfig = ScepterAugment.configAugment(augment.javaClass.simpleName + ScepterAugment.augmentVersion +".json", augmentConfig)
        return AugmentDatapoint(type,augmentAfterConfig.getCooldown(),augmentAfterConfig.manaCost,augmentAfterConfig.minLvl,imbueLevel,augmentAfterConfig.castXp, tier, item, augmentAfterConfig.enabled, augmentAfterConfig.pvpMode)
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

    fun getAugmentManaCost(id: String, reduction: Double = 1.0): Int{
        if(!augmentStats.containsKey(id)) return (10 * reduction).toInt()
        val cost = (augmentStats[id]?.manaCost?.times(reduction))?.toInt() ?: (10 * reduction).toInt()
        return max(0,cost)
    }

    private val DEFAULT_COOLDOWN = PerLvlI(20)
    fun getAugmentCooldown(id: String): PerLvlI{
        if(!augmentStats.containsKey(id)) return DEFAULT_COOLDOWN
        return (augmentStats[id]?.cooldown) ?: DEFAULT_COOLDOWN
    }
    private val DEFAULT_MODIFICATION_INFO = ModificationInfo.empty()
    fun getAugmentModificationInfo(id: String): ModificationInfo{
        if(!augmentStats.containsKey(id)) return DEFAULT_MODIFICATION_INFO
        return (augmentStats[id]?.modificationInfo) ?: DEFAULT_MODIFICATION_INFO
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
    fun getAugmentEnabled(id: String): Boolean {
        if (!augmentStats.containsKey(id)) return false
        return (augmentStats[id]?.enabled) ?: false
    }
    fun getAugmentPvpMode(id: String): Boolean {
        if (!augmentStats.containsKey(id)) return false
        return (augmentStats[id]?.pvpMode) ?: false
    }
    fun getAugmentCastXp(id: String): Int{
        if (!augmentStats.containsKey(id)) return 1
        return (augmentStats[id]?.castXp) ?: 1
    }

    fun getAugmentDatapoint(id: String): AugmentDatapoint{
        if (!augmentStats.containsKey(id)) return AugmentDatapoint()
        return augmentStats[id]?:AugmentDatapoint()
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
