package me.fzzyhmstrs.amethyst_core.entity_util

import me.fzzyhmstrs.fzzy_core.coding_util.compat.FzzyRotation
import net.minecraft.client.render.OverlayTexture
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.render.VertexConsumerProvider
import net.minecraft.client.render.entity.EntityRenderer
import net.minecraft.client.render.entity.EntityRendererFactory
import net.minecraft.client.render.entity.model.EntityModelLayers
import net.minecraft.client.render.entity.model.ShulkerBulletEntityModel
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.entity.projectile.ShulkerBulletEntity
import net.minecraft.util.Identifier
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.MathHelper
/**
 * A prebuilt renderer for a missile-type entity. This can be used as a simple way to provide a rendered shape for your missile.
 *
 * The color and overall scales of the entity can be set directly in the constructor.
 */
@Suppress("PrivatePropertyName", "SpellCheckingInspection")
class MissileEntityRenderer(context: EntityRendererFactory.Context, val r: Float = 1.0f, val g: Float = 1.0f, val b: Float = 1.0f, val downScale: Float = 0.5f, val upScale: Float = 1.5f): EntityRenderer<MissileEntity>(context) {


    private val TEXTURE = Identifier("textures/entity/shulker/spark.png")
    private val LAYER = RenderLayer.getEntityTranslucent(TEXTURE)
    private val model: ShulkerBulletEntityModel<ShulkerBulletEntity> = ShulkerBulletEntityModel(context.getPart(EntityModelLayers.SHULKER_BULLET))

    override fun getBlockLight(shulkerBulletEntity: MissileEntity, blockPos: BlockPos): Int {
        return 15
    }

    override fun render(
        missileEntity: MissileEntity,
        f: Float,
        h: Float,
        matrixStack: MatrixStack,
        vertexConsumerProvider: VertexConsumerProvider,
        i: Int
    ) {
        matrixStack.push()
        val k = missileEntity.age.toFloat() + h
        matrixStack.translate(0.0, 0.15, 0.0)
        matrixStack.multiply(FzzyRotation.POSITIVE_Y.degrees(MathHelper.sin(k * 0.1f) * 180.0f))
        matrixStack.multiply(FzzyRotation.POSITIVE_X.degrees(MathHelper.cos(k * 0.1f) * 180.0f))
        matrixStack.multiply(FzzyRotation.POSITIVE_Z.degrees(MathHelper.sin(k * 0.15f) * 360.0f))
        matrixStack.scale(-downScale, -downScale, downScale)
        val vertexConsumer = vertexConsumerProvider.getBuffer(model.getLayer(TEXTURE))
        model.render(matrixStack, vertexConsumer, i, OverlayTexture.DEFAULT_UV, r, g, b, 1.0f)
        matrixStack.scale(upScale, upScale, upScale)
        val vertexConsumer2 = vertexConsumerProvider.getBuffer(LAYER)
        model.render(matrixStack, vertexConsumer2, i, OverlayTexture.DEFAULT_UV, 1.0f, 1.0f, 1.0f, 0.15f)
        matrixStack.pop()
        super.render(missileEntity, f, h, matrixStack, vertexConsumerProvider, i)
    }

    override fun getTexture(missileEntityCopy: MissileEntity): Identifier {
        return TEXTURE
    }
}