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
    ALTAR_KEY{
        override fun str(): String {
            return "altar_used"
        }
    },
    ALTAR_KEY_1{
        override fun str(): String {
            return "altar_used_1"
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
    FALLBACK_INIT{
        override fun str(): String {
            return "fallback_init"
        }
    },
    DISENCHANT_COUNT{
        override fun str(): String {
            return "disenchant_count"
        }
    },
    LOCK_POS{
        override fun str(): String {
            return "lock_pos"
        }
    },
    LOCKS{
        override fun str(): String {
            return "switch_locks"
        }
    },
    DOORS{
        override fun str(): String {
            return "switch_doors"
        }
    },
    DOOR_POS{
        override fun str(): String {
            return "door_pos"
        }
    },
    KEY_ITEM{
        override fun str(): String {
            return "key_item"
        }
    },
    HELD_ITEM{
        override fun str(): String {
            return "held_item"
        }
    },
    KEY_NUM{
        override fun str(): String {
            return "key_num"
        }
    },
    PORTAL_KEY{
        override fun str(): String {
            return "portal_key"
        }
    },
    FRAME_LIST{
        override fun str(): String {
            return "frame_list"
        }
    },
    FRAME_POS{
        override fun str(): String {
            return "frame_pos"
        }
    },
    PORTAL_LIST{
        override fun str(): String {
            return "portal_list"
        }
    },
    PORTAL_POS{
        override fun str(): String {
            return "portal_pos"
        }
    };

    abstract fun str(): String
}