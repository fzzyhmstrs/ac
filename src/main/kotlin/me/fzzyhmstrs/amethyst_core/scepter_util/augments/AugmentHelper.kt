package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.item_util.AugmentScepterItem
import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.ModificationInfo
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.loot.function.LootFunction
import net.minecraft.loot.function.SetEnchantmentsLootFunction
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import kotlin.math.max

/**
 * helper object for dealing with Scepter Augments. Includes registration methods and data retrieval methods.
 */

object AugmentHelper {

    private val augmentStats: MutableMap<String, AugmentDatapoint> = mutableMapOf()

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

    /**
     * used to check if a registry or other initialization method should consider the provided augment.
     */
    fun checkIfAugmentEnabled(augment: ScepterAugment, id: Identifier): Boolean{
        val augmentConfig = ScepterAugment.Companion.AugmentStats()
        augmentConfig.id = id.toString()
        val augmentAfterConfig = ScepterAugment.configAugment(augment.javaClass.simpleName + ScepterAugment.augmentVersion +".json", augmentConfig)
        return augmentAfterConfig.enabled
    }

    fun registerAugment(augment: ScepterAugment, id: Identifier, imbueLevel: Int = 1){
        Registry.register(Registries.ENCHANTMENT,id,augment)
        augment.augmentData = configAugmentStat(augment,id.toString(),imbueLevel)
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
    
    fun getAugmentCurrentLevel(scepterLevel: Int, augmentId: Identifier, augment: ScepterAugment): Int{
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

    fun getAugmentManaCost(id: String, reduction: Double = 1.0): Int{
        if(!augmentStats.containsKey(id)) return (10 * reduction).toInt()
        val cost = (augmentStats[id]?.manaCost?.times(reduction))?.toInt() ?: (10 * reduction).toInt()
        return max(0,cost)
    }

    private val DEFAULT_COOLDOWN = PerLvlI(20)
    fun getAugmentCooldown(id: String): PerLvlI{
        return getScepterAugment(id)?.augmentData?.cooldown?: DEFAULT_COOLDOWN
    }
    private val DEFAULT_MODIFICATION_INFO = ModificationInfo.empty()
    fun getAugmentModificationInfo(id: String): ModificationInfo {
        return getScepterAugment(id)?.augmentData?.modificationInfo?: DEFAULT_MODIFICATION_INFO
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

    fun getAugmentDatapoint(id: String): AugmentDatapoint{
        return getScepterAugment(id)?.augmentData?:AugmentDatapoint()
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
