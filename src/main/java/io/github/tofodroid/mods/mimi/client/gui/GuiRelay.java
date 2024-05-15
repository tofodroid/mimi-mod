package io.github.tofodroid.mods.mimi.client.gui;

import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import io.github.tofodroid.mods.mimi.util.Vector2Int;
import io.github.tofodroid.mods.mimi.client.gui.widget.BroadcastRangeWidget;
import io.github.tofodroid.mods.mimi.client.gui.widget.TransmitterSourceWidget;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.ConfigurableMidiTileSyncPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class GuiRelay extends BaseGui {
    // GUI
    private static final Vector2Int TRANSMIT_SOURCE_WIDGET_COORDS = new Vector2Int(178,29);
    private static final Vector2Int RANGE_WIDGET_COORDS = new Vector2Int(279,6);

    private static final Vector2Int ALL_CHANNELS_BUTTON_COORDS = new Vector2Int(110,58);
    private static final Vector2Int CLEAR_CHANNELS_BUTTON_COORDS = new Vector2Int(131,58);
    private static final Vector2Int RESET_CHANNELS_BUTTON_COORDS = new Vector2Int(152,58);

    private static final Vector2Int CHANNEL_ONE_BUTTON_COORDS = new Vector2Int(9,58);
    private static final Vector2Int CHANNEL_ONE_LIGHT_COORDS = new Vector2Int(28,64);
    private static final Vector2Int CHANNEL_ONE_SCREEN_COORDS = new Vector2Int(69,62);
    private static final Vector2Int CHANNEL_ONE_DOWN_COORDS = new Vector2Int(50,58);
    private static final Vector2Int CHANNEL_ONE_UP_COORDS = new Vector2Int(84,58);

    private static final Vector2Int CHANNEL_TWO_BUTTON_COORDS = new Vector2Int(9,77);
    private static final Vector2Int CHANNEL_TWO_LIGHT_COORDS = new Vector2Int(28,83);
    private static final Vector2Int CHANNEL_TWO_SCREEN_COORDS = new Vector2Int(69,81);
    private static final Vector2Int CHANNEL_TWO_DOWN_COORDS = new Vector2Int(50,77);
    private static final Vector2Int CHANNEL_TWO_UP_COORDS = new Vector2Int(84,77);

    private static final Vector2Int CHANNEL_ROW_COL_OFFSET = new Vector2Int(96, 19);

    // Widgets
    private TransmitterSourceWidget transmitSource;
    private BroadcastRangeWidget broadcastRange;

    // Input Data
    private final Player player;
    private final ItemStack relayStack;
    private final BlockPos tilePos;

    public GuiRelay(Player player, BlockPos tilePos, ItemStack relayStack) {
        super(300, 177, 300, "textures/gui/container_relay.png", "item.MIMIMod.gui_relay");
        this.player = player;
        this.tilePos = tilePos;

        if(relayStack == null || relayStack.isEmpty()) {
            MIMIMod.LOGGER.error("Relay stack is null or empty. Force closing GUI!");
            Minecraft.getInstance().forceSetScreen((Screen)null);
            this.relayStack = null;
            return;
        }
        this.relayStack = new ItemStack(relayStack.getItem(), relayStack.getCount());
        this.relayStack.applyComponents(relayStack.getComponents());
    }

    @Override
    public void init() {
        super.init();
        this.transmitSource = new TransmitterSourceWidget(relayStack, player.getUUID(), player.getName().getString(), new Vector2Int(START_X, START_Y), TRANSMIT_SOURCE_WIDGET_COORDS);
        this.broadcastRange = new BroadcastRangeWidget(relayStack, new Vector2Int(START_X, START_Y), RANGE_WIDGET_COORDS);
    }

    public void syncRelayToServer() {
        NetworkProxy.sendToServer(new ConfigurableMidiTileSyncPacket(relayStack, tilePos));
    }

    public Byte channelButtonClicked(int imouseX, int imouseY, Vector2Int channelOne, Vector2Int channelTwo) {
        // Channel 1
        if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(channelOne))) {
            return 0;
        }

        // Others
        for(int x = 0; x < 3; x++) {
            for(int y = 0; y < 5; y++) {
                if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(new Vector2Int(channelTwo.x + x * CHANNEL_ROW_COL_OFFSET.x, channelTwo.y + y * CHANNEL_ROW_COL_OFFSET.y)))) {
                    return Integer.valueOf(1 + x*5 + y).byteValue();
                }
            }
        }

        return null;
    }

    @Override
    public boolean mouseClicked(double dmouseX, double dmouseY, int mouseButton) {
        int imouseX = (int)Math.round(dmouseX);
        int imouseY = (int)Math.round(dmouseY);
        
        // MIDI Controls
        if(transmitSource.mouseClicked(imouseX, imouseY, mouseButton)) {
            this.syncRelayToServer();
        } else if(broadcastRange.mouseClicked(imouseX, imouseY, mouseButton)) {
            this.syncRelayToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(ALL_CHANNELS_BUTTON_COORDS))) {
            MidiNbtDataUtils.setEnableAllChannels(relayStack);
            this.syncRelayToServer();
        } else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(CLEAR_CHANNELS_BUTTON_COORDS))) {
            MidiNbtDataUtils.clearEnabledChannels(relayStack);
            this.syncRelayToServer();
        }else if(CommonGuiUtils.clickedBox(imouseX, imouseY, guiToScreenCoords(RESET_CHANNELS_BUTTON_COORDS))) {
            MidiNbtDataUtils.setChannelMap(relayStack, new Byte[]{0,1,2,3,4,5,6,7,8,9,10,11,12,13,14,15});
            this.syncRelayToServer();
        } else {
            // Channel Buttons
            Byte clickedChannel = channelButtonClicked(imouseX, imouseY, CHANNEL_ONE_BUTTON_COORDS, CHANNEL_TWO_BUTTON_COORDS);
            if(clickedChannel != null) {
                MidiNbtDataUtils.toggleChannel(relayStack, clickedChannel);
                this.syncRelayToServer();
            }

            // Down Buttons
            clickedChannel = channelButtonClicked(imouseX, imouseY, CHANNEL_ONE_DOWN_COORDS, CHANNEL_TWO_DOWN_COORDS);
            if(clickedChannel != null) {
                MidiNbtDataUtils.setChannelMap(relayStack, clickedChannel, Integer.valueOf(MidiNbtDataUtils.getChannelMap(relayStack, clickedChannel)-1).byteValue());
                this.syncRelayToServer();
            }

            // Up Buttons
            clickedChannel = channelButtonClicked(imouseX, imouseY, CHANNEL_ONE_UP_COORDS, CHANNEL_TWO_UP_COORDS);
            if(clickedChannel != null) {
                MidiNbtDataUtils.setChannelMap(relayStack, clickedChannel, Integer.valueOf(MidiNbtDataUtils.getChannelMap(relayStack, clickedChannel)+1).byteValue());
                this.syncRelayToServer();
            }
        }

        return super.mouseClicked(dmouseX, dmouseY, mouseButton);
    }

    // Render Functions
    @Override
    protected GuiGraphics renderGraphics(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        // GUI Background
        this.blitRelative(graphics, 0, 0, 0, 0, this.GUI_WIDTH, this.GUI_HEIGHT);
    
        this.transmitSource.renderGraphics(graphics, mouseX, mouseY);
        this.broadcastRange.renderGraphics(graphics, mouseX, mouseY);

        // Lights
        // Channel 1
        if(MidiNbtDataUtils.isChannelEnabled(relayStack, Integer.valueOf(0).byteValue())) {
            this.blitRelative(graphics, CHANNEL_ONE_LIGHT_COORDS.x, CHANNEL_ONE_LIGHT_COORDS.y, 0, 177, 3, 3);
        }

        // Others
        for(int x = 0; x < 3; x++) {
            for(int y = 0; y < 5; y++) {
                if(MidiNbtDataUtils.isChannelEnabled(relayStack, Integer.valueOf(1 + x*5 + y).byteValue())) {
                    this.blitRelative(graphics, CHANNEL_TWO_LIGHT_COORDS.x + x * CHANNEL_ROW_COL_OFFSET.x, CHANNEL_TWO_LIGHT_COORDS.y + y * CHANNEL_ROW_COL_OFFSET.y, 0, 177, 3, 3);
                }
            }
        }
        
        return graphics;
    }

    @Override
    protected GuiGraphics renderText(GuiGraphics graphics, int mouseX, int mouseY, float partialTicks) {
        this.transmitSource.renderText(graphics, font, mouseX, mouseY);
        this.broadcastRange.renderText(graphics, font, mouseX, mouseY);

        // Mappings
        // Channel 1
        Integer mapValue = MidiNbtDataUtils.getChannelMap(relayStack, Integer.valueOf(0).byteValue())+1;
        this.drawStringRelative(graphics, mapValue < 10 ? "0" + mapValue : mapValue.toString() , CHANNEL_ONE_SCREEN_COORDS.x, CHANNEL_ONE_SCREEN_COORDS.y, 0xFF00E600);

        // Others
        for(int x = 0; x < 3; x++) {
            for(int y = 0; y < 5; y++) {
                mapValue = MidiNbtDataUtils.getChannelMap(relayStack, Integer.valueOf(1 + x*5 + y).byteValue())+1;
                this.drawStringRelative(graphics, mapValue < 10 ? "0" + mapValue : mapValue.toString(), CHANNEL_TWO_SCREEN_COORDS.x + x * CHANNEL_ROW_COL_OFFSET.x, CHANNEL_TWO_SCREEN_COORDS.y + y * CHANNEL_ROW_COL_OFFSET.y, 0xFF00E600);
            }
        }

        return graphics;
    }
}