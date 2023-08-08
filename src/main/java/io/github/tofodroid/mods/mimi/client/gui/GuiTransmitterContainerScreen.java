package io.github.tofodroid.mods.mimi.client.gui;

import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import org.joml.Vector2f;

import io.github.tofodroid.mods.mimi.common.container.ContainerTransmitter;
import io.github.tofodroid.mods.mimi.common.item.ItemFloppyDisk;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileInfo;
import io.github.tofodroid.mods.mimi.common.network.BroadcastControlPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiInfoPacket;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacket;
import io.github.tofodroid.mods.mimi.common.network.BroadcastControlPacket.CONTROL;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiStatus.STATUS_CODE;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiTransmitterContainerScreen extends BaseContainerGui<ContainerTransmitter> {
    private final Integer SYNC_STATUS_EVERY_TICKS = 10;
    private ServerMusicPlayerStatusPacket playerInfo = null;
    private ServerMidiInfoPacket midiInfo = null;
    private Map<Integer,String> instrumentMap = null;
    private Integer statusSyncTrack = 0;
    private ItemStack lastDiskStack = null;

    // Button Boxes
    private static final Vector2f STOP_BUTTON = new Vector2f(28,190);
    private static final Vector2f PLAY_PAUSE_BUTTON = new Vector2f(48,190);
    private static final Vector2f TRANSMIT_BUTTON = new Vector2f(145,190);
    private static final Vector2f TRANSMIT_SCREEN = new Vector2f(163,191);
    private static final Vector2f TRANSMIT_LIGHT = new Vector2f(338,33);

    // Time Slider
    private static final Integer SLIDE_Y = 189;
    private static final Integer SLIDE_MIN_X = 65;
    private static final Integer SLIDE_MAX_X = 135;
    private static final Integer SLIDE_WIDTH = SLIDE_MAX_X - SLIDE_MIN_X;

    public GuiTransmitterContainerScreen(ContainerTransmitter container, Inventory inv, Component textComponent) {
        super(container, inv, 350, 212, 350, "textures/gui/container_transmitter.png", textComponent);
        this.midiInfo = null;
        this.instrumentMap = null;
        this.playerInfo = null;
        this.statusSyncTrack = this.SYNC_STATUS_EVERY_TICKS;
        this.lastDiskStack = this.menu.getActiveFloppyStack();
        this.syncMidiInfo();
    }

    public void syncPlayerInfo() {
        if(statusSyncTrack >= SYNC_STATUS_EVERY_TICKS) {
            NetworkManager.INFO_CHANNEL.sendToServer(ServerMusicPlayerStatusPacket.requestPacket());
            statusSyncTrack = 0;
        } else {
            statusSyncTrack++;
        }
    }
    
    public void syncMidiInfo() {
        if(this.lastDiskStack != null) {
            NetworkManager.INFO_CHANNEL.sendToServer(new ServerMidiInfoPacket(ItemFloppyDisk.getMidiUrl(this.lastDiskStack)));
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(this.midiInfo != null && this.midiInfo.isValid() && this.playerInfo != null && clickedBox(imouseX, imouseY, STOP_BUTTON)) {
            NetworkManager.INFO_CHANNEL.sendToServer(new BroadcastControlPacket(CONTROL.STOP));
        } else if(this.midiInfo != null && this.midiInfo.isValid() && clickedBox(imouseX, imouseY, PLAY_PAUSE_BUTTON)) {
            if(this.playerInfo != null && this.playerInfo.running) { 
                NetworkManager.INFO_CHANNEL.sendToServer(new BroadcastControlPacket(CONTROL.PAUSE));
            } else {
                NetworkManager.INFO_CHANNEL.sendToServer(new BroadcastControlPacket(CONTROL.PLAY));
            } 
        } else if(clickedBox(imouseX, imouseY, TRANSMIT_BUTTON)) {
            NetworkManager.INFO_CHANNEL.sendToServer(new BroadcastControlPacket(CONTROL.TOGGLE_PUBLIC));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void containerTick() {
        if(this.lastDiskStack != null && (this.menu.getActiveFloppyStack() == null || (this.menu.getActiveFloppyStack() != null && !ItemStack.matches(this.lastDiskStack, this.menu.getActiveFloppyStack())))) {
            this.lastDiskStack = this.menu.getActiveFloppyStack();
            this.playerInfo = null;
            this.midiInfo = null;
            this.instrumentMap = null;
            this.syncMidiInfo();
        } else if(this.lastDiskStack == null) {
            this.lastDiskStack = this.menu.getActiveFloppyStack();
            this.syncMidiInfo();
        }
        
        this.syncPlayerInfo();
    }

    @Override
    protected GuiGraphics renderGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // GUI Background
        graphics.blit(guiTexture, START_X, START_Y, 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        
        // Play/Pause Button
        graphics.blit(guiTexture, START_X + Float.valueOf(PLAY_PAUSE_BUTTON.x()).intValue() + 1, START_Y + Float.valueOf(PLAY_PAUSE_BUTTON.y()).intValue() + 1, 1 + (this.playerInfo != null ? this.playerInfo.running.compareTo(false) : 0) * 13, 245, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
        
        // Transmit Screen    
        graphics.blit(guiTexture, START_X + Float.valueOf(TRANSMIT_SCREEN.x()).intValue(), START_Y + Float.valueOf(TRANSMIT_SCREEN.y()).intValue(), 1 + (13 * (this.menu.getPublicTransmit() ? 0 : 1)), 231, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Transmit Light
        if(this.playerInfo != null && this.playerInfo.running) {
            graphics.blit(guiTexture, START_X + Float.valueOf(TRANSMIT_LIGHT.x()).intValue(), START_Y + Float.valueOf(TRANSMIT_LIGHT.y()).intValue(), 8, 225, 5, 5, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        // Time Slider
        if(this.menu.hasActiveFloppy() && this.midiInfo != null && this.playerInfo != null && STATUS_CODE.SUCCESS.equals(this.playerInfo.status)) {
            Integer slideLength = this.midiInfo.songLengthSeconds;
            Integer slideProgress = this.playerInfo.songPositionSeconds >= 0 ? this.playerInfo.songPositionSeconds : 0;
            Double slidePercentage =  Double.valueOf(slideProgress) / Double.valueOf(slideLength);
            Integer slideOffset = Double.valueOf(Math.floor(slidePercentage * SLIDE_WIDTH)).intValue();
            graphics.blit(guiTexture, START_X + SLIDE_MIN_X + slideOffset, START_Y + SLIDE_Y, 0, 213, 7, 17, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY) {
        if(this.menu.hasActiveFloppy()) {
            if(this.midiInfo != null) {
                switch(this.midiInfo.status) {
                    case ERROR_OTHER:
                        graphics.drawString(this.font,"Failed to load MIDI file from Disk URL.", 7, 33, 0xFF00E600);
                        break;
                    case ERROR_URL:
                        graphics.drawString(this.font,"Disk has invalid or inaccessible URL.", 7, 33, 0xFF00E600);
                        break;
                    case ERROR_DISABLED:
                        graphics.drawString(this.font,"Web MIDI files are disabled by this server.", 7, 33, 0xFF00E600);
                        break;
                    case ERROR_HOST:
                        graphics.drawString(this.font,"Disk URL points to a website not allowed by this server.", 7, 33, 0xFF00E600);
                        break;
                    case ERROR_NOT_FOUND:
                        graphics.drawString(this.font,"Disk URL points to a server MIDI file that does not exist.", 7, 33, 0xFF00E600);
                        break;
                    case SUCCESS:
                        if(this.instrumentMap != null) {
                            graphics.drawString(font, "Channel Instruments: ", 7, 33, 0xFF00E600);
                        
                            Integer index = 0;
                            for(Integer i = 0; i < 16; i+=2) {
                                String name = instrumentMap.get(i) == null ? "None" : instrumentMap.get(i);
                                graphics.drawString(font, (i+1) + ": " + name, 7, 47 + 10*index, 0xFF00E600);
                                index++;
                            }
    
                            index = 0;
                            for(Integer i = 1; i < 16; i+=2) {
                                String name = instrumentMap.get(i) == null ? "None" : instrumentMap.get(i);
                                graphics.drawString(font, (i+1) + ": " + name, 175, 47 + 10*index, 0xFF00E600);
                                index++;
                            }
                        }
                        break;
                    default:
                        break;
                }
            } else {
                graphics.drawString(this.font,"Loading MIDI from Disk...", 7, 33, 0xFF00E600);
            }
            graphics.drawString(font, this.font.substrByWidth(FormattedText.of("Title: " + ItemFloppyDisk.getDiskTitle(this.menu.getActiveFloppyStack())), 169).getString(), 7, 131, 0xFF00E600);
            graphics.drawString(font, this.font.substrByWidth(FormattedText.of("Author: " + ItemFloppyDisk.getDiskAuthor(this.menu.getActiveFloppyStack())), 169).getString(), 7, 141, 0xFF00E600);

            List<FormattedCharSequence> urlLines = this.font.split(FormattedText.of("URL: " + ItemFloppyDisk.getMidiUrl(this.menu.getActiveFloppyStack())), 165);
            int i = 0;
            for(FormattedCharSequence seq : urlLines) {
                if(i < 3) {
                    graphics.drawString(font, seq, i == 0 ? 7 : 11, 151 + (i*10), 0xFF00E600);
                    i++;
                } else {
                    break;
                }
            }
        } else {
            graphics.drawString(this.font,"Insert a Floppy Disk", 7, 33, 0xFF00E600);
        }
        return graphics;
    }

    public void handlePlayerStatusPacket(ServerMusicPlayerStatusPacket packet) {
        if(STATUS_CODE.EMPTY.equals(packet.status)) {
            this.playerInfo = null;
        } else {
            this.playerInfo = packet;
        }
    }
    
    public void handleMidiInfoPacket(ServerMidiInfoPacket packet) {
        this.midiInfo = packet;
        
        if(STATUS_CODE.SUCCESS.equals(packet.status)) {
            this.instrumentMap = MidiFileInfo.getInstrumentMapping(packet.channelMapping);
        } else {
            this.instrumentMap = null;
        }
    }
}
