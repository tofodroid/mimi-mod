package io.github.tofodroid.mods.mimi.client.midi;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacket;
import net.minecraft.client.Minecraft;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.ClientTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MidiDataManager {
    public static final Integer UPDATE_MUSIC_PLAYER_STATUS_EVERY_TICKS = 10;
    public final MidiInputDeviceManager inputDeviceManager;
    private ServerMusicPlayerStatusPacket playerEnderTransmitterStatusPacket;
    private Integer updateTickCount = 0;

    public MidiDataManager() {
        this.inputDeviceManager = new MidiInputDeviceManager();
        MinecraftForge.EVENT_BUS.register(this.inputDeviceManager);
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

    @SubscribeEvent
    @SuppressWarnings({"null", "resource"})
    public void handleTick(ClientTickEvent event) {
        if(!connectedToServer()) return;

        if(updateTickCount >= UPDATE_MUSIC_PLAYER_STATUS_EVERY_TICKS) {
            updateTickCount = 0;
            NetworkManager.INFO_CHANNEL.sendToServer(new ServerMusicPlayerStatusPacket(Minecraft.getInstance().player.getUUID()));
        } else {
            updateTickCount++;
        }
    }

    @SubscribeEvent
    public void handleSelfLogOut(LoggingOut event) {
        playerEnderTransmitterStatusPacket = null;
        updateTickCount = 0;
    }

    @SubscribeEvent
    public void handleSelfLogin(LoggingIn event) {
        playerEnderTransmitterStatusPacket = null;
        updateTickCount = 0;
    }

}
