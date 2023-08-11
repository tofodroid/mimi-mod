package io.github.tofodroid.mods.mimi.client.gui;

import org.joml.Vector2i;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.tofodroid.mods.mimi.client.gui.widget.MidiChannelToggleWidget;
import io.github.tofodroid.mods.mimi.client.gui.widget.NoteFilterWidget;
import io.github.tofodroid.mods.mimi.client.gui.widget.TransmitterSourceWidget;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.ConfigurableMidiTileSyncPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GuiReceiver extends BaseGui {
    // GUI
    private static final Vector2i MIDI_CHANNEL_WIDGET_COORDS = new Vector2i(6,31);
    private static final Vector2i NOTE_FILTER_WIDGET_COORDS = new Vector2i(180,26);
    private static final Vector2i TRANSMIT_SOURCE_WIDGET_COORDS = new Vector2i(180,59);

    // Widgets
    private MidiChannelToggleWidget midiChannelToggle;
    private NoteFilterWidget noteFilter;
    private TransmitterSourceWidget transmitSource;

    // Input Data
    private final Player player;
    private final ItemStack receiverStack;
    private final BlockPos tilePos;

    public GuiReceiver(Player player, BlockPos tilePos, ItemStack receiverStack) {
        super(302, 109, 302, "textures/gui/container_receiver.png", "item.MIMIMod.gui_receiver");
        this.player = player;
        this.tilePos = tilePos;

        if(receiverStack == null || receiverStack.isEmpty()) {
            MIMIMod.LOGGER.error("Receiver stack is null or empty. Force closing GUI!");
            Minecraft.getInstance().forceSetScreen((Screen)null);
            this.receiverStack = null;
            return;
        }
        this.receiverStack = new ItemStack(receiverStack.getItem(), receiverStack.getCount());
        this.receiverStack.setTag(receiverStack.getOrCreateTag().copy());
    }

    @Override
    public void init() {
        super.init();
        this.midiChannelToggle = new MidiChannelToggleWidget(receiverStack, new Vector2i(START_X, START_Y), MIDI_CHANNEL_WIDGET_COORDS);
        this.noteFilter = new NoteFilterWidget(receiverStack, new Vector2i(START_X, START_Y), NOTE_FILTER_WIDGET_COORDS);
        this.transmitSource = new TransmitterSourceWidget(receiverStack, player.getUUID(), player.getName().getString(), new Vector2i(START_X, START_Y), TRANSMIT_SOURCE_WIDGET_COORDS);
    }

    public void syncListenerToServer() {
        NetworkManager.INFO_CHANNEL.sendToServer(new ConfigurableMidiTileSyncPacket(receiverStack, tilePos));
    }

    @Override
    public boolean mouseClicked(double dmouseX, double dmouseY, int mouseButton) {
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);
        
        // MIDI Controls
        if(midiChannelToggle.mouseClicked(imouseX, imouseY, mouseButton)) {
            this.syncListenerToServer();
        } else if(transmitSource.mouseClicked(imouseX, imouseY, mouseButton)) {
            this.syncListenerToServer();
        } else if(noteFilter.mouseClicked(imouseX, imouseY, mouseButton)) {
            this.syncListenerToServer();
        }

        return super.mouseClicked(dmouseX, dmouseY, mouseButton);
    }

    // Render Functions
    @Override
    protected GuiGraphics renderGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // GUI Background
        graphics.blit(guiTexture, START_X, START_Y, 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    
        this.midiChannelToggle.renderGraphics(graphics, mouseX, mouseY);
        this.noteFilter.renderGraphics(graphics, mouseX, mouseY);
        this.transmitSource.renderGraphics(graphics, mouseX, mouseY);
        
        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.midiChannelToggle.renderText(graphics, font, mouseX, mouseY);
        this.noteFilter.renderText(graphics, font, mouseX, mouseY);
        this.transmitSource.renderText(graphics, font, mouseX, mouseY);

        return graphics;
    }
}