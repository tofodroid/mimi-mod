package io.github.tofodroid.mods.mimi.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;

import io.github.tofodroid.mods.mimi.util.Vector2Int;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public abstract class CommonGuiUtils {
    public static final Integer STANDARD_BUTTON_SIZE = 15;
    
    public static final Boolean clickedBox(Integer mouseX, Integer mouseY, Vector2Int buttonPos) {
        return clickedBox(mouseX, mouseY, buttonPos, new Vector2Int(STANDARD_BUTTON_SIZE, STANDARD_BUTTON_SIZE));
    }
    
    public static final Boolean clickedBox(Integer mouseX, Integer mouseY, Vector2Int buttonPos, Vector2Int buttonSize) {
        Integer buttonMinX = buttonPos.x();
        Integer buttonMaxX = buttonMinX + buttonSize.x();
        Integer buttonMinY = buttonPos.y();
        Integer buttonMaxY = buttonMinY + buttonSize.y();

        Boolean result = mouseX >= buttonMinX && mouseX <= buttonMaxX && mouseY >= buttonMinY && mouseY <= buttonMaxY;

        if(result) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
        }

        return result;
    }
    
    public static void setAlpha(float alpha) {
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);
    }

    public static String truncateString(Font font, String source, Integer maxWidth) {
        if(source == null || font.width(source) <= maxWidth) {
            return source;
        }
        return font.plainSubstrByWidth("..." + source, maxWidth).substring(3) + "...";
    }

    public static void blitAbsolute(PoseStack graphics, ResourceLocation texture, int renderStartX, int renderStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY, int textureSizeX, int textureSizeY) {
        RenderSystem.setShaderTexture(0, texture);
        GuiComponent.blit(graphics, renderStartX, renderStartY, atlasStartX, atlasStartY, sizeX, sizeY, textureSizeX, textureSizeY);
    }

    public static void drawStringAbsolute(PoseStack graphics, Font font, String string, Integer renderStartX, Integer renderStartY, Integer color) {
        GuiComponent.drawString(graphics, font, string, renderStartX, renderStartY, color);
    }

    public static void pushLayer(PoseStack graphics) {
        graphics.pushPose();
    }

    public static void rotateLayer(PoseStack graphics, Float degrees) {
        graphics.mulPose(new Quaternion(new Vector3f(0,0,1),-90,true));
    }

    public static void popLayer(PoseStack graphics) {
        graphics.popPose();
    }
}
