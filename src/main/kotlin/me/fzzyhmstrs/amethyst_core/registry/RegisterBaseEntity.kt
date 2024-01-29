package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.AC
import me.fzzyhmstrs.amethyst_core.entity_util.MissileEntity
import me.fzzyhmstrs.fzzy_core.coding_util.FzzyPort
import net.fabricmc.fabric.api.`object`.builder.v1.entity.FabricEntityTypeBuilder
import net.minecraft.entity.EntityDimensions
import net.minecraft.entity.EntityType
import net.minecraft.entity.SpawnGroup
import net.minecraft.util.Identifier
import net.minecraft.world.World

object RegisterBaseEntity {

    val MISSILE_ENTITY: EntityType<MissileEntity> = FzzyPort.ENTITY_TYPE.register(
        Identifier(AC.MOD_ID, "missile_entity"),
        FabricEntityTypeBuilder.create(
            SpawnGroup.MISC
        ) { entityType: EntityType<MissileEntity>, world: World ->
            MissileEntity(
                entityType,
                world
            )
        }.dimensions(EntityDimensions.fixed(0.3125f, 0.3125f)).build()
    )

    internal fun registerAll(){}

}
