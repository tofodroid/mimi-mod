package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.util.Vector2Int;

import io.github.tofodroid.mods.mimi.client.gui.widget.MidiChannelToggleWidget;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.ConfigurableMidiTileSyncPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

public class GuiConductor extends BaseGui {
    // GUI
    private static final Vector2Int MIDI_CHANNEL_WIDGET_COORDS = new Vector2Int(6,26);
    private static final Vector2Int NOTE_LETTER_BUTTON_COORDS = new Vector2Int(181,41);
    private static final Vector2Int NOTE_OCTAVE_BUTTON_COORDS = new Vector2Int(203,41);

    // Widgets
    private MidiChannelToggleWidget midiChannelToggle;

    // Input Data
    private final ItemStack conductorStack;
    private final BlockPos tilePos;

    // Runtime Data
    private Byte broadcastNote;

    public GuiConductor(BlockPos tilePos, ItemStack conductorStack) {
        super(272, 99, 272, "textures/gui/container_conductor.png", "item.MIMIMod.gui_conductor");
        this.tilePos = tilePos;

        if(conductorStack == null || conductorStack.isEmpty()) {
            MIMIMod.LOGGER.error("Conductor stack is null or empty. Force closing GUI!");
            Minecraft.getInstance().forceSetScreen((Screen)null);
            this.conductorStack = null;
            return;
        }
        this.conductorStack = new ItemStack(conductorStack.getItem(), conductorStack.getCount());
        this.conductorStack.setTag(conductorStack.getOrCreateTag().copy());
    }

    @Override
    public void init() {
        super.init();
        this.broadcastNote = MidiNbtDataUtils.getBroadcastNote(conductorStack);
        this.midiChannelToggle = new MidiChannelToggleWidget(conductorStack, new Vector2Int(START_X, START_Y), MIDI_CHANNEL_WIDGET_COORDS);
    }

    public void syncConductorToServer() {
        NetworkProxy.sendToServer(new ConfigurableMidiTileSyncPacket(conductorStack, tilePos));
    }

    @Override
    public boolean mouseClicked(double dmouseX, double dmouseY, int mouseButton) {
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);
        
        // MIDI Controls
        if(midiChannelToggle.mouseClicked(imouseX, imouseY, mouseButton)) {
            this.syncConductorToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(NOTE_LETTER_BUTTON_COORDS))) {
            this.shiftBroadcastNoteLetter();
            this.syncConductorToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(NOTE_OCTAVE_BUTTON_COORDS))) {
            this.shiftBroadcastNoteOctave();
            this.syncConductorToServer();
        }

        return super.mouseClicked(dmouseX, dmouseY, mouseButton);
    }

    // Render Functions
    @Override
    protected GuiGraphics renderGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // GUI Background
        this.blitAbsolute(graphics, guiTexture, START_X, START_Y, 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT, TEXTURE_SIZE, TEXTURE_SIZE);
    
        this.midiChannelToggle.renderGraphics(graphics, mouseX, mouseY);
        
        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // Broadcast note
        this.drawStringAbsolute(graphics, font, MidiNbtDataUtils.getMidiNoteAsString(broadcastNote), START_X + 224, START_Y + 45,0xFF00E600);

        this.midiChannelToggle.renderText(graphics, font, mouseX, mouseY);

        return graphics;
    }
        
    protected void shiftBroadcastNoteLetter() {
        if(broadcastNote % 12 < 11) {
            if(broadcastNote + 1 <= Byte.MAX_VALUE) {
                broadcastNote++;
            } else {
                broadcastNote = Integer.valueOf(broadcastNote - (broadcastNote % 12)).byteValue();
            }
        } else {
            broadcastNote = Integer.valueOf(broadcastNote - 11).byteValue();
        }
        MidiNbtDataUtils.setBroadcastNote(conductorStack, broadcastNote);
    }
    
    protected void shiftBroadcastNoteOctave() {
        if((broadcastNote / 12)  < 10) {
            if(broadcastNote + 12 <= Byte.MAX_VALUE) {
                broadcastNote = Integer.valueOf(broadcastNote + 12).byteValue();
            } else {
                broadcastNote = Integer.valueOf(broadcastNote + - 108).byteValue();
            }
        } else {
            broadcastNote = Integer.valueOf(broadcastNote - 120).byteValue();
        }
        MidiNbtDataUtils.setBroadcastNote(conductorStack, broadcastNote);
    }
}