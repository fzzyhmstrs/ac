package me.fzzyhmstrs.amethyst_core.registry

import me.fzzyhmstrs.amethyst_core.entity.MissileEntityRenderer
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.fabricmc.fabric.api.client.rendering.v1.EntityRendererRegistry
import net.minecraft.client.render.entity.EntityRendererFactory

@Environment(value = EnvType.CLIENT)
object RegisterBaseRenderer {

    internal fun registerAll(){
        EntityRendererRegistry.register(
            RegisterBaseEntity.MISSILE_ENTITY
        ){context: EntityRendererFactory.Context ->
            MissileEntityRenderer(
                context
            )
        }
    }

}