package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.item_util.AugmentScepterItem
import me.fzzyhmstrs.amethyst_core.scepter_util.LoreTier
import me.fzzyhmstrs.amethyst_core.scepter_util.SpellType
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.item.Item
import net.minecraft.item.Items
import net.minecraft.loot.function.LootFunction
import net.minecraft.loot.function.SetEnchantmentsLootFunction
import net.minecraft.loot.provider.number.ConstantLootNumberProvider
import net.minecraft.registry.Registries
import net.minecraft.util.Identifier
import kotlin.math.max

/**
 * helper object for dealing with Scepter Augments. Includes registration methods and data retrieval methods.
 */

@Suppress("DeprecatedCallableAddReplaceWith")
object AugmentHelper {

    @Deprecated("No longer serves it's original purpose, but does still make sure the spell is registered.")
    fun checkAugmentStat(id: String): Boolean{
        return Registries.ENCHANTMENT.containsId(Identifier(id))
    }

    @Deprecated("This system is internally removed. The imbue level from the AugmentDatapoint will be plucked out and put into the new data.")
    fun registerAugmentStat(id: String, datapoint: AugmentDatapoint, bl: Boolean){

    }

    @Deprecated("This doesn't do anything anymore")
    fun registerAugmentStat(augment: ScepterAugment){}

    /**
     * used to check if a registry or other initialization method should consider the provided augment.
     */
    fun checkIfAugmentEnabled(augment: ScepterAugment, id: Identifier): Boolean{
        return getAugmentEnabled(augment)
    }

    fun getAugment(id: String): ScepterAugment?{
        return Registries.ENCHANTMENT.get(Identifier(id)) as? ScepterAugment
    }

    fun getAugmentType(augment: ScepterAugment): SpellType {
        return augment.augmentData.type
    }
    @Deprecated("Use the direct Augment overload when possible")
    fun getAugmentType(id: String): SpellType {
        return getAugment(id)?.augmentData?.type ?: SpellType.NULL
    }

    fun getAugmentItem(augment: ScepterAugment): Item {
        return augment.augmentData.keyItem
    }
    @Deprecated("Use the direct Augment overload when possible")
    fun getAugmentItem(id: String): Item {
        return getAugment(id)?.augmentData?.keyItem ?: Items.AIR
    }

    fun getAugmentMinLvl(augment: ScepterAugment): Int {
        return augment.augmentData.minLvl
    }
    @Deprecated("Use the direct Augment overload when possible")
    fun getAugmentMinLvl(id: String): Int {
        return getAugment(id)?.augmentData?.minLvl ?: 1
    }
    
    fun getAugmentCurrentLevel(scepterLevel: Int, augment: ScepterAugment): Int{
        val minLvl = getAugmentMinLvl(augment)
        val maxLevel = (augment.getAugmentMaxLevel()) + minLvl - 1
        var testLevel = 1
        if (scepterLevel >= minLvl){
            testLevel = scepterLevel
            if (testLevel > maxLevel) testLevel = maxLevel
            testLevel -= (minLvl - 1)
        }
        return testLevel
    }

    fun getAugmentManaCost(augment: ScepterAugment, reduction: Double = 1.0): Int{
        val cost = (augment.augmentData.manaCost * reduction).toInt()
        return max(1,cost)
    }
    @Deprecated("Use the direct Augment overload when possible")
    fun getAugmentManaCost(id: String, reduction: Double = 1.0): Int{
        val cost = (getAugment(id)?.augmentData?.manaCost?.times(reduction))?.toInt() ?: (10 * reduction).toInt()
        return max(0,cost)
    }

    fun getAugmentCooldown(augment: ScepterAugment): PerLvlI{
        return augment.augmentData.cooldown
    }
    private val DEFAULT_COOLDOWN = PerLvlI(20)
    @Deprecated("Use the direct Augment overload when possible")
    fun getAugmentCooldown(id: String): PerLvlI{
        return getAugment(id)?.augmentData?.cooldown ?: DEFAULT_COOLDOWN
    }

    fun getAugmentImbueLevel(augment: ScepterAugment): Int{
        val cd = augment.augmentData.imbueLevel
        return max(1,cd)
    }
    @Deprecated("Use the direct Augment overload when possible")
    fun getAugmentImbueLevel(id: String): Int{
        val il = getAugment(id)?.augmentData?.imbueLevel ?: 1
        return max(1,il)
    }

    fun getAugmentTier(augment: ScepterAugment): LoreTier {
        return augment.augmentData.bookOfLoreTier
    }
    @Deprecated("Use the direct Augment overload when possible")
    fun getAugmentTier(id: String): LoreTier {
        return getAugment(id)?.augmentData?.bookOfLoreTier ?: LoreTier.NO_TIER
    }

    fun getAugmentEnabled(augment: ScepterAugment): Boolean {
        return augment.augmentData.enabled
    }
    @Deprecated("Use the direct Augment overload when possible")
    fun getAugmentEnabled(id: String): Boolean {
        return getAugment(id)?.augmentData?.enabled ?: true
    }

    fun getAugmentPvpMode(augment: ScepterAugment): Boolean {
        return augment.augmentData.pvpMode
    }
    @Deprecated("Use the direct Augment overload when possible")
    fun getAugmentPvpMode(id: String): Boolean {
        return getAugment(id)?.augmentData?.pvpMode ?: false
    }

    fun getAugmentCastXp(augment: ScepterAugment): Int{
        return augment.augmentData.castXp
    }
    @Deprecated("Use the direct Augment overload when possible")
    fun getAugmentCastXp(id: String): Int{
        return getAugment(id)?.augmentData?.castXp ?: 1
    }

    fun getAugmentDatapoint(augment: ScepterAugment): me.fzzyhmstrs.amethyst_core.scepter_util.data.AugmentDatapoint {
        return augment.augmentData
    }
    @Deprecated("Use the direct Augment overload when possible")
    fun getAugmentDatapoint(id: String): me.fzzyhmstrs.amethyst_core.scepter_util.data.AugmentDatapoint {
        return getAugment(id)?.augmentData ?: me.fzzyhmstrs.amethyst_core.scepter_util.data.AugmentDatapoint()
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
