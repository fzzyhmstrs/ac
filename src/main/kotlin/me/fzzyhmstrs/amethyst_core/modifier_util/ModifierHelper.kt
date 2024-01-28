package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.item_util.ScepterLike
import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import me.fzzyhmstrs.fzzy_core.config.FcConfig
import me.fzzyhmstrs.fzzy_core.interfaces.Modifiable
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifierHelper
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType
import net.minecraft.client.item.TooltipContext
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextParameterSet
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.loot.provider.number.BinomialLootNumberProvider
import net.minecraft.loot.provider.number.LootNumberProvider
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

@Suppress("unused")
object ModifierHelper: AbstractModifierHelper<AugmentModifier>() {

    override val fallbackData: AbstractModifier.CompiledModifiers<AugmentModifier> = ModifierDefaults.BLANK_COMPILED_DATA

    private val DEFAULT_MODIFIER_TOLL = BinomialLootNumberProvider.create(25,0.24f)
    private val scepterAcceptableMap: MutableMap<Int,MutableList<ItemStack>> = mutableMapOf()

    fun addModifierForREI(modifier: Identifier, stack: ItemStack){
        val nbt = stack.orCreateNbt
        addModifierToNbt(stack, modifier, nbt)
    }

    fun getRandomRollableModifier(): Identifier{
        return ModifierRegistry.modifierRollList.random().modifierId
    }

    fun rollScepterModifiers(@Suppress("UNUSED_PARAMETER") stack: ItemStack, playerEntity: ServerPlayerEntity, world: ServerWorld, toll: LootNumberProvider = DEFAULT_MODIFIER_TOLL): List<Identifier>{
        val list = ModifierRegistry.modifierRollList
        val parameters = LootContextParameterSet.Builder(world).luck(playerEntity.luck).build(LootContextTypes.EMPTY)
        val contextBuilder = LootContext.Builder(parameters).random(AC.acRandom.nextLong())
        val context = contextBuilder.build(null)
        val result: MutableList<Identifier> = mutableListOf()
        do{
            var tollRemaining = (toll.nextFloat(context) + context.luck).toInt()
            while (tollRemaining > 0){
                val modChk = list[context.random.nextInt(list.size)]
                tollRemaining -= modChk.rollToll
                if (tollRemaining >= 0) {
                    result.add(modChk.modifierId)
                }
            }
        } while (result.isEmpty())
        return result
    }
        
    fun addRolledModifiers(stack: ItemStack, mods: List<Identifier>) {
        for (mod in mods){
            addModifier(mod, stack)
        }
    }

    fun removeRolledModifiers(stack: ItemStack, mods: List<Identifier>){
        for (mod in mods){
            removeModifier(stack,mod)
        }
    }

    fun scepterAcceptableItemStacks(tier:Int): MutableList<ItemStack>{
        return if (scepterAcceptableMap.containsKey(tier)){
            scepterAcceptableMap[tier] ?: mutableListOf()
        } else {
            val list: MutableList<ItemStack> = mutableListOf()
            for (item in FzzyPort.ITEM){
                if (item is ScepterLike){
                    if (item.getTier() >= tier){
                        list.add(ItemStack(item,1))
                    }
                }
            }
            scepterAcceptableMap[tier] = list
            list
        }
    }

    override fun addModifierTooltip(stack: ItemStack, tooltip: MutableList<Text>, context: TooltipContext){
        val modifierList = getModifiersFromNbt(stack)
        if (modifierList.isEmpty()) return
        addModifierTooltip(modifierList, tooltip, context)
    }

    fun addModifierTooltip(modifierList: List<Identifier>,tooltip: MutableList<Text>, context: TooltipContext){
        if ((context.isAdvanced && FcConfig.flavors.showFlavorDescOnAdvanced) || FcConfig.flavors.showFlavorDesc){
            modifierList.forEach {
                val mod = getModifierByType(it)
                if (mod != null) {
                    val text = mod.getTranslation()
                    val descText =
                        mod.getDescTranslation()
                    tooltip.add(
                        text.formatted(Formatting.GOLD)
                            .append(AcText.literal(" - ").formatted(Formatting.GOLD))
                            .append(
                                descText.formatted(Formatting.GOLD)
                                    .formatted(Formatting.ITALIC)
                            )
                    )
                }
            }
            return
        }
        val commaText: MutableText = AcText.literal(", ").formatted(Formatting.GOLD)
        val modifierText = AcText.translatable("modifiers.base_text").formatted(Formatting.GOLD)
        val itr = modifierList.asIterable().iterator()
        while(itr.hasNext()){
            val id = itr.next()
            val mod = getModifierByType(id)
            val text = mod?.getTranslation()?:AcText.translatable(getTranslationKeyFromIdentifier(id))
            modifierText.append(text.formatted(Formatting.GOLD))
            if (itr.hasNext()){
                modifierText.append(commaText)
            }
        }
        tooltip.add(modifierText)
    }

    override fun initializeModifiers(stack: ItemStack): List<Identifier> {
        val item = stack.item
        val list = if(item is Modifiable){
            item.defaultModifiers(getType())
        } else {
            listOf()
        }
        val nbt = stack.orCreateNbt
        if (list.isNotEmpty()){
            if (!nbt.contains(getType().getModifierInitKey() + stack.translationKey)){
                for (mod in list) {
                    val mods = getModifiersFromNbt(nbt)
                    val realMod = getModifierByType(mod) ?: continue
                    var descendant: AbstractModifier<AugmentModifier> = realMod
                    var highestDescendant: AbstractModifier<AugmentModifier>? = null
                    do {
                        if (mods.contains(descendant.modifierId))
                            highestDescendant = descendant
                        descendant = descendant.getDescendant() ?: continue
                    }  while (descendant.hasDescendant())
                    if (mods.contains(descendant.modifierId))
                        highestDescendant = descendant
                    if (highestDescendant == null){
                        highestDescendant = realMod
                    }
                    if (!highestDescendant.hasDescendant() && mods.contains(highestDescendant.modifierId)) {
                        continue
                    }else{
                        if (highestDescendant.hasDescendant() && mods.contains(highestDescendant.modifierId)) {
                            removeModifierWithoutCheck(stack, highestDescendant.modifierId, nbt)
                            val newDescendant = highestDescendant.getDescendant() ?: continue
                            addModifierToNbt(stack, newDescendant.modifierId, nbt)
                        } else {
                            addModifierToNbt(stack, highestDescendant.modifierId, nbt)
                        }
                    }
                }
                nbt.putBoolean(getType().getModifierInitKey() + stack.translationKey,true)
            }
        }
        return getModifiersFromNbt(stack)
    }

    /*fun gatherActiveModifiers(stack: ItemStack, activeEnchant: Identifier){
        val nbt = stack.nbt
        if (nbt != null) {
            val id = Nbt.getItemStackId(nbt)
            //println(getModifiers(stack))
            val compiled = gatherActiveAbstractModifiers(stack, activeEnchant, ModifierDefaults.BLANK_AUG_MOD.compiler())
            //println(compiled.modifiers)
            setModifiersById(
                id,
                compiled
            )
        }
    }*/

    /*override fun gatherActiveModifiers(stack: ItemStack){
        val nbt = stack.nbt
        if (nbt != null) {
            val id = Nbt.getItemStackId(nbt)
            val activeEnchant = if (!nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())) ModifierDefaults.BLANK_ID else Identifier(nbt.getString(NbtKeys.ACTIVE_ENCHANT.str()))
            //println(getModifiers(stack))
            val compiled = gatherActiveAbstractModifiers(stack, activeEnchant, ModifierDefaults.BLANK_AUG_MOD.compiler())
            //println(compiled.modifiers)
            setModifiersById(
                id,
                compiled
            )
        }
    }*/

    override fun compiler(): AbstractModifier<AugmentModifier>.Compiler{
        return ModifierDefaults.BLANK_AUG_MOD.compiler()
    }

    override fun getTranslationKeyFromIdentifier(id: Identifier): String {
        return "scepter.modifier.${id}"
    }
    
    override fun getDescTranslationKeyFromIdentifier(id: Identifier): String {
        return "scepter.modifier.${id}.desc"
    }

    override fun getModifierByType(id: Identifier): AugmentModifier? {
        return me.fzzyhmstrs.fzzy_core.registry.ModifierRegistry.getByType<AugmentModifier>(id)
    }

    override fun getType(): ModifierHelperType<AugmentModifier> {
        return ModifierRegistry.MODIFIER_TYPE
    }
}
