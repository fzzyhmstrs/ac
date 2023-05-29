package me.fzzyhmstrs.amethyst_core.scepter_util.augments

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.event.AfterSpellEvent
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentConsumer
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentEffect
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterHelper
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterTier
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment.DamageProviderFunction
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.paired.*
import me.fzzyhmstrs.fzzy_core.coding_util.*
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.gson
import me.fzzyhmstrs.fzzy_core.coding_util.SyncedConfigHelper.readOrCreateUpdated
import me.fzzyhmstrs.fzzy_core.registry.SyncedConfigRegistry
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.network.PacketByteBuf
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.registry.Registries
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.world.World

/**
 * the base scepter augment. Any Augment-type scepter will be able to successfully cast an augment made with this class or one of the templates.
 */

abstract class ScepterAugment(
    private val tier: ScepterTier,
    private val maxLvl: Int,
    var augmentData: AugmentDatapoint,
    val augmentType: AugmentType
)
    :
    BaseScepterAugment()
{
    
    open val baseEffect = AugmentEffect()
    open val damageSource: DamageProviderFunction = DamageProviderFunction {p,_ -> if(p is PlayerEntity) DamageSource.player(p) else DamageSource.mob(p)}

    fun applyModifiableTasks(world: World, user: LivingEntity, hand: Hand, level: Int, modifiers: List<AugmentModifier> = listOf(), modifierData: AugmentModifier, pairedAugments: PairedAugments): Boolean{
        if (!augmentData.enabled) {
            if (user is PlayerEntity){
                user.sendMessage(AcText.translatable("scepter.augment.disabled_message", this.getName(1)), false)
            }
            return false
        }
        val effectModifiers = pairedAugments.processAugmentEffects(user, modifierData)
        val bl = applyTasks(world,user,hand,level,effectModifiers,pairedAugments)
        if (bl.result.isAccepted) {
            modifiers.forEach {
                val secondary = it.getSecondaryEffect()
                if (secondary != null) {
                    it.getSecondaryEffect()?.applyModifiableTasks(world, user, hand, level, listOf(), AugmentModifier(),
                        PairedAugments(secondary)
                    )
                }
            }
            effectModifiers.accept(user,AugmentConsumer.Type.AUTOMATIC)
            AfterSpellEvent.EVENT.invoker().afterCast(world,user,user.getStackInHand(hand),bl.value, this)
        }
        return bl.result.isAccepted
    }

    /**
     * The only mandatory method for extending in order to apply your spell effects. Other open functions below are available for use, but this method is where the basic effect implementation goes.
     */
    abstract fun applyTasks(world: World, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect, spells: PairedAugments): TypedActionResult<List<Identifier>>

    /**
     * If your scepter has some client side effects/tasks, extend them here. This can be something like adding visual effects, or affecting a GUI, and so on.
     */
    open fun clientTask(world: World, user: LivingEntity, hand: Hand, level: Int){
    }
    open fun onCast(context: ProcessContext, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments): TypedActionResult<List<Identifier>>{
        return TypedActionResult.pass(listOf())
    }
    open fun onBlockHit(blockHitResult: BlockHitResult,context: ProcessContext, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments): TypedActionResult<List<Identifier>>{
        return TypedActionResult.pass(listOf())
    }
    open fun onEntityHit(entityHitResult: EntityHitResult,context: ProcessContext, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments): TypedActionResult<List<Identifier>>{
        return TypedActionResult.pass(listOf())
    }
    open fun onEntityKill(entityHitResult: EntityHitResult,context: ProcessContext, world: World, source: Entity?, user: LivingEntity, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments): TypedActionResult<List<Identifier>>{
        return TypedActionResult.pass(listOf())
    }
    open fun modifyCooldown(cooldown: PerLvlI,other: ScepterAugment, othersType: AugmentType, spells: PairedAugments): PerLvlI{
        return cooldown
    }
    open fun modifyManaCost(manaCost: PerLvlI,other: ScepterAugment, othersType: AugmentType, spells: PairedAugments): PerLvlI{
        return manaCost
    }
    open fun modifyDamage(damage: PerLvlF, other: ScepterAugment, othersType: AugmentType, spells: PairedAugments): PerLvlF{
        return damage
    }
    open fun modifyAmplifier(amplifier: PerLvlI,other: ScepterAugment, othersType: AugmentType, spells: PairedAugments): PerLvlI{
        return amplifier
    }
    open fun modifyDuration(duration: PerLvlI,other: ScepterAugment, othersType: AugmentType, spells: PairedAugments): PerLvlI{
        return duration
    }
    open fun modifyRange(range: PerLvlD,other: ScepterAugment, othersType: AugmentType, spells: PairedAugments): PerLvlD{
        return range
    }
    open fun modifyDamage(amount: Float, cause: ScepterAugment, entityHitResult: EntityHitResult, user: LivingEntity, world: World, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments): Float{
        return amount
    }
    open fun modifyDamageSource(builder: DamageSourceBuilder, cause: ScepterAugment, entityHitResult: EntityHitResult, source: Entity?, user: LivingEntity, world: World, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments): DamageSourceBuilder {
        return builder
    }
    open fun damageSourceBuilder(source: Entity?, attacker: LivingEntity): DamageSourceBuilder{
        return DamageSourceBuilder(attacker, source)
    }
    open fun modifySummons(summons: List<Entity>, cause: ScepterAugment, user: LivingEntity, world: World, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments): List<Entity>{
        return summons
    }
    open fun modifyExplosion(builder: ExplosionBuilder, cause: ScepterAugment, user: LivingEntity, world: World, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments): ExplosionBuilder {
        return builder
    }

    open fun castSoundEvent(world: World, blockPos: BlockPos){
    }
    open fun hitSoundEvent(world: World, blockPos: BlockPos){
    }
    
    open fun castParticleType(): ParticleEffect {
        return ParticleTypes.CRIT
    }
    open fun hitParticleType(hit: HitResult): ParticleEffect {
        return ParticleTypes.CRIT
    }
    open fun splashParticles(hitResult: HitResult, world: World, x: Double, y: Double, z: Double, spells: PairedAugments){
        if (world is ServerWorld){
            val particle = spells.getHitParticleType(hitResult)
            world.spawnParticles(particle,x,y,z,20,.25,.25,.25,0.2)
        }
    }
    fun augmentName(stack: ItemStack, level: Int): Text{
        val enchantId = this.id?.toString()?:return getName(level)
        val pairedSpells = AugmentHelper.getPairedAugments(enchantId, stack)?:return getName(level)
        return pairedSpells.provideName(level)
    }
    open fun augmentName(pairedSpell: ScepterAugment): MutableText {
        return AcText.translatable(orCreateTranslationKey)
    }
    open fun specialName(otherSpell: ScepterAugment): MutableText {
        return AcText.empty()
    }
    open fun doubleName(): MutableText {
        return AcText.translatable("$orCreateTranslationKey.double")
    }
    open fun provideNoun(pairedSpell: ScepterAugment?): Text{
        return AcText.translatable(getTranslationKey() + ".noun")
    }
    open fun provideVerb(pairedSpell: ScepterAugment?): Text{
        return AcText.translatable(getTranslationKey() + ".verb")
    }
    abstract fun appendDescription(description: MutableList<Text>, other: ScepterAugment, otherType: AugmentType)

    open fun getAugmentMaxLevel(): Int{
        return maxLvl
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
        return augmentData.pvpMode
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

    companion object{

        const val augmentVersion = "_v2"
        private const val oldAugmentVersion = "_v1"

        val FAIL = actionResult(ActionResult.FAIL)
        fun actionResult(result: ActionResult,vararg ids: Identifier): TypedActionResult<List<Identifier>>{
            return actionResult(result,ids.asList())
        }
        fun actionResult(result: ActionResult,ids: List<Identifier>): TypedActionResult<List<Identifier>>{
            return when(result){
                ActionResult.SUCCESS -> TypedActionResult.success(ids)
                ActionResult.CONSUME -> TypedActionResult.consume(ids)
                ActionResult.CONSUME_PARTIAL -> TypedActionResult.consume(ids)
                ActionResult.PASS -> TypedActionResult.pass(ids)
                ActionResult.FAIL -> TypedActionResult.fail(ids)
            }
        }
        
        //small config class for syncing purposes
        class AugmentConfig(val id: String, stats: AugmentStats): SyncedConfigHelper.SyncedConfig{

            private var augmentStats: AugmentStats = stats

            init{
                initConfig()
            }

            override fun readFromServer(buf: PacketByteBuf) {
                augmentStats = gson.fromJson(buf.readString(), AugmentStats::class.java).validate()
                val augment = Registries.ENCHANTMENT.get(Identifier(id))
                if (augment != null && augment is ScepterAugment){
                    val currentDataPoint = augment.augmentData
                    val newDataPoint = currentDataPoint.copy(
                        cooldown = augmentStats.getCooldown(),
                        manaCost = augmentStats.manaCost,
                        minLvl = augmentStats.minLvl,
                        castXp = augmentStats.castXp,
                        enabled = augmentStats.enabled,
                        pvpMode = augmentStats.pvpMode
                    )
                    augment.augmentData = newDataPoint
                }
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
            @Suppress("UNUSED_VARIABLE") val config = AugmentConfig(file,configuredStats)
            return configuredStats
        }
    }


    fun interface DamageProviderFunction{
        fun provideDamageSource(dealer: LivingEntity, source: Entity?): DamageSource
    }
}
