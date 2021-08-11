package io.github.tofodroid.mods.mimi.client.renderer;

import com.mojang.blaze3d.matrix.MatrixStack;

import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;

import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public class EntityMechanicalMaestroRenderer extends EntityRenderer<EntityNoteResponsiveTile> {
    public EntityMechanicalMaestroRenderer(EntityRendererManager manager) {
        super(manager);
    }

    @Override
    public ResourceLocation getEntityTexture(EntityNoteResponsiveTile seatEntity) {
        return null;
    }

    @Override
    protected void renderName(EntityNoteResponsiveTile p_225629_1_, ITextComponent p_225629_2_, MatrixStack p_225629_3_, IRenderTypeBuffer p_225629_4_, int p_225629_5_) {}
}
