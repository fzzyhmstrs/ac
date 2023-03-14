package me.fzzyhmstrs.amethyst_core.modifier_util

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.item_util.AbstractScepterItem
import me.fzzyhmstrs.amethyst_core.registry.ModifierRegistry
import me.fzzyhmstrs.fzzy_core.coding_util.AcText
import me.fzzyhmstrs.fzzy_core.config.FcConfig
import me.fzzyhmstrs.fzzy_core.nbt_util.Nbt
import me.fzzyhmstrs.amethyst_core.scepter_util.ScepterToolMaterial
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifier
import me.fzzyhmstrs.fzzy_core.modifier_util.AbstractModifierHelper
import me.fzzyhmstrs.fzzy_core.modifier_util.ModifierHelperType
import me.fzzyhmstrs.fzzy_core.nbt_util.NbtKeys
import net.minecraft.client.item.TooltipContext
import net.minecraft.enchantment.Enchantment
import net.minecraft.item.ItemStack
import net.minecraft.loot.context.LootContext
import net.minecraft.loot.context.LootContextTypes
import net.minecraft.loot.provider.number.BinomialLootNumberProvider
import net.minecraft.loot.provider.number.LootNumberProvider
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.server.world.ServerWorld
import net.minecraft.tag.TagKey
import net.minecraft.text.MutableText
import net.minecraft.text.Text
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier
import net.minecraft.util.registry.Registry

object ModifierHelper: AbstractModifierHelper<AugmentModifier>() {

    override val fallbackData: AbstractModifier.CompiledModifiers<AugmentModifier> = ModifierDefaults.BLANK_COMPILED_DATA

    private val DEFAULT_MODIFIER_TOLL = BinomialLootNumberProvider.create(25,0.24f)
    private val scepterAcceptableMap: MutableMap<Int,MutableList<ItemStack>> = mutableMapOf()

    fun addModifierForREI(modifier: Identifier, stack: ItemStack){
        val nbt = stack.orCreateNbt
        Nbt.makeItemStackId(stack)
        addModifierToNbt(modifier, nbt)
    }

    fun isInTag(id: Identifier,tag: TagKey<Enchantment>): Boolean{
        val augment = Registry.ENCHANTMENT.get(id)?:return false
        val opt = Registry.ENCHANTMENT.getEntry(Registry.ENCHANTMENT.getRawId(augment))
        var bl = false
        opt.ifPresent { entry -> bl = entry.isIn(tag) }
        return bl
    }

    fun createAugmentTag(path: String): TagKey<Enchantment> {
        return TagKey.of(Registry.ENCHANTMENT_KEY, Identifier(AC.MOD_ID,path))
    }

    private fun addUniqueModifier(modifier: Identifier, stack: ItemStack) {
        val nbt = stack.nbt
        if (nbt == null){
            addModifier(modifier, stack)
            return
        }
        val id = Nbt.getItemStackId(nbt)
        if (!(id != -1L && checkDescendant(modifier, stack) != null)) {
            addModifier(modifier, stack)
        }
    }

    fun rollScepterModifiers(stack: ItemStack, playerEntity: ServerPlayerEntity, world: ServerWorld, toll: LootNumberProvider = DEFAULT_MODIFIER_TOLL){
        val list = ModifierRegistry.modifierRollList
        val context = LootContext.Builder(world).random(world.random).luck(playerEntity.luck).build(LootContextTypes.EMPTY)
        var tollRemaining = (toll.nextFloat(context) + context.luck).toInt()
        while (tollRemaining > 0){
            val modChk = list[context.random.nextInt(list.size)]
            tollRemaining -= modChk.rollToll
            if (tollRemaining >= 0) {
                addUniqueModifier(modChk.modifierId, stack)
            }
        }
    }

    fun scepterAcceptableItemStacks(tier:Int): MutableList<ItemStack>{
        if (scepterAcceptableMap.containsKey(tier)){
            return scepterAcceptableMap[tier] ?: mutableListOf()
        } else {
            val entries = Registry.ITEM.indexedEntries
            val list: MutableList<ItemStack> = mutableListOf()
            for (entry in entries){
                val item = entry.value()
                if (item is AbstractScepterItem){
                    val material = item.material
                    if (material is ScepterToolMaterial){
                        if (material.scepterTier() >= tier){
                            list.add(ItemStack(item,1))
                        }
                    }
                }
            }
            scepterAcceptableMap[tier] = list
            return list
        }
    }

    override fun addModifierTooltip(stack: ItemStack, tooltip: MutableList<Text>, context: TooltipContext){
        val modifierList = getModifiers(stack)
        if (modifierList.isEmpty()) return
        if ((context.isAdvanced && FcConfig.flavors.showFlavorDescOnAdvanced) || FcConfig.flavors.showFlavorDesc){
            modifierList.forEach {
                val mod = getModifierByType(it)
                val text = mod?.getTranslation()?:AcText.translatable(getTranslationKeyFromIdentifier(it))
                val descText = mod?.getDescTranslation()?:AcText.translatable(getDescTranslationKeyFromIdentifier(it))
                tooltip.add(text.formatted(Formatting.GOLD)
                    .append(AcText.literal(" - ").formatted(Formatting.GOLD))
                    .append(
                        descText.formatted(Formatting.GOLD)
                            .formatted(Formatting.ITALIC)
                    ))
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

    override fun gatherActiveModifiers(stack: ItemStack){
        val nbt = stack.nbt
        if (nbt != null) {
            val id = Nbt.getItemStackId(nbt)
            if (!nbt.contains(NbtKeys.ACTIVE_ENCHANT.str())) return
            val activeEnchant = Identifier(nbt.getString(NbtKeys.ACTIVE_ENCHANT.str()))
            //println(getModifiers(stack))
            val compiled = gatherActiveAbstractModifiers(stack, activeEnchant, ModifierDefaults.BLANK_AUG_MOD.compiler())
            //println(compiled.modifiers)
            setModifiersById(
                id,
                compiled
            )
        }
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

    override fun getType(): ModifierHelperType {
        return ModifierRegistry.MODIFIER_TYPE
    }
}
