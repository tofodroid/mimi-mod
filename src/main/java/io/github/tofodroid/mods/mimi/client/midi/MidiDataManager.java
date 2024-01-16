package io.github.tofodroid.mods.mimi.client.midi;

import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;

public class MidiDataManager {
    public static final Integer UPDATE_MUSIC_PLAYER_STATUS_EVERY_TICKS = 10;
    public final MidiInputDeviceManager inputDeviceManager;
    private ServerMusicPlayerStatusPacket playerEnderTransmitterStatusPacket;
    private Integer updateTickCount = 0;

    public MidiDataManager() {
        this.inputDeviceManager = new MidiInputDeviceManager();
        this.inputDeviceManager.open();
    }

    public void setPlayerStatusPakcet(ServerMusicPlayerStatusPacket packet) {
        if(connectedToServer()) {
            this.playerEnderTransmitterStatusPacket = packet;
        } else {
            this.playerEnderTransmitterStatusPacket = null;
        }
    }

    public Boolean hasPlayerStatus() {
        return connectedToServer() && this.playerEnderTransmitterStatusPacket != null;
    }

    public ServerMusicPlayerStatusPacket getPlayerStatusPacket() {
        if(connectedToServer()) {
            return this.playerEnderTransmitterStatusPacket;
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
            NetworkManager.INFO_CHANNEL.sendToServer(new ServerMusicPlayerStatusPacket(Minecraft.getInstance().player.getUUID()));
        } else {
            updateTickCount++;
        }
    }

    public void handlePlayerTick(Player player) {
        this.inputDeviceManager.handlePlayerTick(player);
    }

    public void handleLoginLogout() {
        playerEnderTransmitterStatusPacket = null;
        updateTickCount = 0;
    }
}
