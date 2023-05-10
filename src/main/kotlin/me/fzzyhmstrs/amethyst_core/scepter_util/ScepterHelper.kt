@file:Suppress("REDUNDANT_ELSE_IN_WHEN")

package me.fzzyhmstrs.amethyst_core.scepter_util

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.event.ModifyModifiersEvent
import me.fzzyhmstrs.amethyst_core.event.ModifySpellEvent
import me.fzzyhmstrs.amethyst_core.item_util.AbstractScepterItem
import me.fzzyhmstrs.amethyst_core.item_util.AugmentScepterItem
import me.fzzyhmstrs.amethyst_core.item_util.SpellCasting
import me.fzzyhmstrs.amethyst_core.modifier_util.AugmentModifier
import me.fzzyhmstrs.amethyst_core.modifier_util.ModifierHelper
import me.fzzyhmstrs.amethyst_core.modifier_util.XpModifiers
import me.fzzyhmstrs.amethyst_core.registry.RegisterAttribute
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.*
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import me.fzzyhmstrs.fzzy_core.nbt_util.NbtKeys
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.PacketSender
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.advancement.criterion.Criteria
import net.minecraft.advancement.criterion.TickCriterion
import net.minecraft.enchantment.EnchantmentHelper
import net.minecraft.enchantment.Enchantments
import net.minecraft.entity.LivingEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.ItemStack
import net.minecraft.nbt.NbtCompound
import net.minecraft.network.PacketByteBuf
import net.minecraft.registry.Registries
import net.minecraft.server.MinecraftServer
import net.minecraft.server.network.ServerPlayNetworkHandler
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.Identifier
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import kotlin.math.max

/**
 * Helper object for dealing with Augment-type scepters. methods for using a scepter, working with the scepter stat system, and checking and resetting cooldowns
 *
 * Look into the AugmentScepterItem and DefaultScepterItem for implementation examples.
 */
object ScepterHelper {

    private val SCEPTER_SYNC_PACKET = Identifier(AC.MOD_ID,"scepter_sync_packet")
    val CAST_SPELL = SpellCriterion(Identifier(AC.MOD_ID,"cast_spell"))
    val USED_KNOWLEDGE_BOOK = TickCriterion(Identifier(AC.MOD_ID,"used_knowledge_book"))

    fun useScepter(activeEnchantId: String, pairedAugments: PairedAugments, stack: ItemStack, world: World, level: Int, modifiers: AbstractModifier.CompiledModifiers<AugmentModifier>, user: LivingEntity, checkEnchant: Boolean = true): Int?{
        if (world !is ServerWorld){return null}
        val scepterNbt = stack.orCreateNbt
        if (checkEnchant) {
            if (EnchantmentHelper.getLevel(activeEnchant, stack) == 0) {
                fixActiveEnchantWhenMissing(stack)
                return null
            }
        }
        //cooldown modifier is a percentage modifier, so 20% will boost cooldown by 20%. -20% will take away 20% cooldown
        val cdMod = modifiers.compiledData.cooldownModifier
        val cooldown = getEffectiveCooldown(pairedAugments,cdMod,level,user)
        val time = world.time

        val lastUsedList = Nbt.getOrCreateSubCompound(scepterNbt, NbtKeys.LAST_USED_LIST.str())
        val lastUsed = checkLastUsed(lastUsedList,activeEnchantId,time-1000000L)
        val cooldown2 = max(cooldown,1) // don't let cooldown be less than 1 tick
        return if (time - cooldown2 >= lastUsed){ //checks that enough time has passed since last usage
            updateLastUsed(lastUsedList, activeEnchantId, time)
            cooldown2
        } else {
            null
        }
    }

    fun castSpell(
        world: World,
        user: LivingEntity,
        hand: Hand,
        stack: ItemStack,
        spell: ScepterAugment,
        pairedSpell: ScepterAugment?,
        activeEnchantId: String,
        testLevel: Int,
        spellCaster: SpellCasting,
        incrementStats: Boolean = true,
        checkEnchant: Boolean = true): TypedActionResult<ItemStack>{
        val pairedAugments = PairedAugments(spell, pairedSpell)
        // Modify Modifiers event fires here //
        val modifiers = ModifyModifiersEvent.EVENT.invoker().modifyModifiers(world, user, stack, ModifierHelper.getActiveModifiers(stack))
        // Modify Spell Event fires here //
        val level = getEffectiveLevel(testLevel,modifiers.compiledData.levelModifier,user)
        val result = ModifySpellEvent.EVENT.invoker().modifySpell(spell,world,user,hand,modifiers)
        if (result == ActionResult.CONSUME) {
            useScepter(
                activeEnchantId,
                pairedAugments,
                stack,
                world,
                level,
                modifiers,
                user,
                checkEnchant
            )
            return TypedActionResult.success(stack)
        }
        else if (result == ActionResult.FAIL) {
            return spellCaster.resetCooldown(stack,world,user,activeEnchantId)
        }

        val cooldown : Int? = useScepter(
            activeEnchantId,
            pairedAugments,
            stack,
            world,
            level,
            modifiers,
            user,
            checkEnchant
        )
        return if (cooldown != null) {
            val manaCost = pairedAugments.provideManaCost(((modifiers.compiledData.manaCostModifier + 100.0)/100.0)  * user.getAttributeValue(RegisterAttribute.SPELL_MANA_COST))
            //val manaCost = AugmentHelper.getAugmentManaCost(activeEnchantId,((modifiers.compiledData.manaCostModifier + 100.0)/100.0)  * user.getAttributeValue(RegisterAttribute.SPELL_MANA_COST))
            if (!spellCaster.checkManaCost(manaCost,stack, world, user)) return spellCaster.resetCooldown(stack,world,user,activeEnchantId)
            if (spell.applyModifiableTasks(world, user, hand, level, modifiers.modifiers, modifiers.compiledData)) {
                spellCaster.applyManaCost(manaCost,stack, world, user)
                if (incrementStats) {
                    incrementScepterStats(
                        stack.orCreateNbt,
                        stack,
                        activeEnchantId,
                        user,
                        modifiers.compiledData.getXpModifiers()
                    )
                }
                if (user is PlayerEntity) {
                    user.itemCooldownManager.set(stack.item, cooldown)
                }
                if (user is ServerPlayerEntity) {
                    CAST_SPELL.trigger(user, Identifier(activeEnchantId))
                }
                TypedActionResult.success(stack)
            } else {
                spellCaster.resetCooldown(stack,world,user,activeEnchantId)
            }
        } else {
            spellCaster.resetCooldown(stack,world,user,activeEnchantId)
        }
    }

    fun sendScepterUpdateFromClient(up: Boolean) {
        val buf = PacketByteBufs.create()
        buf.writeBoolean(up)
        ClientPlayNetworking.send(SCEPTER_SYNC_PACKET,buf)
    }

    fun registerServer() {
        Criteria.register(CAST_SPELL)
        Criteria.register(USED_KNOWLEDGE_BOOK)
        ServerPlayNetworking.registerGlobalReceiver(SCEPTER_SYNC_PACKET)
        { server: MinecraftServer,
          serverPlayerEntity: ServerPlayerEntity,
          _: ServerPlayNetworkHandler,
          packetByteBuf: PacketByteBuf,
          _: PacketSender ->
            val stack = serverPlayerEntity.getStackInHand(Hand.MAIN_HAND)
            val up = packetByteBuf.readBoolean()
            server.execute {
                updateScepterActiveEnchant(stack, serverPlayerEntity, up)
            }
        }
    }

    private fun updateScepterActiveEnchant(stack: ItemStack, user: PlayerEntity, up: Boolean){
        val item = stack.item
        if (item !is AbstractScepterItem) return
        val nbt = stack.orCreateNbt

        if (!nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())){
            item.initializeScepter(stack, nbt)
        }

        if (!stack.hasEnchantments()) {
            nbt.putString(NbtKeys.ACTIVE_ENCHANT.str(),"none")
            return
        }

        val activeEnchantCheckId = nbt.getString(NbtKeys.ACTIVE_ENCHANT.str())

        val activeEnchant = Registries.ENCHANTMENT.get(Identifier(activeEnchantCheckId))
        val activeEnchantId = if (activeEnchant != null) {
            if (EnchantmentHelper.getLevel(activeEnchant, stack) == 0) {
                fixActiveEnchantWhenMissing(stack)
                nbt.getString(NbtKeys.ACTIVE_ENCHANT.str())
            } else {
                activeEnchantCheckId
            }
        } else {
            fixActiveEnchantWhenMissing(stack)
            nbt.getString(NbtKeys.ACTIVE_ENCHANT.str())
        }

        val nbtEls = stack.enchantments
        var matchIndex = 0
        val augIndexes: MutableList<Int> = mutableListOf()
        for (i in 0..nbtEls.lastIndex){
            val identifier = EnchantmentHelper.getIdFromNbt(nbtEls[i] as NbtCompound)
            val enchantCheck = Registries.ENCHANTMENT.get(identifier)?: Enchantments.VANISHING_CURSE
            if(enchantCheck is ScepterAugment) {
                augIndexes.add(i)
            }
            if (activeEnchantId == identifier?.toString()){
                matchIndex = i
            }
        }
        val newIndex = if (augIndexes.size != 0) {
            val augElIndex = if (augIndexes.indexOf(matchIndex) == -1){
                0
            } else {
                augIndexes.indexOf(matchIndex)
            }
            if (up) {
                augIndexes.getOrElse(augElIndex + 1) { 0 }
            } else {
                augIndexes.getOrElse(augElIndex - 1) { augIndexes[augIndexes.lastIndex] }
            }
        } else {
            0
        }
        val nbtTemp = nbtEls[newIndex] as NbtCompound
        val newActiveEnchantId = EnchantmentHelper.getIdFromNbt(nbtTemp)?.toString()?:return
        val newActiveEnchant = Registries.ENCHANTMENT.get(Identifier(newActiveEnchantId))?:return
        if (newActiveEnchant !is ScepterAugment) return

        val lastUsedList = Nbt.getOrCreateSubCompound(nbt, NbtKeys.LAST_USED_LIST.str())
        val timeSinceLast = user.world.time - checkLastUsed(lastUsedList,newActiveEnchantId,user.world.time-1000000L)
        val modifiers = ModifierHelper.getActiveModifiers(stack)
        val level = getEffectiveLevel(getTestLevel(nbt,newActiveEnchantId, newActiveEnchant), modifiers.compiledData.levelModifier,user)
        val pairedSpellId = getPairedSpell(nbtTemp)?.id?.toString()
        val cooldown = getEffectiveCooldown(newActiveEnchantId,pairedSpellId, modifiers.compiledData.cooldownModifier,level,user)
        if(timeSinceLast >= cooldown){
            user.itemCooldownManager.remove(stack.item)
        } else{
            user.itemCooldownManager.set(stack.item, (cooldown - timeSinceLast).toInt())
        }
        nbt.putString(NbtKeys.ACTIVE_ENCHANT.str(),newActiveEnchantId)
        ModifierHelper.gatherActiveModifiers(stack)
        val name = newActiveEnchant.getName(1) //?: AcText.translatable("enchantment.${Identifier(newActiveEnchant).namespace}.${Identifier(newActiveEnchant).path}")
        val message = AcText.translatable("scepter.new_active_spell").append(name)
        user.sendMessage(message,false)
    }

    private fun fixActiveEnchantWhenMissing(stack: ItemStack) {
        val nbt = stack.orCreateNbt
        val item = stack.item
        if (item is AugmentScepterItem) {
            val newEnchant = EnchantmentHelper.get(stack).keys.firstOrNull()
            val identifier = if (newEnchant != null) {
                Registries.ENCHANTMENT.getId(newEnchant)
            } else {
                item.addDefaultEnchantments(stack, nbt)
                item.fallbackId
            }
            if (identifier != null) {
                nbt.putString(NbtKeys.ACTIVE_ENCHANT.str(), identifier.toString())
            }
            item.initializeScepter(stack, nbt)
        }
    }

    fun getTestLevel(nbt: NbtCompound, activeEnchantId: String, testEnchant: ScepterAugment): Int{
        val level = getScepterStat(nbt,activeEnchantId).first
        val minLvl = AugmentHelper.getAugmentMinLvl(activeEnchantId)
        val maxLevel = (testEnchant.getAugmentMaxLevel()) + minLvl - 1
        var testLevel = 1
        if (level >= minLvl){
            testLevel = level
            if (testLevel > maxLevel) testLevel = maxLevel
            testLevel -= (minLvl - 1)
        }
        return testLevel
    }

    fun getEffectiveLevel(testLevel: Int, levelModifier: Int, user: LivingEntity): Int{
        return max(1,((testLevel + levelModifier) * user.getAttributeValue(RegisterAttribute.SPELL_LEVEL)).toInt())
    }

    fun getPairedSpell(nbt: NbtCompound): ScepterAugment?{
        val str = nbt.getString(me.fzzyhmstrs.amethyst_core.nbt_util.NbtKeys.PAIRED_ENCHANT.str())
        if (str.isEmpty()) return null
        val spell = Registries.ENCHANTMENT.get(Identifier(str))
        if (spell !is ScepterAugment) return null
        return spell
    }

    fun incrementScepterStats(scepterNbt: NbtCompound, scepter: ItemStack, activeEnchantId: String, user: LivingEntity, xpMods: XpModifiers? = null){
        val spellKey = AugmentHelper.getAugmentType(activeEnchantId).name
        if(spellKey == SpellType.NULL.name) return
        val statLvl = scepterNbt.getInt(spellKey + "_lvl")
        val statMod = xpMods?.getMod(spellKey) ?: 0
        val statMod2 = AugmentHelper.getAugmentCastXp(activeEnchantId)
        val statXp = (scepterNbt.getInt(spellKey + "_xp") + ((statMod + statMod2) * user.getAttributeValue(RegisterAttribute.SPELL_EXPERIENCE))).toInt()
        scepterNbt.putInt(spellKey + "_xp",statXp)
        val lvlUp = checkXpForLevelUp(statXp,statLvl)
        if(lvlUp > 0){
            scepterNbt.putInt(spellKey + "_lvl",statLvl + lvlUp)
            updateScepterAugments(scepter, scepterNbt)
        }
    }
    
    private fun checkXpForLevelUp(xp:Int, lvl:Int): Int{
        var lvlUp = 0
        while (xp > calcXp(lvl + lvlUp)){
            lvlUp++
        }
        return lvlUp
    }

    private fun calcXp(lvl: Int): Int{
        return if (lvl < 11){
            (2 * lvl * lvl) + (40 * lvl)
        } else if (lvl < 21){
            (3 * lvl * lvl) + (24 * lvl) + 57
        } else {
            ((4.5 * lvl * lvl) - (35.5 * lvl) + 648).toInt()
        }
    }
    
    private fun updateScepterAugments(scepter: ItemStack, scepterNbt: NbtCompound){
        val enchantMap = EnchantmentHelper.get(scepter)
        for (e in enchantMap){
            val aug = e.key
            if (aug is ScepterAugment){
                val l = e.value
                val maxL = aug.getAugmentMaxLevel()
                if (l >= maxL) continue
                val augId = Registries.ENCHANTMENT.getId(aug)?:continue
                val scepterL = getScepterStat(scepterNbt,augId.toString()).first
                val newAugL = AugmentHelper.getAugmentCurrentLevel(scepterL,augId, aug)
                enchantMap[aug] = newAugL
            }
        }
        EnchantmentHelper.set(enchantMap,scepter)
    }

    fun getScepterStat(scepterNbt: NbtCompound, activeEnchantId: String): Pair<Int,Int>{
        val spellKey = AugmentHelper.getAugmentType(activeEnchantId).name
        if (!scepterNbt.contains(spellKey + "_lvl")) getStatsHelper(scepterNbt)
        val statLvl = scepterNbt.getInt(spellKey + "_lvl")
        val statXp = scepterNbt.getInt(spellKey + "_xp")
        return Pair(statLvl,statXp)
    }

    fun getScepterStats(stack: ItemStack): IntArray {
        val nbt = stack.orCreateNbt
        return getStatsHelper(nbt)
    }

    /**
     * library method to check if an augment scepter meets the level requirements of the given augment
     *
     * @see AugmentDatapoint needs to be defined in the augment class
     */
    fun isAcceptableScepterItem(augment: ScepterAugment, stack: ItemStack, player: PlayerEntity): Boolean {
        val nbt = stack.orCreateNbt
        if (player.abilities.creativeMode) return true
        val activeEnchantId = Registries.ENCHANTMENT.getId(augment)?.toString() ?: ""
        if (!AugmentHelper.checkAugmentStat(activeEnchantId)) return false
        val minLvl = AugmentHelper.getAugmentMinLvl(activeEnchantId)
        val curLvl = getScepterStat(nbt,activeEnchantId).first
        return (curLvl >= minLvl)

    }

    fun resetCooldown(world: World, stack: ItemStack, user:LivingEntity, activeEnchantId: String, givenLevel: Int = 0){
        val nbt = stack.nbt?: return
        val testEnchant = Registries.ENCHANTMENT.get(Identifier(activeEnchantId))?:return
        if (testEnchant !is ScepterAugment) return
        val lastUsedList = Nbt.getOrCreateSubCompound(nbt, NbtKeys.LAST_USED_LIST.str())
        val modifiers = ModifierHelper.getActiveModifiers(stack)
        val testLevel = if (givenLevel == 0) getTestLevel(nbt,activeEnchantId, testEnchant) else givenLevel
        val level = max(1,((testLevel + modifiers.compiledData.levelModifier) * user.getAttributeValue(RegisterAttribute.SPELL_LEVEL)).toInt())
        val cd = AugmentHelper.getAugmentCooldown(activeEnchantId).value(level)
        val currentLastUsed = checkLastUsed(lastUsedList,activeEnchantId, world.time)
        updateLastUsed(lastUsedList,activeEnchantId,currentLastUsed - cd - 2)
    }

    fun getEffectiveCooldown(activeEnchantId: String, pairedEnchantId: String?, cooldownModifier: Double, level: Int, user: LivingEntity): Int{
        val cooldown = if (pairedEnchantId != null){
            val mod = AugmentHelper.getAugmentModificationInfo(pairedEnchantId)
            val blah = when (mod.cooldownType) {
                ModificationType.REPLACE -> {
                    AugmentHelper.getAugmentCooldown(pairedEnchantId).value(level)
                }
                ModificationType.DEFER -> {
                    AugmentHelper.getAugmentCooldown(activeEnchantId).value(level)
                }
                else -> {
                    PerLvlI(AugmentHelper.getAugmentCooldown(pairedEnchantId).value(level)).plus(mod.cooldownModifier).value(0)
                }
            }
            blah
        } else {
            AugmentHelper.getAugmentCooldown(activeEnchantId).value(level)
        }
        return (cooldown * ((cooldownModifier + 100.0)/100.0) * user.getAttributeValue(RegisterAttribute.SPELL_COOLDOWN)).toInt()
    }

    fun checkLastUsed(lastUsedList: NbtCompound, activeEnchantId: String, time: Long): Long{
        val key = activeEnchantId + NbtKeys.LAST_USED.str()
        return if (!lastUsedList.contains(key)) {
            lastUsedList.putLong(key, time)
            time
        } else {
            lastUsedList.getLong(key)
        }
    }
    fun updateLastUsed(lastUsedList: NbtCompound, activeEnchantId: String, currentTime: Long){
        val key = activeEnchantId + NbtKeys.LAST_USED.str()
        lastUsedList.putLong(key, currentTime)

    }

    fun xpToNextLevel(xp: Int,lvl: Int): Int{
        val xpNext = calcXp(lvl)
        return (xpNext - xp + 1)
    }

    private fun getStatsHelper(nbt: NbtCompound): IntArray{
        val stats = intArrayOf(0,0,0,0,0,0)
        if(!nbt.contains("FURY_lvl")){
            nbt.putInt("FURY_lvl",1)
        }
        stats[0] = nbt.getInt("FURY_lvl")
        if(!nbt.contains("GRACE_lvl")){
            nbt.putInt("GRACE_lvl",1)
        }
        stats[1] = nbt.getInt("GRACE_lvl")
        if(!nbt.contains("WIT_lvl")){
            nbt.putInt("WIT_lvl",1)
        }
        stats[2] = nbt.getInt("WIT_lvl")
        if(!nbt.contains("FURY_xp")){
            nbt.putInt("FURY_xp",0)
        }
        stats[3] = nbt.getInt("FURY_xp")
        if(!nbt.contains("GRACE_xp")){
            nbt.putInt("GRACE_xp",0)
        }
        stats[4] = nbt.getInt("GRACE_xp")
        if(!nbt.contains("WIT_xp")){
            nbt.putInt("WIT_xp",0)
        }
        stats[5] = nbt.getInt("WIT_xp")
        return stats
    }

}
