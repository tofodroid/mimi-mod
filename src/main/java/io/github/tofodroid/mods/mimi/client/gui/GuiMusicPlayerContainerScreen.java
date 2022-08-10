package io.github.tofodroid.mods.mimi.client.gui;

import java.util.List;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;

import io.github.tofodroid.mods.mimi.common.container.ContainerMusicPlayer;
import io.github.tofodroid.mods.mimi.common.item.ItemFloppyDisk;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileInfo;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiInfoPacket;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiInfoPacket.STATUS_CODE;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiMusicPlayerContainerScreen extends BaseContainerGui<ContainerMusicPlayer> {
    private final Integer SYNC_EVERY_TICKS = 10;
    private ServerMidiInfoPacket midiInfo = null;
    private Map<Integer,String> instrumentMap = null;
    private Integer infoSyncTrack = 0;
    private ItemStack lastDiskStack = null;

    // Time Slider
    private static final Integer SLIDE_Y = 188;
    private static final Integer SLIDE_MIN_X = 39;
    private static final Integer SLIDE_MAX_X = 165;
    private static final Integer SLIDE_WIDTH = SLIDE_MAX_X - SLIDE_MIN_X;

    public GuiMusicPlayerContainerScreen(ContainerMusicPlayer container, Inventory inv, Component textComponent) {
        super(container, inv, 350, 212, 350, "textures/gui/container_music_player.png", textComponent);
        resetMidiInfo();
        this.lastDiskStack = this.menu.getActiveFloppyStack();
        this.infoSyncTrack = this.SYNC_EVERY_TICKS;
    }

    public void resetMidiInfo() {
        this.midiInfo = null;
        this.instrumentMap = null;
    }

    public void syncMidiInfo() {
        if(infoSyncTrack >= SYNC_EVERY_TICKS) {
            NetworkManager.NET_CHANNEL.sendToServer(new ServerMidiInfoPacket(STATUS_CODE.INFO, null, null, null));
            infoSyncTrack = 0;
        } else {
            infoSyncTrack++;
        }
    }

    @Override
    protected void containerTick() {
        if(this.lastDiskStack != null && !ItemStack.matches(this.lastDiskStack, this.menu.getActiveFloppyStack())) {
            this.lastDiskStack = this.menu.getActiveFloppyStack();
            this.resetMidiInfo();
        } else if(this.lastDiskStack == null) {
            this.lastDiskStack = this.menu.getActiveFloppyStack();
        }
        
        this.syncMidiInfo();
    }

    @Override
    protected PoseStack renderGraphics(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // GUI Background
        blit(matrixStack, START_X, START_Y, this.getBlitOffset(), 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        
        Integer slideOffset = 0;
        if(this.menu.hasActiveFloppy() && this.midiInfo != null && STATUS_CODE.INFO.equals(this.midiInfo.status)) {
            Integer slideLength = this.midiInfo.songLengthSeconds;
            Integer slideProgress = this.midiInfo.songPositionSeconds;
            Double slidePercentage =  Double.valueOf(slideProgress) / Double.valueOf(slideLength);
            slideOffset = Double.valueOf(Math.floor(slidePercentage * SLIDE_WIDTH)).intValue();
        }

        blit(matrixStack, START_X + SLIDE_MIN_X + slideOffset, START_Y + SLIDE_Y, this.getBlitOffset(), 0, 213, 7, 17, TEXTURE_SIZE, TEXTURE_SIZE);

        return matrixStack;
    }

    @Override
    protected PoseStack renderText(PoseStack matrixStack, int mouseX, int mouseY) {
        if(this.menu.hasActiveFloppy()) {
            if(this.midiInfo != null) {
                switch(this.midiInfo.status) {
                    case ERROR_OTHER:
                        this.font.draw(matrixStack,"Unable to parse MIDI file from Disk URL.", 7, 33, 0xFF00E600);
                        break;
                    case ERROR_URL:
                        this.font.draw(matrixStack,"Disk has invalid or inaccessible URL.", 7, 33, 0xFF00E600);
                        break;
                    case INFO:
                        this.font.draw(matrixStack, "Channel Instruments: ", 7, 33, 0xFF00E600);
                        
                        Integer index = 0;
                        for(Integer i = 0; i < 16; i+=2) {
                            String name = instrumentMap.get(i) == null ? "None" : instrumentMap.get(i);
                            this.font.draw(matrixStack, (i+1) + ": " + name, 7, 47 + 10*index, 0xFF00E600);
                            index++;
                        }

                        index = 0;
                        for(Integer i = 1; i < 16; i+=2) {
                            String name = instrumentMap.get(i) == null ? "None" : instrumentMap.get(i);
                            this.font.draw(matrixStack, (i+1) + ": " + name, 175, 47 + 10*index, 0xFF00E600);
                            index++;
                        }
                        break;
                    default:
                        break;
                }
            } else {
                this.font.draw(matrixStack,"Loading MIDI from Disk...", 7, 33, 0xFF00E600);
            }
            this.font.draw(matrixStack, this.font.substrByWidth(FormattedText.of("Title: " + ItemFloppyDisk.getDiskTitle(this.menu.getActiveFloppyStack())), 169).getString(), 7, 131, 0xFF00E600);
            this.font.draw(matrixStack, this.font.substrByWidth(FormattedText.of("Author: " + ItemFloppyDisk.getDiskAuthor(this.menu.getActiveFloppyStack())), 169).getString(), 7, 141, 0xFF00E600);

            List<FormattedCharSequence> urlLines = this.font.split(FormattedText.of("URL: " + ItemFloppyDisk.getMidiUrl(this.menu.getActiveFloppyStack())), 165);
            int i = 0;
            for(FormattedCharSequence seq : urlLines) {
                if(i < 3) {
                    this.font.draw(matrixStack, seq, i == 0 ? 7 : 11, 151 + (i*10), 0xFF00E600);
                    i++;
                } else {
                    break;
                }
            }
        } else {
            this.font.draw(matrixStack,"Insert a Floppy Disk", 7, 33, 0xFF00E600);
        }
        return matrixStack;
    }

    public void handleMidiInfoPacket(ServerMidiInfoPacket packet) {
        this.midiInfo = packet;

        if(STATUS_CODE.INFO.equals(packet.status)) {
            this.instrumentMap = MidiFileInfo.getInstrumentMapping(packet.channelMapping);
        } else if(STATUS_CODE.EMPTY.equals(packet.status)) {
            this.resetMidiInfo();
        }
    }
}
