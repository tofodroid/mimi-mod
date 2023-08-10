package io.github.tofodroid.mods.mimi.client.gui;

import org.joml.Vector2i;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
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
}