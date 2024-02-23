package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.scepter_util.augments.ScepterAugment
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import net.minecraft.util.Formatting
import net.minecraft.util.Identifier

@Suppress("unused", "MemberVisibilityCanBePrivate")
object RegisterTag {
    val FIRE_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"fire_augments"))
    val ICE_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"ice_augments"))
    val LIGHTNING_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"lightning_augments"))
    val ELEMENTAL_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"elemental_augments"))
    val ARCANE_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"arcane_augments"))
    val HEALER_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"healer_augments"))
    val EFFECTS_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"effects_augments"))
    val BUILDER_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"builder_augments"))
    val TRAVELER_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"traveler_augments"))
    val BOLT_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"bolt_augments"))
    val SOUL_AUGMENTS = FzzyPort.ENCHANTMENT.tagOf(Identifier(AC.MOD_ID,"soul_augments"))

    val TIER_1_SPELL_SCEPTERS = FzzyPort.ITEM.tagOf(Identifier(AC.MOD_ID,"tier_one_spell_scepters"))
    val TIER_2_SPELL_SCEPTERS = FzzyPort.ITEM.tagOf(Identifier(AC.MOD_ID,"tier_two_spell_scepters"))
    val TIER_3_SPELL_SCEPTERS = FzzyPort.ITEM.tagOf(Identifier(AC.MOD_ID,"tier_three_spell_scepters"))

    fun registerAll(){}

    fun getStylesFromSpell(spell: ScepterAugment): Set<TagStyle>{
        val set: MutableSet<TagStyle> = mutableSetOf()
        for (style in TagStyle.values()){
            if (style == TagStyle.EMPTY) continue
            if (style.isInTag(spell)) set.add(style)
        }
        if (set.isEmpty()) return setOf(TagStyle.EMPTY)
        return set
    }

    fun getStyleFromSpell(spell: ScepterAugment): TagStyle{
        for (style in TagStyle.values()){
            if (style.isInTag(spell)) return style
        }
        return TagStyle.EMPTY
    }

    enum class TagStyle(val x: Int, val y: Int, val color: Formatting){
        LIGHTNING(177,121,Formatting.GOLD){
            override fun isInTag(spell: ScepterAugment): Boolean {
                return FzzyPort.ENCHANTMENT.isInTag(spell, LIGHTNING_AUGMENTS)
            }
        },
        ICE(177,81,Formatting.BLUE){
            override fun isInTag(spell: ScepterAugment): Boolean {
                return FzzyPort.ENCHANTMENT.isInTag(spell, ICE_AUGMENTS)
            }
        },
        FIRE(177,41,Formatting.RED){
            override fun isInTag(spell: ScepterAugment): Boolean {
                return FzzyPort.ENCHANTMENT.isInTag(spell, FIRE_AUGMENTS)
            }
        },
        HEALER(217,41,Formatting.YELLOW){
            override fun isInTag(spell: ScepterAugment): Boolean {
                return FzzyPort.ENCHANTMENT.isInTag(spell, HEALER_AUGMENTS)
            }
        },
        TRAVELER(217,1,Formatting.GRAY){
            override fun isInTag(spell: ScepterAugment): Boolean {
                return FzzyPort.ENCHANTMENT.isInTag(spell, TRAVELER_AUGMENTS)
            }
        },
        ARCANE(217,81,Formatting.LIGHT_PURPLE){
            override fun isInTag(spell: ScepterAugment): Boolean {
                return FzzyPort.ENCHANTMENT.isInTag(spell, ARCANE_AUGMENTS)
            }
        },
        BUILDER(177,1,Formatting.GRAY){
            override fun isInTag(spell: ScepterAugment): Boolean {
                return FzzyPort.ENCHANTMENT.isInTag(spell,BUILDER_AUGMENTS)
            }
        },
        SOUL(217,121,Formatting.DARK_AQUA){
            override fun isInTag(spell: ScepterAugment): Boolean {
                return FzzyPort.ENCHANTMENT.isInTag(spell, SOUL_AUGMENTS)
            }
        },
        EMPTY(137,1,Formatting.GRAY){
            override fun isInTag(spell: ScepterAugment): Boolean {
                return true
            }
        };
        abstract fun isInTag(spell: ScepterAugment): Boolean
    }
}
