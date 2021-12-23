package io.github.tofodroid.mods.mimi.client.renderer;

import io.github.tofodroid.mods.mimi.common.entity.EntitySeat;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class EntitySeatRenderer extends EntityRenderer<EntitySeat> {
    public EntitySeatRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EntitySeat p_114482_) {
        return null;
    }
}
