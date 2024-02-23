package io.github.tofodroid.mods.mimi.client.midi;

import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class MidiDataManager {
    public static final Integer UPDATE_MUSIC_PLAYER_STATUS_EVERY_TICKS = 10;
    public final MidiInputDeviceManager inputDeviceManager;
    private ServerMusicPlayerStatusPacket playerTransmitterStatusPacket;
    private Integer updateTickCount = 0;

    public MidiDataManager() {
        this.inputDeviceManager = new MidiInputDeviceManager();
        this.inputDeviceManager.open();
    }

    public void setPlayerStatusPakcet(ServerMusicPlayerStatusPacket packet) {
        if(connectedToServer()) {
            this.playerTransmitterStatusPacket = packet;
        } else {
            this.playerTransmitterStatusPacket = null;
        }
    }

    public Boolean hasPlayerStatus() {
        return connectedToServer() && this.playerTransmitterStatusPacket != null;
    }

    public ServerMusicPlayerStatusPacket getPlayerStatusPacket() {
        if(connectedToServer()) {
            return this.playerTransmitterStatusPacket;
        }
        return null;
    }

    @SuppressWarnings("resource")
    public Boolean connectedToServer() {
        return Minecraft.getInstance().player != null && (Minecraft.getInstance().hasSingleplayerServer() || Minecraft.getInstance().getCurrentServer() != null);
    }

    @SuppressWarnings("resource")
    public void handleClientTick() {
        if(!connectedToServer()) return;

        if(updateTickCount >= UPDATE_MUSIC_PLAYER_STATUS_EVERY_TICKS) {
            updateTickCount = 0;
            NetworkProxy.sendToServer(new ServerMusicPlayerStatusPacket(Minecraft.getInstance().player.getUUID()));
        } else {
            updateTickCount++;
        }
    }

    public void handlePlayerTick(Player player) {
        this.inputDeviceManager.handlePlayerTick(player);
    }

    public void handleLoginLogout() {
        playerTransmitterStatusPacket = null;
        updateTickCount = 0;
    }
}
