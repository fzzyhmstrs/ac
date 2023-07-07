package me.fzzyhmstrs.amethyst_core.augments.paired

import me.fzzyhmstrs.amethyst_core.augments.LevelProviding
import me.fzzyhmstrs.amethyst_core.augments.ScepterAugment
import me.fzzyhmstrs.amethyst_core.boost.AugmentBoost
import me.fzzyhmstrs.amethyst_core.entity.ModifiableEffectEntity
import me.fzzyhmstrs.amethyst_core.event.BlockHitActionEvent
import me.fzzyhmstrs.amethyst_core.event.EntityHitActionEvent
import me.fzzyhmstrs.amethyst_core.modifier.AugmentConsumer
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import me.fzzyhmstrs.amethyst_core.modifier.AugmentModifier
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute
import me.fzzyhmstrs.amethyst_core.scepter.SpellType
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlD
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlF
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.enchantment.Enchantment
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.hit.BlockHitResult
import net.minecraft.util.hit.EntityHitResult
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.random.Random
import net.minecraft.world.World

class PairedAugments private constructor (internal val augments: Array<ScepterAugment>, private val boost: AugmentBoost? = null){

    constructor(): this(arrayOf())

    constructor(main: ScepterAugment): this(arrayOf(main))

    constructor(main: ScepterAugment, pairedSpell: ScepterAugment?, boost: AugmentBoost?): this(if(pairedSpell != null) arrayOf(main, pairedSpell) else arrayOf(main),boost)

    private val type: Type = if (augments.isEmpty()){
        Type.EMPTY
    } else if (augments.size == 1){
        Type.SINGLE
    } else{
        Type.PAIRED
    }
    private val cooldown: PerLvlI by lazy{
        if (augments.isEmpty()){
            PerLvlI()
        } else if (augments.size == 1){
            augments[0].augmentData.cooldown.copy()
        } else {
            augments[1].modifyCooldown(augments[0].augmentData.cooldown.copy(),augments[0],augments[0].augmentType,this)

        }.plus(boost?.cooldownModifier ?: PerLvlI())
    }
    private val manaCost: PerLvlI by lazy {
        if (augments.isEmpty()){
            PerLvlI()
        } else if (augments.size == 1){
            PerLvlI(augments[0].augmentData.manaCost)
        } else{
            augments[1].modifyManaCost(augments[0].augmentData.cooldown.copy(),augments[0],augments[0].augmentType,this)
        }.plus(boost?.manaCostModifier ?: PerLvlI())
    }
    private val augmentEffects: AugmentEffect by lazy {
        when (type){
            Type.SINGLE ->{
                augments[0].baseEffect
            }
            Type.PAIRED ->{
                AugmentEffect(
                    augments[1].modifyDamage(augments[0].baseEffect.damageData.copy(),augments[0],augments[0].augmentType,this),
                    augments[1].modifyAmplifier(augments[0].baseEffect.amplifierData.copy(),augments[0],augments[0].augmentType,this),
                    augments[1].modifyDuration(augments[0].baseEffect.durationData.copy(),augments[0],augments[0].augmentType,this),
                    augments[1].modifyRange(augments[0].baseEffect.rangeData.copy(),augments[0],augments[0].augmentType,this)
                )
            }
            Type.EMPTY ->{
                AugmentEffect()
            }
        }
    }
    private val name: MutableText
    private val enabled: Boolean
    private val maxLevel: Int
    private val random = Random.createThreadSafe()

    init{
        
        name = if(augments.isEmpty()){
            enabled = false
            maxLevel = 0
            AcText.empty()
        }  else if (augments.size == 1){
            enabled = augments[0].augmentData.enabled
            maxLevel = augments[0].maxLevel
            AcText.translatable(augments[0].translationKey)
        } else {
            enabled = augments[0].augmentData.enabled
            maxLevel = augments[0].maxLevel
            if (spellsAreEqual()){
                augments[0].doubleName()
            } else {
                val specialName = augments[1].specialName(augments[0])
                if (specialName != AcText.empty()) {
                    specialName
                } else {
                    augments[0].augmentName(augments[1])
                }
            }
        }
    }

    fun primary(): ScepterAugment?{
        return if(augments.isNotEmpty()) augments[0] else null
    }

    fun paired(): ScepterAugment?{
        return if(augments.size > 1) augments[1] else null
    }

    fun boost(): AugmentBoost? {
        return boost
    }

    fun spellsAreEqual(): Boolean{
        return if (augments.size < 2) false else augments[0] == augments[1]
    }

    private val castParticleEffect by lazy{
        when(type){
            Type.SINGLE -> augments[0].castParticleType()?:ParticleTypes.CRIT
            Type.PAIRED -> augments[1].castParticleType()?:augments[0].castParticleType()?:ParticleTypes.CRIT
            Type.EMPTY -> ParticleTypes.CRIT
        }
    }

    override fun toString(): String {
        return "Paired Augment:[${primary()}, ${paired()}, ${boost()}]"
    }

    fun getCastParticleType(): ParticleEffect {
        return castParticleEffect
    }

    fun getHitParticleType(hit: HitResult): ParticleEffect {
        return when(type){
            Type.SINGLE -> augments[0].hitParticleType(hit)?:ParticleTypes.CRIT
            Type.PAIRED -> augments[1].hitParticleType(hit)?:augments[0].hitParticleType(hit)?:ParticleTypes.CRIT
            Type.EMPTY -> ParticleTypes.CRIT
        }
    }

    fun processAugmentEffects(user: LivingEntity, modifierData: AugmentModifier): AugmentEffect{
        val effectModifiers = AugmentEffect(
            PerLvlF(0f,0f,(user.getAttributeValue(RegisterAttribute.SPELL_DAMAGE).toFloat() - 1f) * 100f),
            PerLvlI(user.getAttributeValue(RegisterAttribute.SPELL_AMPLIFIER).toInt()),
            PerLvlI(0,0,(user.getAttributeValue(RegisterAttribute.SPELL_DURATION).toInt() - 1) * 100),
            PerLvlD(0.0,0.0,(user.getAttributeValue(RegisterAttribute.SPELL_RANGE) - 1.0) * 100.0)
        )
        effectModifiers.plus(modifierData.getEffectModifier())
        if (boost != null){
            effectModifiers.plus(boost.boostEffect)
        }
        return effectModifiers.plus(this.augmentEffects)
    }
    fun <T> processOnCast(world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    MutableList<Identifier>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return processOnCast(ProcessContext.EMPTY, world, source, user, hand, level, effects)
    }
    fun <T> processOnCast(context: ProcessContext, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    MutableList<Identifier>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val returnList: MutableList<Identifier> = mutableListOf()
        if (type == Type.SINGLE) {
            val result = augments[0].onCast(context, world, source, user, hand, level, effects, AugmentType.EMPTY, this)
            if (result.success()){
                returnList.addAll(result.results())
            }
        } else if (type == Type.PAIRED){
            val result = augments[1].onCast(context, world,source, user,hand,level, effects,augments[0].augmentType, this)
            if (result.success()){
                returnList.addAll(result.results())
                if (!result.overwrite()){
                    val result2 = augments[0].onCast(context, world,source, user,hand,level, effects,
                        AugmentType.EMPTY, this)
                    if (result2.success()){
                        returnList.addAll(result2.results())
                    }
                }
            }
        }
        return returnList
    }

    fun <T> processMultipleEntityHits(entityHitResults: List<EntityHitResult>, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    MutableList<Identifier>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return processMultipleEntityHits(entityHitResults,
            ProcessContext.EMPTY, world, source, user, hand, level, effects)
    }
    
    fun <T> processMultipleEntityHits(entityHitResults: List<EntityHitResult>, context: ProcessContext, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    MutableList<Identifier>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        var successes = 0
        val actionList: MutableList<Identifier> = mutableListOf()
        for (entityHitResult in entityHitResults){
            if(processEntityHit(entityHitResult,context,world,source,user,hand,level,effects).also { actionList.addAll(it) }.isNotEmpty()) {
                successes++
                val entity = entityHitResult.entity
                if (entity is LivingEntity){
                    effects.accept(entity, AugmentConsumer.Type.HARMFUL)
                }
            }
        }
        if (successes > 0){
            EntityHitActionEvent.EVENT.invoker().onAction(world,user,actionList, *entityHitResults.toTypedArray())
            effects.accept(user, AugmentConsumer.Type.BENEFICIAL)
        }
        return actionList
    }
    
    fun <T> processSingleEntityHit(entityHitResult: EntityHitResult, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    MutableList<Identifier>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return processSingleEntityHit(entityHitResult, ProcessContext.EMPTY, world, source, user, hand, level, effects)
    }
    fun <T> processSingleEntityHit(entityHitResult: EntityHitResult, context: ProcessContext, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    MutableList<Identifier>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val actionList = processEntityHit(entityHitResult,context,world,source, user, hand, level, effects)
        if (actionList.isNotEmpty()){
            val entity = entityHitResult.entity
            if (entity is LivingEntity){
                effects.accept(entity, AugmentConsumer.Type.HARMFUL)
            }
            EntityHitActionEvent.EVENT.invoker().onAction(world,user,actionList, entityHitResult)
            effects.accept(user, AugmentConsumer.Type.BENEFICIAL)
        }
        return actionList
    }
    
    private fun <T> processEntityHit(entityHitResult: EntityHitResult, context: ProcessContext, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    MutableList<Identifier>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val returnList: MutableList<Identifier> = mutableListOf()
        if (type == Type.SINGLE) {
            val result = augments[0].onEntityHit(entityHitResult,context, world,source, user, hand, level, effects,
                AugmentType.EMPTY, this)
            if (result.success()){
                returnList.addAll(result.results())
            }
        } else if (type == Type.PAIRED){
            val result = augments[1].onEntityHit(entityHitResult,context, world,source, user, hand, level, effects,augments[0].augmentType, this)
            if (result.success()){
                returnList.addAll(result.results())
                if (!result.overwrite()){
                    val result2 = augments[0].onEntityHit(entityHitResult,context, world,source, user,hand,level, effects,
                        AugmentType.EMPTY, this)
                    if (result2.success()){
                        returnList.addAll(result2.results())
                    }
                }
            }
        }
        return returnList
    }

    fun <T> processMultipleBlockHits(blockHitResults: List<BlockHitResult>, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    MutableList<Identifier>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return processMultipleBlockHits(blockHitResults, ProcessContext.EMPTY, world, source, user, hand, level, effects)
    }
    
    fun <T> processMultipleBlockHits(blockHitResults: List<BlockHitResult>, context: ProcessContext, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    MutableList<Identifier>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        var successes = 0
        val actionList: MutableList<Identifier> = mutableListOf()
        for (blockHitResult in blockHitResults){
            if(processBlockHit(blockHitResult,context,world,source,user,hand,level,effects).also { actionList.addAll(it) }.isNotEmpty()) {
                successes++
            }
        }
        if (successes > 0){
            BlockHitActionEvent.EVENT.invoker().onAction(world,user,actionList,*blockHitResults.toTypedArray())
            effects.accept(user, AugmentConsumer.Type.BENEFICIAL)
        }
        return actionList
    }

    fun <T> processSingleBlockHit(blockHitResult: BlockHitResult, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    MutableList<Identifier>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return processSingleBlockHit(blockHitResult, ProcessContext.EMPTY, world, source, user, hand, level, effects)
    }
    
    fun <T> processSingleBlockHit(blockHitResult: BlockHitResult, context: ProcessContext, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    MutableList<Identifier>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val actionList = processBlockHit(blockHitResult,context,world,source, user, hand, level, effects)
        if (actionList.isNotEmpty()){
            BlockHitActionEvent.EVENT.invoker().onAction(world,user,actionList,blockHitResult)
            effects.accept(user, AugmentConsumer.Type.BENEFICIAL)
        }
        return actionList
    }
    
    private fun <T> processBlockHit(blockHitResult: BlockHitResult, context: ProcessContext, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    MutableList<Identifier>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val returnList: MutableList<Identifier> = mutableListOf()
        if (type == Type.SINGLE) {
            for (augment in augments) {
                val result = augment.onBlockHit(blockHitResult,context, world,source, user, hand, level, effects,
                    AugmentType.EMPTY, this)
                if (result.success()){
                    returnList.addAll(result.results())
                }
            }
        } else if (type == Type.PAIRED){
            val result = augments[1].onBlockHit(blockHitResult,context, world,source, user,hand,level, effects,augments[0].augmentType, this)
            if (result.success()){
                returnList.addAll(result.results())
                if (!result.overwrite()){
                    val result2 = augments[0].onBlockHit(blockHitResult,context, world,source, user,hand,level, effects,
                        AugmentType.EMPTY, this)
                    if (result2.success()){
                        returnList.addAll(result.results())
                    }
                }
            }
        }
        return returnList
    }

    fun <T> processOnKill(entityHitResult: EntityHitResult, world: World,source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        processOnKill(entityHitResult, ProcessContext.EMPTY, world, source, user, hand, level, effects)
    }
    fun <T> processOnKill(entityHitResult: EntityHitResult, context: ProcessContext, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect)
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        if (type == Type.PAIRED){
            val result = augments[1].onEntityKill(entityHitResult,context, world, source, user,hand,level, effects,augments[0].augmentType, this)
            if (result.success() && !result.overwrite()){
                augments[0].onEntityKill(entityHitResult,context, world,source, user,hand,level, effects,AugmentType.EMPTY, this)
            }
        } else {
            for (augment in augments) {
                val result = augment.onEntityKill(entityHitResult,context, world,source, user, hand, level, effects, AugmentType.EMPTY, this)
                if (!result.success()) break
            }
        }
    }
    
    fun provideName(level: Int): Text {
        val text = name
        if (level != 1 || maxLevel != 1 ) {
            text.append(" ").append(AcText.translatable("enchantment.level.$level"))
        }
        if(!enabled){
            text.append(AcText.translatable("scepter.augment.disabled"))
            text.formatted(Formatting.DARK_RED).formatted(Formatting.STRIKETHROUGH)
        }
        return text
    }
    
    fun provideDescription(): List<Text>{
        val textList: MutableList<Text> = mutableListOf()
        if (type != Type.EMPTY){
            textList.add(AcText.translatable(augments[0].translationKey + ".desc"))
        }
        if (type == Type.PAIRED || boost != null){
            textList.add(AcText.translatable("scepter.augment.changes"))
        }
        if (type == Type.PAIRED) {
            val list: MutableList<Text> = mutableListOf()
            augments[1].appendBaseDescription(list,augments[0],augments[0].augmentType)
            for (text in list) {
                textList.add(AcText.literal("  ").append(text))
            }
        }
        if (boost != null) {
            val list: MutableList<Text> = mutableListOf()
            boost.appendDescription(list)
            for (text in list) {
                textList.add(AcText.literal("  ").append(text))
            }
        }
        return textList
    }

    fun provideCooldown(level: Int): Int{
        return cooldown.value(level)
    }

    fun provideManaCost(level: Int): Int{
        return manaCost.value(level)
    }

    fun provideStack(stack: ItemStack): ItemStack{
        return boost?.modifyStack(stack) ?: stack
    }

    fun provideLevel(enchantment: Enchantment): Int{
        var lvl = if (boost is LevelProviding){
            boost.provideLevel(enchantment)
        } else {
            0
        }
        if (type == Type.PAIRED){
           lvl += augments[1].provideLevel(enchantment)
        }
        return lvl
    }

    fun <T> provideDealtDamage(amount: Float, cause: ScepterAugment, entityHitResult: EntityHitResult, user: T, world: World, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    Float
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        val amount1 = if (type == Type.PAIRED){
            if (cause == augments[0]){
                augments[1].modifyDealtDamage(amount,cause, entityHitResult, user, world, hand, level, effects, augments[0].augmentType, this)
            } else {
                augments[0].modifyDealtDamage(amount,cause, entityHitResult, user, world, hand, level, effects, augments[1].augmentType, this)
            }
        } else {
            amount
        }
        return boost?.modifyDamage(amount1, cause, entityHitResult, user, world, hand, level, effects, this) ?: amount1
    }
    
    fun <T> provideDamageSource(builder: DamageSourceBuilder, cause: ScepterAugment, entityHitResult: EntityHitResult, source: Entity?, user: T, world: World, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    DamageSource
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return if (type == Type.PAIRED){
            if (cause == augments[0]){
                val mod = augments[1].modifyDamageSource(builder, cause, entityHitResult, source, user, world, hand, level, effects, augments[0].augmentType, this)
                boost?.modifyDamageSource(mod,user,source)?.build()?:mod.build()
            } else {
                val mod = augments[0].modifyDamageSource(builder,cause, entityHitResult, source, user, world, hand, level, effects, augments[1].augmentType, this)
                boost?.modifyDamageSource(mod,user,source)?.build()?:mod.build()
            }
        } else {
            boost?.modifyDamageSource(builder,user,source)?.build()?:builder.build()
        }
    }
    
    fun <T, U> provideSummons(summons: List<T>, cause: ScepterAugment, user: U, world: World, hand: Hand, level: Int, effects: AugmentEffect)
    : List<T>
    where 
    T: Entity,
    T: ModifiableEffectEntity 
    where 
    U: LivingEntity,
    U: SpellCastingEntity
    {
        return if (type == Type.PAIRED){
            if (cause == augments[0]) {
                augments[1].modifySummons(summons,cause, user, world, hand, level, effects, augments[0].augmentType, this)
            } else {
                augments[0].modifySummons(summons,cause, user, world, hand, level, effects, augments[1].augmentType, this)
            }
        } else {
            summons
        }
    }

    fun <T> provideDrops(drops: List<ItemStack>, cause: ScepterAugment, user: T, world: World, hand: Hand, level: Int, effects: AugmentEffect)
    : 
    List<ItemStack>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return if (type == Type.PAIRED){
            if (cause == augments[0]) {
                augments[1].modifyDrops(drops,cause, user, world, hand, level, effects, augments[0].augmentType, this)
            } else {
                augments[0].modifyDrops(drops,cause, user, world, hand, level, effects, augments[1].augmentType, this)
            }
        } else {
            drops
        }
    }
    
    fun provideCastXp(spellType: SpellType): Int{
        val primaryAmount = if(type == Type.EMPTY || augments[0].augmentData.type != spellType){
            0
        } else {
            if (type == Type.PAIRED){
                getPartialXp(augments[1].augmentData.castXp,0.75f)
            } else {
                augments[1].augmentData.castXp
            }
        }
        val secondaryAmount = if(type == Type.PAIRED){
            if (augments[1].augmentData.type != spellType){
                0
            } else {
                getPartialXp(augments[1].augmentData.castXp,0.5f)
            }
        } else {
            0
        }
        return primaryAmount + secondaryAmount
    }
    private fun getPartialXp(base: Int, fraction: Float): Int{
        val a = (base * fraction).toInt()
        val b = base % fraction
        val c = if(b == 0f){
            0
        } else {
            if (random.nextFloat() < b){
                1
            } else {
                0
            }
        }
        return a + c
    }

    fun <T> causeExplosion(builder: ExplosionBuilder, cause: ScepterAugment, user: T, world: World, hand: Hand, level: Int, effects: AugmentEffect)
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return if (type == Type.PAIRED){
            if (cause == augments[0]) {
                augments[1].modifyExplosion(builder,cause, user, world, hand, level, effects, augments[0].augmentType, this).explode(world)
            } else {
                augments[0].modifyExplosion(builder,cause, user, world, hand, level, effects, augments[1].augmentType, this).explode(world)
            }
        } else {
            builder.explode(world)
        }
    }

    private enum class Type{
        EMPTY,
        SINGLE,
        PAIRED
    }
}
