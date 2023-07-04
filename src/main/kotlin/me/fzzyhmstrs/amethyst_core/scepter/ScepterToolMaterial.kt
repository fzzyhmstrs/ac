package me.fzzyhmstrs.amethyst_core.scepter

import me.fzzyhmstrs.fzzy_core.mana_util.ManaItemMaterial
import net.minecraft.item.ToolMaterial

/**
 * A tool material for defining a scepter. The Scepter tier is used to determine which augments can be applied to which scepters. See [LoreTier] for more info.
 */
abstract class ScepterToolMaterial: ToolMaterial, ManaItemMaterial {
   
  abstract fun scepterTier(): Int

  abstract fun getAttackSpeed(): Double
  
}
