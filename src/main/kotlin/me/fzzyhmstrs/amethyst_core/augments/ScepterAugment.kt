package me.fzzyhmstrs.amethyst_core.augments

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.augments.data.AugmentDatapoint
import me.fzzyhmstrs.amethyst_core.augments.paired.*
import me.fzzyhmstrs.amethyst_core.entity.ModifiableEffectEntity
import me.fzzyhmstrs.amethyst_core.event.AfterSpellEvent
import me.fzzyhmstrs.amethyst_core.interfaces.SpellCastingEntity
import me.fzzyhmstrs.amethyst_core.modifier.AugmentConsumer
import me.fzzyhmstrs.amethyst_core.modifier.AugmentEffect
import me.fzzyhmstrs.amethyst_core.modifier.AugmentModifier
import me.fzzyhmstrs.amethyst_core.scepter.ScepterTier
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlD
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlF
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.entity.Entity
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.damage.DamageSource
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.particle.ParticleEffect
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.registry.tag.TagKey
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
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
    val augmentType: AugmentType
)
    :
    BaseScepterAugment(), LevelProviding
{

    abstract val augmentData: AugmentDatapoint

    open val baseEffect = AugmentEffect()

    override fun generateId(): Identifier {
        return augmentData.id
    }

    fun <T> applyModifiableTasks(world: World, user: T, hand: Hand, level: Int, pairedAugments: PairedAugments, context: ProcessContext = ProcessContext.EMPTY_CONTEXT, modifiers: List<AugmentModifier> = listOf(), modifierData: AugmentModifier = AugmentModifier())
    : 
    Boolean
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        if (!augmentData.enabled) {
            if (user is PlayerEntity){
                user.sendMessage(AcText.translatable("scepter.augment.disabled_message", this.getName(1)), false)
            }
            return false
        }
        val effectModifiers = pairedAugments.processAugmentEffects(user, modifierData)
        val bl = applyTasks(world, spellContext(context), user, hand, level, effectModifiers, pairedAugments)
        if (bl.success()) {
            modifiers.forEach {
                val secondary = it.getSecondaryEffect()
                if (secondary != null) {
                    it.getSecondaryEffect()?.applyModifiableTasks(world, user, hand, level,PairedAugments(secondary),
                        ProcessContext.EMPTY_CONTEXT, listOf(), AugmentModifier(),

                    )
                }
            }
            effectModifiers.accept(user,AugmentConsumer.Type.AUTOMATIC)
            AfterSpellEvent.EVENT.invoker().afterCast(world,user,user.getStackInHand(hand),bl.results(), this)
        }
        return bl.success()
    }

    /**
     * The only mandatory method for extending in order to apply your spell effects. Other open functions below are available for use, but this method is where the basic effect implementation goes.
     */
    abstract fun <T> applyTasks(world: World,context: ProcessContext, user: T, hand: Hand, level: Int, effects: AugmentEffect, spells: PairedAugments)
    : 
    SpellActionResult 
    where 
    T: LivingEntity,
    T: SpellCastingEntity

    /**
     * If your scepter has some client side effects/tasks, extend them here. This can be something like adding visual effects, or affecting a GUI, and so on.
     */
    open fun clientTask(world: World, user: LivingEntity, hand: Hand, level: Int){
    }

    open fun <T> onCast(context: ProcessContext, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    : 
    SpellActionResult
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return SUCCESSFUL_PASS
    }
    
    open fun <T> onBlockHit(blockHitResult: BlockHitResult, context: ProcessContext, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    : 
    SpellActionResult
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return SUCCESSFUL_PASS
    }
    open fun <T> onEntityHit(entityHitResult: EntityHitResult, context: ProcessContext, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    : 
    SpellActionResult
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return SUCCESSFUL_PASS
    }
    open fun <T> onEntityKill(entityHitResult: EntityHitResult, context: ProcessContext, world: World, source: Entity?, user: T, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    : 
    SpellActionResult
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return SUCCESSFUL_PASS
    }

    /**
     * Basic Attribute Modification
     *
     * Modifies the spell cooldown in some way. For example, Ice elemental augments might lower spell cooldown by 10% when paired
     */
    open fun modifyCooldown(cooldown: PerLvlI, other: ScepterAugment, othersType: AugmentType, spells: PairedAugments): PerLvlI{
        return cooldown
    }

    /**
     * Basic Attribute Modification
     *
     * Modifies the spell mana cost in some way. For example, lightning elemental augments might lower spell mana costs by 10% when paired
     */
    open fun modifyManaCost(manaCost: PerLvlI, other: ScepterAugment, othersType: AugmentType, spells: PairedAugments): PerLvlI{
        return manaCost
    }
    /**
     * Basic Attribute Modification
     *
     * Modifies spell damage in some way. For example, double-paired spells might have 25% increased base damage.
     *
     * This is NOT the same as [modifyDealtDamage], which is context-aware. This is modifying the basic [PerLvlF] for getting the damage on cast.
     */
    open fun modifyDamage(damage: PerLvlF, other: ScepterAugment, othersType: AugmentType, spells: PairedAugments): PerLvlF{
        return damage
    }
    /**
     * Basic Attribute Modification
     *
     * Modifies spell amplifier in some way. For example, double-paired spells might have +2 amplifier.
     */
    open fun modifyAmplifier(amplifier: PerLvlI, other: ScepterAugment, othersType: AugmentType, spells: PairedAugments): PerLvlI{
        return amplifier
    }
    /**
     * Basic Attribute Modification
     *
     * Modifies spell duration in some way. For example, double-paired spells might have + 40 tick duration.
     */
    open fun modifyDuration(duration: PerLvlI, other: ScepterAugment, othersType: AugmentType, spells: PairedAugments): PerLvlI{
        return duration
    }
    /**
     * Basic Attribute Modification
     *
     * Modifies spell range in some way. For example, double-paired spells might have 50% increased range.
     */
    open fun modifyRange(range: PerLvlD, other: ScepterAugment, othersType: AugmentType, spells: PairedAugments): PerLvlD{
        return range
    }

    /**
     * Modifies damage in a context aware way.
     *
     * A simple example is modifying damage based on the struck entity's EntityType. Damage might be doubled against Aquatic mobs, for example.
     */
    open fun <T> modifyDealtDamage(amount: Float, context: ProcessContext, entityHitResult: EntityHitResult, user: T, world: World, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    : 
    Float
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return amount
    }
    /**
     * Modifies the damage source a spell uses for damaging something.
     *
     * A simple example is modifying damage based on the struck entity's EntityType. Damage might be doubled against Aquatic mobs, for example.
     */
    open fun <T> modifyDamageSource(builder: DamageSourceBuilder, context: ProcessContext, entityHitResult: EntityHitResult, source: Entity?, user: T, world: World, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    : 
    DamageSourceBuilder 
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return builder
    }
    /**
     * Creates the starting point for modifying a damage source to provide to a damage-dealing method.
     *
     * Spells should overwrite this to provide the proper initial damage source in the builder
     */
    open fun damageSourceBuilder(world: World, source: Entity?, attacker: LivingEntity): DamageSourceBuilder {
        return DamageSourceBuilder(world,attacker, source)
    }

    /**
     * Modifies (or replaces) summoned entities.
     *
     * A common usage is to replace a basic version of summoned entities with a special paired version (like Unhallowed -> Incinerated for a pairing with a fire spell)
     *
     * Can also modify certain features like armor, provide persistent attributes like health, speed, etc., or change quantity summoned
     */
    open fun <T, U> modifySummons(summons: List<T>, context: ProcessContext, user: U, world: World, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    : 
    List<T> 
    where 
    T: Entity,
    T: ModifiableEffectEntity,
    U: LivingEntity,
    U: SpellCastingEntity
    {
        return summons
    }

    /**
     * Modifies (or replaces) projectiles for use in missile or other ranged contexts
     *
     * A common usage would be to replace a default projectile with a new one like ShardEntity -> MissileEntity
     *
     *
     */
    open fun <T, U> modifyProjectile(projectile: T, context: ProcessContext, user: U, world: World, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    :
    T
    where
    T: Entity,
    T: ModifiableEffectEntity,
    U: LivingEntity,
    U: SpellCastingEntity
    {
        return projectile
    }

    /**
     * Uses an explosion builder to create a custom explosion
     *
     * Use to do things like changing the blocks that are created (snow instead of fire, for example)
     */
    open fun <T> modifyExplosion(builder: ExplosionBuilder, context: ProcessContext, user: T, world: World, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    : 
    ExplosionBuilder 
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return builder
    }

    /**
     * Modifies the list of drops from a drop source
     *
     * Currently unused by anything in AC itself, it can be used by something like Excavate to drop smelted things if paired with flame spell.
     */
    open fun <T> modifyDrops(stacks: List<ItemStack>, context: ProcessContext, user: T, world: World, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    : 
    List<ItemStack>
    where 
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return stacks
    }

    fun <T> modifyCount(start: Int, context: ProcessContext, user: T, world: World, hand: Hand, level: Int, effects: AugmentEffect, othersType: AugmentType, spells: PairedAugments)
    :
    Int
    where
    T: LivingEntity,
    T: SpellCastingEntity
    {
        return start
    }
    /**
     * sound event to be played on cast
     */
    open fun castSoundEvent(world: World, blockPos: BlockPos){
    }

    /**
     * sound event to be played on hit
     */
    open fun hitSoundEvent(world: World, blockPos: BlockPos){
    }

    /**
     * the particle type for the cast. This is, for example, the particles that trail a missile entity
     */
    open fun castParticleType(): ParticleEffect?{
        return null
    }
    /**
     * the particle type for hitting something. This is, for example, the particles that will appear when a missile hits a block
     */
    open fun hitParticleType(hit: HitResult): ParticleEffect?{
        return null
    }

    /**
     * generates a "splash" of particles at a HitResult
     */
    open fun splashParticles(hitResult: HitResult, world: World, x: Double, y: Double, z: Double, spells: PairedAugments){
        if (world is ServerWorld){
            val particle = spells.getHitParticleType(hitResult)
            world.spawnParticles(particle,x,y,z,20,.25,.25,.25,0.2)
        }
    }
    /**
     * Used by the mixin to generate the mixed name from the paired spells
     */
    fun augmentName(stack: ItemStack, level: Int): Text{
        val enchantId = this.id.toString()
        val pairedSpells = AugmentHelper.getPairedAugments(enchantId, stack) ?:return getName(level)
        return pairedSpells.provideName(level)
    }

    /**
     * This is the primary method for building the name from pieces.
     *
     * Generally it shouldn't have to be overwritten unless there is a reason the normal description key won't work. Overwrite [provideArgs] to supply the language key with the parts it needs
     *
     * for special cases, [specialName] should be used instead
     */
    open fun augmentName(pairedSpell: ScepterAugment): MutableText {
        return AcText.translatable("$orCreateTranslationKey.combination",*provideArgs(pairedSpell))
    }

    /**
     * Use this method to provide unique names for certain spell combinations, not "{Noun} Bolt" or similar, but "Devastation" or something else suitably interesting
     */
    open fun specialName(otherSpell: ScepterAugment): MutableText {
        return AcText.empty()
    }

    /**
     * name provided when a spell is paired to itself. Shouldn't need to be overriden in most cases
     */
    open fun doubleName(): MutableText {
        return AcText.translatable("$orCreateTranslationKey.double")
    }

    /**
     * name provided when a spell is paired to itself. Shouldn't need to be overriden in most cases
     */
    open fun doubleNameDesc(): MutableText {
        return AcText.translatable("$orCreateTranslationKey.double.desc")
    }

    /**
     * Override this method to build the generally acceptable [augmentName]
     *
     * Typically, it will be some combination of [provideNoun], [provideAdjective], and [provideVerb]
     */
    open fun provideArgs(pairedSpell: ScepterAugment): Array<Text>{
        return arrayOf()
    }

    /**
     * provides a noun for use in a paired spell name. Something like "{Noun}blast"
     */
    open fun provideNoun(pairedSpell: ScepterAugment?): Text{
        return AcText.translatable(getTranslationKey() + ".noun")
    }
    /**
     * provides a verb for use in a paired spell name.
     */
    open fun provideVerb(pairedSpell: ScepterAugment?): Text{
        return AcText.translatable(getTranslationKey() + ".verb")
    }
    /**
     * provides an adjective for use in a paired spell name. Something like "{Adjective} Bolt"
     */
    open fun provideAdjective(pairedSpell: ScepterAugment?): Text{
        return AcText.translatable(getTranslationKey() + ".adjective")
    }

    open fun appendBaseDescription(description: MutableList<Text>, other: ScepterAugment, otherType: AugmentType){
        if (other == this){
            description.add(doubleNameDesc())
            return
        }
        appendDescription(description, other, otherType)
    }

    /**
     * This method is used to build the description that is shown when spells are paired.
     *
     * This method should be built up to provide chunks of description based on the actual changes that the pairing will be making to the original spell. Different changes should be provided separately. A change to the damage source would be one line, a change to the damage dealt another line.
     *
     * It takes the form of a list, with a format like so:
     *
     * "Original Spell Description"
     *
     * Changes:
     *
     * "Change Description 1"
     *
     * "Change Description 2"
     * etc.
     */
    abstract fun appendDescription(description: MutableList<Text>, other: ScepterAugment, otherType: AugmentType)

    /**
     * provides the maximum level the spell can reach
     */
    open fun getAugmentMaxLevel(): Int{
        return augmentData.maxLvl
    }
    override fun isAcceptableItem(stack: ItemStack): Boolean {
        return stack.isIn(getTag())
    }

    /**
     * provides the spell tier integer that the provided [ScepterTier] has. A scepter of this tier or higher will be needed to cast this spell
     *
     * Note: this is only used to provide visuals, the actual scepter suitability is determined by [getTag]
     */
    fun getTier(): Int{
        return tier.tier
    }

    /**
     * returns the item tag of scepters that this augment is suitable for.
     */
    fun getTag(): TagKey<Item>{
        return tier.tag
    }

    /**
     * Returns whether this spell should affect other players in consideration of ally status. For example, a healing spell with PvpMode shouldn't affect non-team-mates, and a damaging spell shouldn't damage other players if PvpMode is off.
     */
    fun getPvpMode(): Boolean{
        return augmentData.pvpMode
    }

    fun spellContext(context: ProcessContext = ProcessContext.EMPTY_CONTEXT): ProcessContext{
        return context.set(ProcessContext.SPELL,this.id)
    }

    fun spellFromContext(context: ProcessContext): ScepterAugment?{
        val id = context.get(ProcessContext.SPELL)
        return Registries.ENCHANTMENT.get(id) as? ScepterAugment
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
        val spellType = Identifier(AC.MOD_ID,"spell_context")
        val FAIL = SpellActionResult.fail()
        val SUCCESSFUL_PASS = SpellActionResult.pass()
    }


    fun interface DamageProviderFunction{
        fun provideDamageSource(dealer: LivingEntity, source: Entity?): DamageSource
    }
}
