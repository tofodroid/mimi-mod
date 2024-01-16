package io.github.tofodroid.mods.mimi.client;

import io.github.tofodroid.mods.mimi.client.midi.MidiDataManager;
import io.github.tofodroid.mods.mimi.client.midi.synth.MidiMultiSynthManager;
import io.github.tofodroid.mods.mimi.common.Proxy;
import io.github.tofodroid.mods.mimi.common.midi.FilesystemMidiFileProvider;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;
import io.github.tofodroid.mods.mimi.common.network.ServerTimeSyncPacket;
import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;

public class ClientProxy implements Proxy {
    private MidiMultiSynthManager MIDI_SYNTH;
    private MidiDataManager MIDI_DATA;
    private FilesystemMidiFileProvider CLIENT_MIDI_FILES = new FilesystemMidiFileProvider(false);
    
    // In singleplayer this proxy is used on server side, so we need both providers
    private FilesystemMidiFileProvider SERVER_MIDI_FILES = new FilesystemMidiFileProvider(true);
    private Long startSyncEpoch = null;
    private Long serverStartEpoch = 0l;

    @Override
    public void init() {
        MIDI_SYNTH = new MidiMultiSynthManager();        
        MIDI_DATA = new MidiDataManager();
    }

    @Override
    public Boolean isClient() {
        return true;
    }

    @Override
    public Long getServerStartEpoch() {
        return serverStartEpoch;
    }

    @Override
    public FilesystemMidiFileProvider clientMidiFiles() {
        return CLIENT_MIDI_FILES;
    }

    // In singleplayer this proxy is used on server side, so we need both providers
    @Override
    public FilesystemMidiFileProvider serverMidiFiles() {
        return SERVER_MIDI_FILES;
    }

    public MidiMultiSynthManager getMidiSynth() {
        return MIDI_SYNTH;
    }

    public MidiDataManager getMidiData() {
        return MIDI_DATA;
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
                this.startSyncEpoch = null;
            }
        }
    }
}
