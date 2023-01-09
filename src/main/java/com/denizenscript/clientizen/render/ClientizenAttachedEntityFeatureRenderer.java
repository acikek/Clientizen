package com.denizenscript.clientizen.render;

import com.denizenscript.clientizen.objects.EntityTag;
import com.denizenscript.clientizen.scripts.commands.AttachCommand;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

import java.util.List;

public class ClientizenAttachedEntityFeatureRenderer<T extends Entity, M extends EntityModel<T>> extends FeatureRenderer<T, M> {

	public ClientizenAttachedEntityFeatureRenderer(FeatureRendererContext<T, M> context) {
		super(context);
	}

	@Override
	public void render(MatrixStack matrices, VertexConsumerProvider vertexConsumers, int light, T entity, float limbAngle, float limbDistance, float tickDelta, float animationProgress, float headYaw, float headPitch) {
		List<EntityTag> attachedEntities = AttachCommand.attachMap.get(entity.getUuid());
		if (attachedEntities == null) {
			return;
		}
		for (EntityTag attached : attachedEntities) {
			matrices.push();
			matrices.scale(1, -1, 1);
			EntityRenderer<? super Entity> renderer = MinecraftClient.getInstance().getEntityRenderDispatcher().getRenderer(attached.getEntity());
			BlockPos blockPos = entity.getBlockPos().add(0, 1, 0);
			light = LightmapTextureManager.pack(renderer.getBlockLight(attached.getEntity(), blockPos), entity.world.getLightLevel(LightType.SKY, blockPos));
			MinecraftClient.getInstance().getEntityRenderDispatcher().render(attached.getEntity(), 0, 0, 0, attached.getEntity().getYaw(tickDelta), tickDelta, matrices, vertexConsumers, light);
			matrices.pop();
		}
	}
}
