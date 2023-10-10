package me.fzzyhmstrs.amethyst_core.scepter_util.data

import me.fzzyhmstrs.fzzy_config.config_util.ConfigSection
import me.fzzyhmstrs.fzzy_config.validated_field.ValidatedInt
import me.fzzyhmstrs.fzzy_core.coding_util.PerLvlI

class ValidatedPerLvlI(
    value: PerLvlI,
    max: PerLvlI = PerLvlI(Int.MAX_VALUE, Int.MAX_VALUE, Int.MAX_VALUE),
    min: PerLvlI = PerLvlI(Int.MIN_VALUE, Int.MIN_VALUE, Int.MIN_VALUE))
    :
    ConfigSection()
{

    private val storedPerLvlI by lazy {
        PerLvlI(base.get(),perLevel.get(),percent.get())
    }

    var base = ValidatedInt(value.base(),max.base(),min.base())
    var perLevel = ValidatedInt(value.perLevel(),max.perLevel(),min.perLevel())
    var percent = ValidatedInt(value.percent(),max.percent(), min.percent())

    fun get(): PerLvlI{
        return storedPerLvlI
    }

}