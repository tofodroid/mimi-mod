package io.github.tofodroid.mods.mimi.client.gui;

import java.util.ArrayList;
import java.util.List;

import org.joml.Vector2i;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;

public abstract class CommonGuiUtils {
    public static final Integer STANDARD_BUTTON_SIZE = 15;
    
    public static final Boolean clickedBox(Integer mouseX, Integer mouseY, Vector2i buttonPos) {
        return clickedBox(mouseX, mouseY, buttonPos, new Vector2i(STANDARD_BUTTON_SIZE, STANDARD_BUTTON_SIZE));
    }
    
    public static final Boolean clickedBox(Integer mouseX, Integer mouseY, Vector2i buttonPos, Vector2i buttonSize) {
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
    public static void blitAbsolute(GuiGraphics graphics, ResourceLocation texture, int renderStartX, int renderStartY, float atlasStartX, float atlasStartY, int sizeX, int sizeY, int textureSizeX, int textureSizeY) {
        RenderSystem.setShaderTexture(0, texture);
        graphics.blit(texture, renderStartX, renderStartY, atlasStartX, atlasStartY, sizeX, sizeY, textureSizeX, textureSizeY);
    }

    public static void drawStringAbsolute(GuiGraphics graphics, Font font, String string, Integer renderStartX, Integer renderStartY, Integer color) {
        graphics.drawString(font, string, renderStartX, renderStartY, color);
    }
    
    public static String truncateString(Font font, String source, Integer maxWidth) {
        if(source == null || font.width(source) <= maxWidth) {
            return source;
        }
        return font.plainSubstrByWidth("..." + source, maxWidth).substring(3) + "...";
    }

    public static List<String> wrapString(Font font, String source, Integer maxWidth, Integer maxLines) {
        if(source == null || font.width(source) <= maxWidth) {
            return List.of(source);
        }

        List<String> lines = new ArrayList<>();
        String remaining = source;

        do {
            String part = font.plainSubstrByWidth(remaining, maxWidth);
            remaining = source.substring(part.length());
            lines.add(part);
        } while(font.width(remaining) > maxWidth || lines.size() <= maxLines);

        lines.add(remaining);

        return lines;
    }
}
