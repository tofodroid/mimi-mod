package io.github.tofodroid.mods.mimi.client;

import io.github.tofodroid.mods.mimi.client.midi.MidiDataManager;
import io.github.tofodroid.mods.mimi.client.midi.synth.MidiMultiSynthManager;
import io.github.tofodroid.mods.mimi.common.Proxy;
import io.github.tofodroid.mods.mimi.common.midi.FilesystemMidiFileProvider;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import io.github.tofodroid.mods.mimi.common.network.ServerTimeSyncPacket;
import io.github.tofodroid.mods.mimi.util.TimeUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.world.entity.player.Player;

public class ClientProxy implements Proxy {
    private MidiMultiSynthManager MIDI_SYNTH;
    private MidiDataManager MIDI_DATA;
    private FilesystemMidiFileProvider CLIENT_MIDI_FILES = new FilesystemMidiFileProvider(false, 0);
    
    // In singleplayer this proxy is used on server side, so we need both providers
    private FilesystemMidiFileProvider SERVER_MIDI_FILES = new FilesystemMidiFileProvider(true, 1);
    private Long startSyncEpoch = null;
    private Long serverStartEpoch = 0l;
    private Boolean initialized = false;

    @Override
    public void init() {
        MIDI_SYNTH = new MidiMultiSynthManager();        
        MIDI_DATA = new MidiDataManager();
        CLIENT_MIDI_FILES.refresh(true);
        SERVER_MIDI_FILES.refresh(true);
        this.initialized = true;
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
                this.startSyncEpoch = TimeUtils.getNowTime();
                NetworkProxy.sendToServer(new ServerTimeSyncPacket(0l, false));
            } else {
                this.serverStartEpoch = TimeUtils.getNowTime() - message.currentServerMilli - ((TimeUtils.getNowTime() - this.startSyncEpoch)/2);
                this.startSyncEpoch = null;
            }
        }
    }

    @Override
    public Boolean isInitialized() {
        return this.initialized;
    }

    public void onLocalPlayerLogout() {
        if(this.isInitialized()) {
            this.getMidiData().handleLoginLogout();
            this.getMidiSynth().handleLogout();
        }
    }

    public void onLocalPlayerLogin() {
        if(this.isInitialized()) {
            this.getMidiData().handleLoginLogout();
            this.getMidiSynth().handleLogin();
        }
    }

    public void onClientTick() {
        if(this.isInitialized()) {
            this.getMidiData().handleClientTick();
            this.getMidiSynth().handleClientTick();
        }
    }

    public void onPlayerTick(Player player) {
        if(this.isInitialized()) {
            this.getMidiData().handlePlayerTick(player);
            this.getMidiSynth().handlePlayerTick(player);
        }
    }
}
