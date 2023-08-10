package io.github.tofodroid.mods.mimi.client.gui;

import java.util.List;
import java.util.stream.Collectors;

import org.joml.Vector2i;

import com.mojang.blaze3d.systems.RenderSystem;

import io.github.tofodroid.mods.mimi.client.gui.widget.NoteFilterWidget;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GuiListener extends BaseGui {
    // GUI
    private static final Vector2i NOTE_FILTER_WIDGET_COORDS = new Vector2i(180,26);
    private static final Vector2i FILTER_INSTRUMENT_PREV_BUTTON_COORDS = new Vector2i(9,40);
    private static final Vector2i FILTER_INSTRUMENT_NEXT_BUTTON_COORDS = new Vector2i(143,40);
    private static final Vector2i FILTER_INSTRUMENT_INVERT_BUTTON_COORDS = new Vector2i(161,40);

    // Widgets
    private NoteFilterWidget noteFilter;

    // Input Data
    protected List<Byte> INSTRUMENT_ID_LIST;
    protected Integer filterInstrumentIndex = 0;
    private final ItemStack listenerStack;

    public GuiListener(Player player, ItemStack listenerStack) {
        super(302, 64, 302, "textures/gui/container_listener.png", "item.MIMIMod.gui_listener");

        if(listenerStack == null || listenerStack.isEmpty()) {
            MIMIMod.LOGGER.error("Listener stack is null or empty. Force closing GUI!");
            Minecraft.getInstance().forceSetScreen((Screen)null);
            this.listenerStack = null;
            return;
        }
        
        this.listenerStack = new ItemStack(listenerStack.getItem(), listenerStack.getCount());
        this.listenerStack.setTag(listenerStack.getOrCreateTag().copy());
    }

    @Override
    public void init() {
        super.init();
        this.noteFilter = new NoteFilterWidget(listenerStack, new Vector2i(START_X, START_Y), NOTE_FILTER_WIDGET_COORDS);
    }

    public void syncListenerToServer() {
        //NetworkManager.INFO_CHANNEL.sendToServer(new SyncInstrumentPacket(instrumentStack, this.handIn));
    }

    @Override
    public boolean mouseClicked(double dmouseX, double dmouseY, int mouseButton) {
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);
        
        // MIDI Controls
        if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(FILTER_INSTRUMENT_PREV_BUTTON_COORDS))) {
            this.shiftInstrumentId(false);
            this.syncListenerToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(FILTER_INSTRUMENT_NEXT_BUTTON_COORDS))) {
            this.shiftInstrumentId(true);
            this.syncListenerToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(FILTER_INSTRUMENT_INVERT_BUTTON_COORDS))) {
            InstrumentDataUtils.setInvertInstrument(listenerStack, !InstrumentDataUtils.getInvertInstrument(listenerStack));
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

        if(InstrumentDataUtils.getInvertInstrument(listenerStack)) {
            graphics.blit(guiTexture, START_X + 167, START_Y + 34, 0, 64, 3, 3, TEXTURE_SIZE, TEXTURE_SIZE);
        }
    
        this.noteFilter.renderGraphics(graphics, mouseX, mouseY);
        
        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        graphics.drawString(font, InstrumentDataUtils.getInstrumentName(InstrumentDataUtils.getFilterInstrument(listenerStack)), START_X + 29, START_Y + 44, 0xFF00E600);
        this.noteFilter.renderText(graphics, font, mouseX, mouseY);
        return graphics;
    }

    public List<Byte> INSTRUMENT_ID_LIST() {
        if(this.INSTRUMENT_ID_LIST == null) {
            this.INSTRUMENT_ID_LIST = InstrumentDataUtils.INSTRUMENT_NAME_MAP().keySet().stream().sorted().collect(Collectors.toList());
        }
        return this.INSTRUMENT_ID_LIST;
    }
	
    public void shiftInstrumentId(Boolean up) {
        if(up) {
            if(filterInstrumentIndex < INSTRUMENT_ID_LIST().size()-1) {
                filterInstrumentIndex++;
            } else {
                filterInstrumentIndex = 0;
            }
        } else {
            if(filterInstrumentIndex > 0) {
                filterInstrumentIndex--;
            } else {
                filterInstrumentIndex = INSTRUMENT_ID_LIST().size()-1;
            }
        }
        
        InstrumentDataUtils.setFilterInstrument(listenerStack, INSTRUMENT_ID_LIST().get(filterInstrumentIndex));
    }
}