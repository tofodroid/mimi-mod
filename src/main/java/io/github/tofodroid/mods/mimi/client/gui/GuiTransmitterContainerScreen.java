package io.github.tofodroid.mods.mimi.client.gui;


import java.util.List;
import java.util.Map;

import javax.sound.midi.Sequence;

import org.apache.commons.lang3.tuple.Pair;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.container.ContainerTransmitter;
import io.github.tofodroid.mods.mimi.common.item.ItemFloppyDisk;
import io.github.tofodroid.mods.mimi.common.item.ItemTransmitter;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileCacheManager;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileInfo;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiInfoPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterStackUpdatePacket;
import io.github.tofodroid.mods.mimi.common.network.ServerMidiInfoPacket.STATUS_CODE;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

public class GuiTransmitterContainerScreen extends BaseContainerGui<ContainerTransmitter> {
    private Map<Integer,String> instrumentMap = null;
    private ItemStack lastDiskStack = null;
    private MidiFileInfo midiInfo = null;
    private ServerMidiInfoPacket.STATUS_CODE status = null;
    private Boolean loadedFloppyValid = false;

    // Button Boxes
    private static final Vector3f STOP_BUTTON = new Vector3f(28,190,0);
    private static final Vector3f PLAY_PAUSE_BUTTON = new Vector3f(48,190,0);
    private static final Vector3f TRANSMIT_BUTTON = new Vector3f(145,190,0);
    private static final Vector3f TRANSMIT_SCREEN = new Vector3f(163,191,0);
    private static final Vector3f TRANSMIT_LIGHT = new Vector3f(338,33,0);

    // Time Slider
    private static final Integer SLIDE_Y = 189;
    private static final Integer SLIDE_MIN_X = 65;
    private static final Integer SLIDE_MAX_X = 135;
    private static final Integer SLIDE_WIDTH = SLIDE_MAX_X - SLIDE_MIN_X;

    public GuiTransmitterContainerScreen(ContainerTransmitter container, Inventory inv, Component textComponent) {
        super(container, inv, 350, 212, 350, "textures/gui/container_transmitter.png", textComponent);
        this.lastDiskStack = this.menu.getActiveFloppyStack();

        if(this.menu.hasActiveFloppy()) {
            loadFloppyDisk();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        int imouseX = (int)Math.round(mouseX);
        int imouseY = (int)Math.round(mouseY);

        if(loadedFloppyValid && clickedBox(imouseX, imouseY, STOP_BUTTON)) {
            ((ClientProxy)MIMIMod.proxy).getMidiInput().transmitterManager.stop();
        } else if(loadedFloppyValid && clickedBox(imouseX, imouseY, PLAY_PAUSE_BUTTON)) {
            if(((ClientProxy)MIMIMod.proxy).getMidiInput().transmitterManager.isPlaying()) { 
                ((ClientProxy)MIMIMod.proxy).getMidiInput().transmitterManager.pause();
            } else if(((ClientProxy)MIMIMod.proxy).getMidiInput().transmitterIsActive()) {
                ((ClientProxy)MIMIMod.proxy).getMidiInput().transmitterManager.play();
            } else {
                ((ClientProxy)MIMIMod.proxy).getMidiInput().setActiveSlot(this.menu.playerInvSlot, this.menu.getTransmitterStack());
            }
        } else if(clickedBox(imouseX, imouseY, TRANSMIT_BUTTON)) {
            TransmitMode oldMode = ItemTransmitter.getTransmitMode(this.menu.getTransmitterStack());
            TransmitMode newMode;
            
            if(TransmitMode.SELF.equals(oldMode)) {
                newMode = TransmitMode.LINKED;
            } else if(TransmitMode.LINKED.equals(oldMode)) {
                newMode = TransmitMode.PUBLIC;
            } else {
                newMode = TransmitMode.SELF;
            }

            NetworkManager.NET_CHANNEL.sendToServer(new TransmitterStackUpdatePacket(newMode));
            NetworkManager.NET_CHANNEL.sendToServer(TransmitterNotePacket.createAllNotesOffPacket(TransmitterNotePacket.ALL_CHANNELS, oldMode));
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    protected void containerTick() {
        Boolean changed = false;

        if(this.lastDiskStack != null && (this.menu.getActiveFloppyStack() == null || (this.menu.getActiveFloppyStack() != null && !ItemStack.matches(this.lastDiskStack, this.menu.getActiveFloppyStack())))) {
            this.lastDiskStack = this.menu.getActiveFloppyStack();
            changed = true;
        } else if(this.lastDiskStack == null) {
            this.lastDiskStack = this.menu.getActiveFloppyStack();
            changed = true;
        }

        if(changed && this.menu.hasActiveFloppy()) {
            this.status = null;
            this.midiInfo = null;
            loadFloppyDisk();
        }
    }

    protected void loadFloppyDisk() {
        Pair<Sequence,ServerMidiInfoPacket.STATUS_CODE> result = MidiFileCacheManager.getOrCreateCachedSequence(
            ItemFloppyDisk.getMidiUrl(this.menu.getActiveFloppyStack())
        );

        this.status = result.getRight() != null ? result.getRight() : STATUS_CODE.INFO;
        
        if(result.getLeft() != null && result.getRight() == null) {
            this.midiInfo = MidiFileInfo.fromSequence(ItemFloppyDisk.getMidiUrl(this.menu.getActiveFloppyStack()), result.getLeft());
            this.instrumentMap = MidiFileInfo.getInstrumentMapping(this.midiInfo.byteChannelMapping);
            this.loadedFloppyValid = true;
        } else {
            this.midiInfo = null;
            this.loadedFloppyValid = false;
        }
    }

    protected Boolean thisTransmitterIsActive() {
        return ((ClientProxy)MIMIMod.proxy).getMidiInput().transmitterIsActive() && ((ClientProxy)MIMIMod.proxy).getMidiInput().getActiveSlot().equals(this.menu.playerInvSlot);
    }

    @Override
    protected PoseStack renderGraphics(PoseStack matrixStack, int mouseX, int mouseY, float partialTicks) {
        // Set Texture
        RenderSystem.setShaderTexture(0, guiTexture);

        // GUI Background
        blit(matrixStack, START_X, START_Y, this.getBlitOffset(), 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
        
        // Play/Pause Button
        Boolean playing = thisTransmitterIsActive() && ((ClientProxy)MIMIMod.proxy).getMidiInput().transmitterManager.isPlaying();
        blit(matrixStack, START_X + Float.valueOf(PLAY_PAUSE_BUTTON.x()).intValue() + 1, START_Y + Float.valueOf(PLAY_PAUSE_BUTTON.y()).intValue() + 1, this.getBlitOffset(), 1 + (playing.compareTo(false) * 13), 245, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);
        
        // Transmit Screen    
        blit(matrixStack, START_X + Float.valueOf(TRANSMIT_SCREEN.x()).intValue(), START_Y + Float.valueOf(TRANSMIT_SCREEN.y()).intValue(), this.getBlitOffset(), 1 + (13 * ItemTransmitter.getTransmitMode(this.menu.getTransmitterStack()).ordinal()), 231, 13, 13, TEXTURE_SIZE, TEXTURE_SIZE);

        // Transmit Light
        if(thisTransmitterIsActive() && ((ClientProxy)MIMIMod.proxy).getMidiInput().transmitterManager.isInProgress()) {
            blit(matrixStack, START_X + Float.valueOf(TRANSMIT_LIGHT.x()).intValue(), START_Y + Float.valueOf(TRANSMIT_LIGHT.y()).intValue(), this.getBlitOffset(), 8, 225, 5, 5, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        // Time Slider
        if(this.menu.hasActiveFloppy() && this.midiInfo != null) {
            Integer slideLength = this.midiInfo.songLength;
            Integer slideProgress = thisTransmitterIsActive() && ((ClientProxy)MIMIMod.proxy).getMidiInput().transmitterManager.getPositionSeconds() != null && ((ClientProxy)MIMIMod.proxy).getMidiInput().transmitterManager.getPositionSeconds() >= 0 ? ((ClientProxy)MIMIMod.proxy).getMidiInput().transmitterManager.getPositionSeconds() : 0;
            Double slidePercentage =  Double.valueOf(slideProgress) / Double.valueOf(slideLength);
            Integer slideOffset = Double.valueOf(Math.floor(slidePercentage * SLIDE_WIDTH)).intValue();
            blit(matrixStack, START_X + SLIDE_MIN_X + slideOffset, START_Y + SLIDE_Y, this.getBlitOffset(), 0, 213, 7, 17, TEXTURE_SIZE, TEXTURE_SIZE);
        }

        return matrixStack;
    }

    @Override
    protected PoseStack renderText(PoseStack matrixStack, int mouseX, int mouseY) {
        if(this.menu.hasActiveFloppy()) {
            if(this.status != null) {
                switch(this.status) {
                    case ERROR_OTHER:
                        this.font.draw(matrixStack,"Failed to load MIDI file from Disk URL.", 7, 33, 0xFF00E600);
                        break;
                    case ERROR_URL:
                        this.font.draw(matrixStack,"Disk has invalid or inaccessible URL.", 7, 33, 0xFF00E600);
                        break;
                    case ERROR_DISABLED:
                        this.font.draw(matrixStack,"Transmitter playing web MIDI disabled by this server or your settings.", 7, 33, 0xFF00E600);
                        break;
                    case ERROR_HOST:
                        this.font.draw(matrixStack,"Disk URL is a website disallowed by this server or your settings.", 7, 33, 0xFF00E600);
                        break;
                    case ERROR_NOT_FOUND:
                        this.font.draw(matrixStack,"Disk URL points to a server MIDI file that does not exist.", 7, 33, 0xFF00E600);
                        break;
                    case INFO:
                        if(this.midiInfo != null) {
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
}
