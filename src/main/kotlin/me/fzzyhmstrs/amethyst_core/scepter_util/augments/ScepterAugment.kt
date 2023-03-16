package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.event.AfterSpellEvent
import me.fzzyhmstrs.amethyst_core.modifier_util.*
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterTier
import me.fzzyhmstrs.fzzy_core.coding_util.*
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.gson
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.readOrCreateUpdated
import me.fzzyhmstrs.fzzy_core.registry.SyncedConfigRegistry
import net.minecraft.enchantment.Enchantment
import net.minecraft.enchantment.EnchantmentTarget
import net.minecraft.entity.Entity
import net.minecraft.entity.EquipmentSlot
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.sound.SoundEvent
import net.minecraft.sound.SoundEvents
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.HitResult
import net.minecraft.world.World

/**
 * the base scepter augment. Any Augment-type scepter will be able to successfully cast an augment made with this class or one of the templates.
 */

abstract class ScepterAugment(
    private val tier: ScepterTier,
    private val maxLvl: Int)
    :
    Enchantment(Rarity.VERY_RARE,EnchantmentTarget.WEAPON, arrayOf(EquipmentSlot.MAINHAND))
{
    
    open val baseEffect = AugmentEffect()
    val id: Identifier? by lazy {
        Registries.ENCHANTMENT.getId(this)
    }
    val augmentSpecificModifier: AugmentModifier by lazy {
        generateUniqueModifier()
    }

    /**
     * The only mandatory method for extending in order to apply your spell effects. Other open functions below are available for use, but this method is where the basic effect implementation goes.
     */
    abstract fun applyTasks(world: World, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect): Boolean

    /**
     * define the augment characteristics here, such as mana cost, cooldown, etc. See [AugmentDatapoint] for more info.
     */
    abstract fun augmentStat(imbueLevel: Int = 1): AugmentDatapoint

    open fun generateUniqueModifier(): AugmentModifier{
        val augId = id?: return AugmentModifier(Identifier(AC.MOD_ID,"spell_boost"),2,-25.0,-10.0,false)
        return UniqueAugmentModifier(augId,2,-25.0,-10.0)
    }

    fun applyModifiableTasks(world: World, user: LivingEntity, hand: Hand, level: Int, modifiers: List<AugmentModifier> = listOf(), modifierData: AugmentModifier? = null): Boolean{
        val aug = Registries.ENCHANTMENT.getId(this) ?: return false
        if (!AugmentHelper.getAugmentEnabled(aug.toString())) {
            if (user is PlayerEntity){
                user.sendMessage(AcText.translatable("scepter.augment.disabled_message", this.getName(1)), false)
            }
            return false
        }
        val effectModifiers = AugmentEffect(
            PerLvlF(0f,0f,user.getAttributeValue(RegisterAttribute.SPELL_DAMAGE).toFloat()),
            PerLvlI(user.getAttributeValue(RegisterAttribute.SPELL_AMPLIFIER).toInt()),
            PerLvlI(0,0,user.getAttributeValue(RegisterAttribute.SPELL_DURATION).toInt()),
            PerLvlD(0.0,0.0,user.getAttributeValue(RegisterAttribute.SPELL_RANGE))
        )
        effectModifiers.plus(modifierData?.getEffectModifier()?: AugmentEffect())
        effectModifiers.plus(baseEffect)
        val bl = applyTasks(world,user,hand,level,effectModifiers)
        if (bl) {
            modifiers.forEach {
                if (it.hasSecondaryEffect()) {
                    it.getSecondaryEffect()?.applyModifiableTasks(world, user, hand, level, listOf(), null)
                }
            }
            effectModifiers.accept(user,AugmentConsumer.Type.AUTOMATIC)
            AfterSpellEvent.EVENT.invoker().afterCast(world,user,user.getStackInHand(hand), this)
        }
        return bl
    }

    /**
     * If your scepter has some client side effects/tasks, extend them here. This can be something like adding visual effects, or affecting a GUI, and so on.
     */
    open fun clientTask(world: World, user: LivingEntity, hand: Hand, level: Int){
    }

    /**
     * optional open method that you can use for applying effects to secondary entities. See Amethyst Imbuements Freezing augment for an example.
     */
    open fun entityTask(world: World, target: Entity, user: LivingEntity, level: Double, hit: HitResult?, effects: AugmentEffect){
    }

    /**
     * This method defines the sound that plays when the spell is cast. override this with your preferred sound event
     */
    open fun soundEvent(): SoundEvent {
        return SoundEvents.BLOCK_BEACON_ACTIVATE
    }

    protected fun toLivingEntityList(list: List<Entity>): List<LivingEntity>{
        val newList: MutableList<LivingEntity> = mutableListOf()
        list.forEach {
            if (it is LivingEntity){
                newList.add(it)
            }
        }
        return newList
    }

    override fun getMinPower(level: Int): Int {
        return 30
    }

    override fun getMaxPower(level: Int): Int {
        return 50
    }

    override fun getMaxLevel(): Int {
        return 1
    }

    open fun getAugmentMaxLevel(): Int{
        return maxLvl
    }

    override fun isTreasure(): Boolean {
        return true
    }

    override fun isAvailableForEnchantedBookOffer(): Boolean {
        return false
    }

    override fun isAvailableForRandomSelection(): Boolean {
        return false
    }


    override fun isAcceptableItem(stack: ItemStack): Boolean {
        return stack.isIn(tier.tag)
    }

    fun getTier(): Int{
        return tier.tier
    }

    fun getTag(): TagKey<Item>{
        return tier.tag
    }

    fun getPvpMode(): Boolean{
        val id = Registries.ENCHANTMENT.getId(this)?:return false
        return AugmentHelper.getAugmentPvpMode(id.toString())
    }

    companion object{

        const val augmentVersion = "_v2"
        private const val oldAugmentVersion = "_v1"

        //small config class for syncing purposes
        class AugmentConfig(val id: String, stats: AugmentStats): SyncedConfigHelper.SyncedConfig{

            private var augmentStats: AugmentStats = stats

            init{
                initConfig()
            }

            override fun readFromServer(buf: PacketByteBuf) {
                augmentStats = gson.fromJson(buf.readString(), AugmentStats::class.java).validate()
                val currentDataPoint = AugmentHelper.getAugmentDatapoint(augmentStats.id)
                val newDataPoint = currentDataPoint.copy(
                    cooldown = augmentStats.getCooldown(),
                    manaCost = augmentStats.manaCost,
                    minLvl = augmentStats.minLvl,
                    castXp = augmentStats.castXp,
                    enabled = augmentStats.enabled,
                    pvpMode = augmentStats.pvpMode
                )
                AugmentHelper.registerAugmentStat(augmentStats.id, newDataPoint,true)
            }

            override fun writeToClient(buf: PacketByteBuf) {
                buf.writeString(gson.toJson(augmentStats))
            }

            override fun initConfig() {
                SyncedConfigRegistry.registerConfig(id,this)
            }
        }


        class AugmentStats {
            var id: String = AC.fallbackId.toString()
            var enabled: Boolean = true
            var pvpMode: Boolean = false
            var cooldownBase: Int = 20
            var coolDownPerLvl: Int = 0
            var manaCost: Int = 2
            var minLvl: Int = 1
            var castXp: Int = 1

            fun setCooldown(cooldown: PerLvlI){
                cooldownBase = cooldown.base
                coolDownPerLvl = cooldown.perLevel
            }

            fun getCooldown(): PerLvlI{
                return PerLvlI(cooldownBase,coolDownPerLvl,0)
            }

            fun validate(): AugmentStats{
                if (cooldownBase < 0) cooldownBase = 0
                if (cooldownBase == 0 && coolDownPerLvl < 0) coolDownPerLvl = 0
                if (manaCost < 0) manaCost = 0
                if (minLvl < 1) minLvl = 1
                if (castXp < 0) castXp = 0
                return this
            }
        }

        class AugmentStatsV1: SyncedConfigHelper.OldClass<AugmentStats> {
            var id: String = AC.fallbackId.toString()
            var enabled: Boolean = true
            var cooldown: Int = 20
            var manaCost: Int = 2
            var minLvl: Int = 1
            override fun generateNewClass(): AugmentStats {
                val augmentStats = AugmentStats()
                augmentStats.id = id
                augmentStats.enabled = enabled
                augmentStats.cooldownBase = cooldown
                augmentStats.manaCost = manaCost
                augmentStats.minLvl = minLvl
                return augmentStats.validate()
            }
        }

        fun configAugment(file: String, configClass: AugmentStats): AugmentStats {
            val ns = Identifier(configClass.id).namespace
            val oldFile = file.substring(0,file.length - 8) + oldAugmentVersion + ".json"
            val base = if(ns == "minecraft"){
                AC.MOD_ID
            } else {
                ns
            }
            val configuredStats = readOrCreateUpdated(file, oldFile,"augments", base, configClass = {configClass}, previousClass = {AugmentStatsV1()})
            val config = AugmentConfig(file,configuredStats)
            return configuredStats
        }
    }
}
