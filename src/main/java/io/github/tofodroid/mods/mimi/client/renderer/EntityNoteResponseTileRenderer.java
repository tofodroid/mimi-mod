package io.github.tofodroid.mods.mimi.client.renderer;

import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class EntityNoteResponseTileRenderer extends EntityRenderer<EntityNoteResponsiveTile> {
    public EntityNoteResponseTileRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EntityNoteResponsiveTile p_114482_) {
        return null;
    }
}
