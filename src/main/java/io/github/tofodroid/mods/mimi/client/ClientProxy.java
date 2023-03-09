package io.github.tofodroid.mods.mimi.client;

import io.github.tofodroid.mods.mimi.client.midi.MidiInputManager;
import io.github.tofodroid.mods.mimi.client.midi.synth.MidiMultiSynthManager;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.Proxy;
import io.github.tofodroid.mods.mimi.common.keybind.ModBindings;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerTimeSyncPacket;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ClientProxy implements Proxy {
    private MidiMultiSynthManager MIDI_SYNTH;
    private MidiInputManager MIDI_INPUT;
    private Long startSyncEpoch = null;
    private Long serverStartEpoch = 0l;

    @Override
    public void init(final FMLCommonSetupEvent event) {
        // MIDI
        MIDI_SYNTH = new MidiMultiSynthManager();
        MinecraftForge.EVENT_BUS.register(MIDI_SYNTH);
        
        MIDI_INPUT = new MidiInputManager();
        MinecraftForge.EVENT_BUS.register(MIDI_INPUT);
        
        // Keybinds
        ModBindings.register();
    }

    public MidiMultiSynthManager getMidiSynth() {
        return MIDI_SYNTH;
    }

    public MidiInputManager getMidiInput() {
        return MIDI_INPUT;
    }
    
    public void handleTimeSyncPacket(ServerTimeSyncPacket message) {
        ServerData currentServer = Minecraft.getInstance().getCurrentServer();
        if(currentServer != null) {
            // Ignore the first response and do it again because initial login to server adds a lot of latency
            if(this.startSyncEpoch == null) {
                this.startSyncEpoch = Util.getEpochMillis();
                NetworkManager.INFO_CHANNEL.sendToServer(new ServerTimeSyncPacket(0l, false));
            } else {
                this.serverStartEpoch = Util.getEpochMillis() - message.currentServerMilli - ((Util.getEpochMillis() - this.startSyncEpoch)/2);
                MIMIMod.LOGGER.info(
                    "TIME SYNC CLIENT:\n\tServer Response Current Millis: " + message.currentServerMilli
                    + "\n\tReceive Millis: " + Util.getEpochMillis()
                    + "\n\tLatency Millis: " + (Util.getEpochMillis() - this.startSyncEpoch)
                    + "\n\tLatency-Adjusted Server Current Millis: " + (message.currentServerMilli - ((Util.getEpochMillis() - this.startSyncEpoch))/2)
                    + "\n\tClient Current Millis: " + Util.getEpochMillis()
                    + "\n\tCalculated Server Start Millis: " + this.serverStartEpoch
                );
                this.startSyncEpoch = null;
            }
        }
    }

    @Override
    public Boolean isClient() {
        return true;
    }

    @Override
    public Long getServerStartEpoch() {
        return serverStartEpoch;
    }
}
