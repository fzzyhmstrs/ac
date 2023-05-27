package me.fzzyhmstrs.amethyst_core.nbt_util

/**
 * various Nbt keys used by Amethyst Imbuement and other Amethyst mods. Use at your own risk!
 */
enum class NbtKeys {

    TOTEM{
        override fun str(): String {
            return "totem_active"
        }
    },
    ANGELIC{
        override fun str(): String {
            return "angelic"
        }
    },
    MAX_XP{
        override fun str(): String {
            return "max_xp"
        }
    },
    MAX_XP_1{
        override fun str(): String {
            return "max_xp_1"
        }
    },
    SCEPTER_ID{
        override fun str(): String {
            return "scepter_id"
        }
    },
    LORE_KEY{
        override fun str(): String {
            return "book_of_lore_augment"
        }
    }
    ,
    LORE_TYPE{
        override fun str(): String {
            return "lore_type"
        }
    },
    ENCHANT_INIT{
        override fun str(): String {
            return "enchant_init_"
        }
    },
    DISENCHANT_COUNT{
        override fun str(): String {
            return "disenchant_count"
        }
    },
    PAIRED_ENCHANTS{
        override fun str(): String {
            return "paired_enchants"
        }
    },
    PAIRED_ENCHANT{
        override fun str(): String {
            return "paired_enchant_id"
        }
    },
    PAIRED_BOOST{
        override fun str(): String {
            return "paired_helper_id"
        }
    };

    abstract fun str(): String
}