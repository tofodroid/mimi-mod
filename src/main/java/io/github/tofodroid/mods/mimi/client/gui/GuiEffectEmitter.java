package io.github.tofodroid.mods.mimi.client.gui;

import org.lwjgl.glfw.GLFW;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.tofodroid.mods.mimi.client.gui.widget.InvertSignalWidget;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockEffectEmitter;
import io.github.tofodroid.mods.mimi.common.network.EffectEmitterUpdatePacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileEffectEmitter;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.client.Minecraft;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class GuiEffectEmitter extends BaseGui {
    private static final Integer DEFAULT_TEXT_FIELD_COLOR = 14737632;

    // GUI
    private static final Vector2Int INVERT_SIGNAL_WIDGET_COORDS = new Vector2Int(281,5);

    private static final Vector2Int SOUND_BOX_COORDS = new Vector2Int(46,32);
    private static final Vector2Int PLAY_SOUND_BUTTON_COORDS = new Vector2Int(278,29);

    private static final Vector2Int VOL_DOWN_BUTTON_COORDS = new Vector2Int(30,51);
    private static final Vector2Int VOL_UP_BUTTON_COORDS = new Vector2Int(66,51);
    private static final Vector2Int VOL_TEXT_COORDS = new Vector2Int(50,55);
    private static final Vector2Int PITCH_DOWN_BUTTON_COORDS = new Vector2Int(116,51);
    private static final Vector2Int PITCH_UP_BUTTON_COORDS = new Vector2Int(152,51);
    private static final Vector2Int PITCH_TEXT_COORDS = new Vector2Int(136,55);
    private static final Vector2Int S_LOOP_DOWN_BUTTON_COORDS = new Vector2Int(230, 51);
    private static final Vector2Int S_LOOP_UP_BUTTON_COORDS = new Vector2Int(278, 51);
    private static final Vector2Int S_LOOP_TEXT_COORDS = new Vector2Int(250, 55);

    private static final Vector2Int PARTICLE_BOX_COORDS = new Vector2Int(51,81);
    private static final Vector2Int PLAY_PARTICLE_BUTTON_COORDS = new Vector2Int(278,78);
    
    private static final Vector2Int SIDE_DOWN_BUTTON_COORDS = new Vector2Int(32,100);
    private static final Vector2Int SIDE_UP_BUTTON_COORDS = new Vector2Int(62,100);
    private static final Vector2Int SIDE_TEXT_COORDS = new Vector2Int(52,104);
    private static final Vector2Int SPEED_X_DOWN_BUTTON_COORDS = new Vector2Int(116,100);
    private static final Vector2Int SPEED_X_UP_BUTTON_COORDS = new Vector2Int(158,100);
    private static final Vector2Int SPEED_X_TEXT_COORDS = new Vector2Int(136,107);
    private static final Vector2Int SPEED_Y_DOWN_BUTTON_COORDS = new Vector2Int(176,100);
    private static final Vector2Int SPEED_Y_UP_BUTTON_COORDS = new Vector2Int(218,100);
    private static final Vector2Int SPEED_Y_TEXT_COORDS = new Vector2Int(196,107);
    private static final Vector2Int SPEED_Z_DOWN_BUTTON_COORDS = new Vector2Int(236,100);
    private static final Vector2Int SPEED_Z_UP_BUTTON_COORDS = new Vector2Int(278,100);
    private static final Vector2Int SPEED_Z_TEXT_COORDS = new Vector2Int(256,107);
    private static final Vector2Int SPREAD_DOWN_BUTTON_COORDS = new Vector2Int(36,122);
    private static final Vector2Int SPREAD_UP_BUTTON_COORDS = new Vector2Int(66,122);
    private static final Vector2Int SPREAD_TEXT_COORDS = new Vector2Int(56,126);
    private static final Vector2Int COUNT_DOWN_BUTTON_COORDS = new Vector2Int(116,122);
    private static final Vector2Int COUNT_UP_BUTTON_COORDS = new Vector2Int(152,122);
    private static final Vector2Int COUNT_TEXT_COORDS = new Vector2Int(136,126);
    private static final Vector2Int P_LOOP_DOWN_BUTTON_COORDS = new Vector2Int(230, 122);
    private static final Vector2Int P_LOOP_UP_BUTTON_COORDS = new Vector2Int(278, 122);
    private static final Vector2Int P_LOOP_TEXT_COORDS = new Vector2Int(250, 126);

    // Widgets
    private InvertSignalWidget invertSignalWidget;
    private EditBox soundBox;
    private EditBox particleBox;

    // Input Data
    private final Level world;
    private final ItemStack emitterStack;
    private final BlockPos tilePos;

    // Instance Data
    private Boolean shiftModifier = false;

    public GuiEffectEmitter(Level world, BlockPos tilePos, ItemStack emitterStack) {
        super(302, 146, 302, "textures/gui/gui_effect_emitter.png", "item.MIMIMod.gui_effect_emitter");
        this.tilePos = tilePos;
        this.world = world;

        if(emitterStack == null || emitterStack.isEmpty()) {
            MIMIMod.LOGGER.error("Emitter stack is null or empty. Force closing GUI!");
            Minecraft.getInstance().forceSetScreen((Screen)null);
            this.emitterStack = null;
            return;
        }
        this.emitterStack = new ItemStack(emitterStack.getItem(), emitterStack.getCount());
        this.emitterStack.setTag(emitterStack.getOrCreateTag().copy());
    }

    @Override
    public void init() {
        super.init();
        this.invertSignalWidget = new InvertSignalWidget(emitterStack, new Vector2Int(START_X, START_Y), INVERT_SIGNAL_WIDGET_COORDS);
        this.soundBox = this.addWidget(new EditBox(this.font, this.START_X + SOUND_BOX_COORDS.x, this.START_Y + SOUND_BOX_COORDS.y, 229, 10, CommonComponents.EMPTY));
            soundBox.setMaxLength(512);    
            soundBox.setValue(TagUtils.getStringOrDefault(emitterStack, TileEffectEmitter.SOUND_ID_TAG, ""));
            soundBox.setResponder(this::handleSoundChange);
            soundBox.setTextColor(this.validateSound(soundBox.getValue()) ? DEFAULT_TEXT_FIELD_COLOR : 13112340);
        this.particleBox = this.addWidget(new EditBox(this.font, this.START_X + PARTICLE_BOX_COORDS.x, this.START_Y + PARTICLE_BOX_COORDS.y, 224, 10, CommonComponents.EMPTY));
            particleBox.setMaxLength(512);    
            particleBox.setValue(TagUtils.getStringOrDefault(emitterStack, TileEffectEmitter.PARTICLE_ID_TAG, ""));
            particleBox.setResponder(this::handleParticleChange);
            particleBox.setTextColor(this.validateParticle(particleBox.getValue()) ? DEFAULT_TEXT_FIELD_COLOR : 13112340);
    }
    
    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        super.keyPressed(keyCode, scanCode, modifiers);
        
        if(keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.shiftModifier = true;
        }
        
        return true;
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        super.keyReleased(keyCode, scanCode, modifiers);

        if(keyCode == GLFW.GLFW_KEY_LEFT_SHIFT || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            this.shiftModifier = false;
        }

        return true;
    }

    @Override
    public boolean mouseClicked(double dmouseX, double dmouseY, int mouseButton) {
        Boolean mouseModifier = !this.shiftModifier && mouseButton == GLFW.GLFW_MOUSE_BUTTON_RIGHT;
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);
        int addAmount = this.shiftModifier ? 10000 : 1;

        if(invertSignalWidget.mouseClicked(imouseX, imouseY, mouseButton)) {
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(PLAY_SOUND_BUTTON_COORDS))) {
            this.playCurrentSound();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(VOL_DOWN_BUTTON_COORDS))) {
            Byte volume = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.VOLUME_TAG, 5);
            addAmount = mouseModifier ? 2 : addAmount;
            volume = this.addClamped(volume, -addAmount, 0, 10);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.VOLUME_TAG, volume);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(VOL_UP_BUTTON_COORDS))) {
            Byte volume = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.VOLUME_TAG, 5);
            addAmount = mouseModifier ? 2 : addAmount;
            volume = this.addClamped(volume, addAmount, 0, 10);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.VOLUME_TAG, volume);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(PITCH_DOWN_BUTTON_COORDS))) {
            Byte pitch = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.PITCH_TAG, 0);
            addAmount = mouseModifier ? 2 : addAmount;
            pitch = this.addClamped(pitch, -addAmount, -2, 2);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.PITCH_TAG, pitch);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(PITCH_UP_BUTTON_COORDS))) {
            Byte pitch = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.PITCH_TAG, 0);
            addAmount = mouseModifier ? 2 : addAmount;
            pitch = this.addClamped(pitch, addAmount, -2, 2);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.PITCH_TAG, pitch);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(PLAY_PARTICLE_BUTTON_COORDS))) {
            this.playCurrentParticle();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SIDE_DOWN_BUTTON_COORDS))) {
            Byte side = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SIDE_TAG, 0);
            addAmount = mouseModifier ? 2 : addAmount;
            side = this.addClamped(side, -addAmount, 0, 5);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.SIDE_TAG, side);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SIDE_UP_BUTTON_COORDS))) {
            Byte side = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SIDE_TAG, 0);
            addAmount = mouseModifier ? 2 : addAmount;
            side = this.addClamped(side, addAmount, 0, 5);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.SIDE_TAG, side);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SPREAD_DOWN_BUTTON_COORDS))) {
            Byte spread = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SPREAD_TAG, 0);
            addAmount = mouseModifier ? 2 : addAmount;
            spread = this.addClamped(spread, -addAmount, 0, 5);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.SPREAD_TAG, spread);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SPREAD_UP_BUTTON_COORDS))) {
            Byte spread = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SPREAD_TAG, 0);
            addAmount = mouseModifier ? 2 : addAmount;
            spread = this.addClamped(spread, addAmount, 0, 5);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.SPREAD_TAG, spread);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(COUNT_DOWN_BUTTON_COORDS))) {
            Byte count = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.COUNT_TAG, 1);
            addAmount = mouseModifier ? 5 : addAmount;
            count = this.addClamped(count, -addAmount, 1, 20);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.COUNT_TAG, count);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(COUNT_UP_BUTTON_COORDS))) {
            Byte count = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.COUNT_TAG, 1);
            addAmount = mouseModifier ? 5 : addAmount;
            count = this.addClamped(count, addAmount, 1, 20);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.COUNT_TAG, count);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SPEED_X_DOWN_BUTTON_COORDS))) {
            Byte speed = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SPEED_X_TAG, 0);
            addAmount = mouseModifier ? 5 : addAmount;
            speed = this.addClamped(speed, -addAmount, -50, 50);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.SPEED_X_TAG, speed);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SPEED_X_UP_BUTTON_COORDS))) {
            Byte speed = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SPEED_X_TAG, 0);
            addAmount = mouseModifier ? 5 : addAmount;
            speed = this.addClamped(speed, addAmount, -50, 50);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.SPEED_X_TAG, speed);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SPEED_Y_DOWN_BUTTON_COORDS))) {
            Byte speed = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SPEED_Y_TAG, 0);
            addAmount = mouseModifier ? 5 : addAmount;
            speed = this.addClamped(speed, -addAmount, -50, 50);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.SPEED_Y_TAG, speed);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SPEED_Y_UP_BUTTON_COORDS))) {
            Byte speed = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SPEED_Y_TAG, 0);
            addAmount = mouseModifier ? 5 : addAmount;
            speed = this.addClamped(speed, addAmount, -50, 50);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.SPEED_Y_TAG, speed);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SPEED_Z_DOWN_BUTTON_COORDS))) {
            Byte speed = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SPEED_Z_TAG, 0);
            addAmount = mouseModifier ? 5 : addAmount;
            speed = this.addClamped(speed, -addAmount, -50, 50);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.SPEED_Z_TAG, speed);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(SPEED_Z_UP_BUTTON_COORDS))) {
            Byte speed = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SPEED_Z_TAG, 0);
            addAmount = mouseModifier ? 5 : addAmount;
            speed = this.addClamped(speed, addAmount, -50, 50);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.SPEED_Z_TAG, speed);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(S_LOOP_DOWN_BUTTON_COORDS))) {
            Byte loop = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SOUND_LOOP_TAG, 0);
            addAmount = mouseModifier ? 10 : addAmount;
            loop = this.addClamped(loop, -addAmount, 0, 999);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.SOUND_LOOP_TAG, loop);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(S_LOOP_UP_BUTTON_COORDS))) {
            Byte loop = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SOUND_LOOP_TAG, 0);
            addAmount = mouseModifier ? 10 : addAmount;
            loop = this.addClamped(loop, addAmount, 0, 999);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.SOUND_LOOP_TAG, loop);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(P_LOOP_DOWN_BUTTON_COORDS))) {
            Byte loop = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.PARTICLE_LOOP_TAG, 0);
            addAmount = mouseModifier ? 10 : addAmount;
            loop = this.addClamped(loop, -addAmount, 0, 999);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.PARTICLE_LOOP_TAG, loop);
            this.syncEffectEmitterToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(P_LOOP_UP_BUTTON_COORDS))) {
            Byte loop = TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.PARTICLE_LOOP_TAG, 0);
            addAmount = mouseModifier ? 10 : addAmount;
            loop = this.addClamped(loop, addAmount, 0, 999);
            TagUtils.setOrRemoveByte(emitterStack, TileEffectEmitter.PARTICLE_LOOP_TAG, loop);
            this.syncEffectEmitterToServer();
        }

        return super.mouseClicked(dmouseX, dmouseY, mouseButton);
    }

    public Byte addClamped(Byte value, Integer add, Integer min, Integer max) {
        return addClamped(value.intValue(), add, min, max).byteValue();
    }

    public Integer addClamped(Integer value, Integer add, Integer min, Integer max) {
        Integer result = value + add;

        if(result < min) {
            result = min;
        } else if(result > max) {
            result = max;
        }

        return result;
    }

    public void syncEffectEmitterToServer() {
        NetworkProxy.sendToServer(new EffectEmitterUpdatePacket(emitterStack, tilePos));
    }

    public Boolean validateParticle(String newParticleString) {
        if(!newParticleString.trim().isBlank()) {
            try {
                ParticleOptions options = (ParticleOptions)this.world.registryAccess().registry(Registry.PARTICLE_TYPE_REGISTRY).get().get(new ResourceLocation(newParticleString.trim()));
                return options != null;
            } catch(Exception e) { /* No-op */ }
        }
        return false;
    }

    public Boolean validateSound(String newSoundString) {
        if(!newSoundString.trim().isBlank()) {
            try {
                return this.world.registryAccess().registry(Registry.SOUND_EVENT_REGISTRY).get().containsKey(new ResourceLocation(newSoundString.trim()));
            } catch(Exception e) { /* No-op */ }
        }
        return false;
    }

    public void handleParticleChange(String newParticleString) {
        Boolean particleValid = validateParticle(newParticleString);
        this.particleBox.setTextColor(particleValid ? DEFAULT_TEXT_FIELD_COLOR : 13112340);
        TagUtils.setOrRemoveString(emitterStack, TileEffectEmitter.PARTICLE_ID_TAG, newParticleString.trim());
        this.syncEffectEmitterToServer();
    }

    public void handleSoundChange(String newSoundString) {
        Boolean soundValid = validateSound(newSoundString);
        this.soundBox.setTextColor(soundValid ? DEFAULT_TEXT_FIELD_COLOR : 13112340);
        TagUtils.setOrRemoveString(emitterStack, TileEffectEmitter.SOUND_ID_TAG, newSoundString.trim());
        this.syncEffectEmitterToServer();
    }

    public void playCurrentSound() {
        TileEffectEmitter tile = world.getBlockEntity(tilePos, ModTiles.EFFECTEMITTER).orElse(null);

        if(tile != null) {
            tile.playSoundLocal();
        }
    }

    public void playCurrentParticle() {
        TileEffectEmitter tile = world.getBlockEntity(tilePos, ModTiles.EFFECTEMITTER).orElse(null);

        if(tile != null) {
            tile.playParticleLocal();
        }
    }

    // Render Functions
    @Override
    protected PoseStack renderGraphics(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // GUI Background
        blit(graphics, START_X, START_Y, 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    
        // Widgets
        this.invertSignalWidget.renderGraphics(graphics, mouseX, mouseY);
        this.soundBox.render(graphics, mouseX, mouseY, partialTicks);
        this.particleBox.render(graphics, mouseX, mouseY, partialTicks);
        
        return graphics;
    }

    @Override
    protected PoseStack renderText(PoseStack graphics, int mouseX, int mouseY, float partialTicks) {
        // Widgets
        this.invertSignalWidget.renderText(graphics, font, mouseX, mouseY);

        // Counters
        drawString(graphics, font, TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.VOLUME_TAG, 5).toString(), START_X + VOL_TEXT_COORDS.x, START_Y + VOL_TEXT_COORDS.y, 0xFF00E600);
        drawString(graphics, font, TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.PITCH_TAG, 0).toString(), START_X + PITCH_TEXT_COORDS.x, START_Y + PITCH_TEXT_COORDS.y, 0xFF00E600);
        drawString(graphics, font, TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SPREAD_TAG, 0).toString(), START_X + SPREAD_TEXT_COORDS.x, START_Y + SPREAD_TEXT_COORDS.y, 0xFF00E600);
        drawString(graphics, font, BlockEffectEmitter.getSideFromByte(TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SIDE_TAG, 0)).substring(0,1), START_X + SIDE_TEXT_COORDS.x, START_Y + SIDE_TEXT_COORDS.y, 0xFF00E600);
        drawString(graphics, font, TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.COUNT_TAG, 1).toString(), START_X + COUNT_TEXT_COORDS.x, START_Y + COUNT_TEXT_COORDS.y, 0xFF00E600);
        drawString(graphics, font, TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SPEED_X_TAG, 0).toString(), START_X + SPEED_X_TEXT_COORDS.x, START_Y + SPEED_X_TEXT_COORDS.y, 0xFF00E600);
        drawString(graphics, font, TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SPEED_Y_TAG, 0).toString(), START_X + SPEED_Y_TEXT_COORDS.x, START_Y + SPEED_Y_TEXT_COORDS.y, 0xFF00E600);
        drawString(graphics, font, TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SPEED_Z_TAG, 0).toString(), START_X + SPEED_Z_TEXT_COORDS.x, START_Y + SPEED_Z_TEXT_COORDS.y, 0xFF00E600);
        drawString(graphics, font, getLoopFromByte(TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.SOUND_LOOP_TAG, 0)), START_X + S_LOOP_TEXT_COORDS.x, START_Y + S_LOOP_TEXT_COORDS.y, 0xFF00E600);
        drawString(graphics, font, getLoopFromByte(TagUtils.getByteOrDefault(emitterStack, TileEffectEmitter.PARTICLE_LOOP_TAG, 0)), START_X + P_LOOP_TEXT_COORDS.x, START_Y + P_LOOP_TEXT_COORDS.y, 0xFF00E600);

        return graphics;
    }

    public String getLoopFromByte(Byte loop) {
        if(loop == 0) {
            return "None";
        }
        return loop.toString();
    }
}