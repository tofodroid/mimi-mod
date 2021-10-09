package io.github.tofodroid.mods.mimi.client.midi;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MidiChannel;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Soundbank;
import javax.sound.midi.Synthesizer;
import javax.sound.sampled.AudioFormat;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;
import com.google.gson.Gson;

import io.github.tofodroid.mods.mimi.common.midi.MidiChannelNumber;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.common.midi.AMidiSynthManager;
import io.github.tofodroid.mods.mimi.common.network.MidiNotePacket;
import io.github.tofodroid.mods.mimi.common.container.ContainerInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent.PlayerLoggedOutEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

public class MidiSynthManager extends AMidiSynthManager {
    private static final Integer MIDI_TICK_FREQUENCY = 1;
    private static final Double BLOCK_MIDI_REDUCTION = 0.5;
    private ImmutableList<MidiChannelDef> midiChannelSet;
    private BiMap<MidiChannelNumber,String> channelAssignmentMap;
    
    protected Soundbank soundbank;
    protected Synthesizer midiSynth; 
    protected Receiver midiReceiver;
    protected Integer midiTickCounter = 0;

    public MidiSynthManager() {
        init();
    }

    public void init() {
        this.soundbank = openSoundbank(ModConfigs.CLIENT.soundfontPath.get());
        this.midiSynth = openNewSynth(soundbank);
        this.midiReceiver = openNewReceiver(midiSynth);
        this.channelAssignmentMap = HashBiMap.create();

        if(midiSynth != null && midiReceiver != null) {
            // Setup channel map
            Builder<MidiChannelDef> builder = ImmutableList.builder();
            for(MidiChannelNumber num : MidiChannelNumber.values()) {
                // Don't use channel nine because it's only for percussion
                if(num != MidiChannelNumber.NINE) {
                    builder.add(new MidiChannelDef(num.ordinal(), this.midiSynth.getChannels()[num.ordinal()]));
                }
            }
            this.midiChannelSet = builder.build();
        } else {
            this.close();
        }
    }

    @Override
    public void close() {
        // Close Midi
        if(midiReceiver != null) {
            midiReceiver.close();
            midiReceiver = null;
        }

        if(midiSynth != null && midiSynth.isOpen()) {
            midiSynth.close();
            midiSynth = null;
        }

        this.soundbank = null;
        this.midiChannelSet = ImmutableList.of();
        this.channelAssignmentMap = HashBiMap.create();
    }

    public void handlePacket(MidiNotePacket message) {
        // Play
        if(message.velocity > 0) {
            noteOn(message);
        } else {
            noteOff(message);
        }
    }
    
    @SuppressWarnings("resource")
    public Boolean shouldShowOnGUI(UUID messagePlayer, Byte channel, Byte instrument) {
        ClientPlayerEntity thisPlayer = Minecraft.getInstance().player;
    
        if(messagePlayer.equals(thisPlayer.getUniqueID()) && thisPlayer.openContainer instanceof ContainerInstrument) {
            ItemStack switchStack = ((ContainerInstrument)thisPlayer.openContainer).getSelectedSwitchboard();
            Byte guiInstrument = ((ContainerInstrument)thisPlayer.openContainer).getInstrumentId();

            if(instrument == guiInstrument && ModItems.SWITCHBOARD.equals(switchStack.getItem())) {
                UUID midiSource = ItemMidiSwitchboard.getMidiSource(switchStack);
                
                if((messagePlayer.equals(midiSource) || ItemMidiSwitchboard.PUBLIC_SOURCE_ID.equals(midiSource)) && ItemMidiSwitchboard.isChannelEnabled(switchStack, channel)) {
                    return true;
                }             
            }
        }

        return false;
    }

    public void allNotesOff() {
        if(midiSynth != null && midiReceiver != null) {
            for(MidiChannel channel : this.midiSynth.getChannels()) {
                channel.allNotesOff();
            }
        }
    }
    
    public void allNotesOff(MidiChannelNumber num) {
        if(midiSynth != null && midiReceiver != null && num != null) {
            this.midiChannelSet.get(num.ordinal()).noteOff(MidiNotePacket.ALL_NOTES_OFF);
        }
    }

    @SubscribeEvent
    public void handleTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT || !event.player.isUser()) {
            return;
        }

        midiTickCounter++;

        if(midiTickCounter >= MIDI_TICK_FREQUENCY) {
            List<MidiChannelNumber> toUnassign = new ArrayList<>();

            // 1. Tick channels
            channelAssignmentMap.entrySet().forEach(entry -> {
                if(Boolean.FALSE.equals(midiChannelSet.get(entry.getKey().ordinal()).tick(event.player))) {
                    toUnassign.add(entry.getKey());
                }
            });

            // 2. Clear idle channels
            toUnassign.forEach(channel -> {
                midiChannelSet.get(channel.ordinal()).reset();
                channelAssignmentMap.remove(channel);
            });

            midiTickCounter = 0;
        }
    }

    @SubscribeEvent
    public void handleSelfLogOut(LoggedOutEvent event) {
        if(event.getPlayer() != null && event.getPlayer().isUser()) {
            this.allNotesOff();
        }
    }
    
    @SubscribeEvent
    public void handleOtherLogOut(PlayerLoggedOutEvent event) {
        if(!event.getPlayer().isUser()) {
            List<MidiChannelNumber> channelsToStop = getAllChannelsForPlayer(event.getPlayer().getUniqueID());
            for(MidiChannelNumber num : channelsToStop) {
                this.allNotesOff(num);
            }
        }
    }
    
    protected List<MidiChannelNumber> getAllChannelsForPlayer(UUID playerId) {
        List<MidiChannelNumber> result = new ArrayList<>();

        for(String name : channelAssignmentMap.values()) {
            if(name.contains(playerId.toString() + "-")) {
                result.add(channelAssignmentMap.inverse().get(name));
            }
        }

        return result;
    }

    protected MidiChannelNumber getChannelForPlayer(UUID playerId, Boolean mechanical, Byte instrumentId, Boolean getNew) {
        if(playerId == null) {
            return null;
        }

        String channelIdentifier = getChannelIdentifier(playerId, instrumentId);

        // Find player channel
        MidiChannelNumber assignedChannelNum = channelAssignmentMap.inverse().get(channelIdentifier);

        if(assignedChannelNum != null) {
            return assignedChannelNum;
        } else if(getNew) {
            for(MidiChannelNumber num : MidiChannelNumber.values()) {
                if(channelAssignmentMap.get(num) == null) {
                    channelAssignmentMap.put(num, channelIdentifier);
                    this.midiChannelSet.get(num.ordinal()).assign(playerId, mechanical, InstrumentConfig.getBydId(instrumentId));
                    return num;
                }
            }
        }

        return null;        
    }

    protected String getChannelIdentifier(UUID playerId, Byte instrumentId) {
        return playerId.toString() + "-" + instrumentId.toString();
    }

    @SuppressWarnings("resource")
    protected final void noteOn(MidiNotePacket message) {
        InstrumentSpec instrument = InstrumentConfig.getBydId(message.instrumentId);

        if(midiSynth == null || midiReceiver == null || instrument == null || message.velocity <= 0) {
            return;
        }

        MidiChannelNumber channelNumber = getChannelForPlayer(message.player, message.mechanical, message.instrumentId, true);
        MidiChannelDef channelDef = channelNumber != null ? this.midiChannelSet.get(channelNumber.ordinal()) : null;
        Double modifiedVelocity = new Double(message.velocity);

        if(ModConfigs.CLIENT.raytraceSound.get()) {
            Vector3d messageVec = new Vector3d(message.pos.getX() + 0.5, message.pos.getY() + 0.5, message.pos.getZ() + 0.5);
            Vector3d playerVec = new Vector3d(Minecraft.getInstance().player.getPosX(),Minecraft.getInstance().player.getPosYEye(),Minecraft.getInstance().player.getPosZ());
            Vector3d rayVec = Minecraft.getInstance().world.rayTraceBlocks(new RayTraceContext(playerVec, messageVec, BlockMode.VISUAL, FluidMode.NONE, null)).getHitVec();

            if(messageVec.distanceTo(playerVec) - rayVec.distanceTo(playerVec) >= 1) {
                modifiedVelocity *= BLOCK_MIDI_REDUCTION;
                MIMIMod.LOGGER.info("Muffling!");
            }
        }

        if(modifiedVelocity.intValue() > 0) {
            if(channelDef != null) channelDef.noteOn(instrument, message.note, new Integer(modifiedVelocity.intValue()).byteValue(), message.pos);
        }
    }

    protected final void noteOff(MidiNotePacket message) {
        if(midiSynth == null || midiReceiver == null) {
            return;
        }

        MidiChannelNumber channelNumber = getChannelForPlayer(message.player, message.mechanical, message.instrumentId, false);
        MidiChannelDef channelDef = channelNumber != null ? this.midiChannelSet.get(channelNumber.ordinal()) : null;

        if(channelDef != null) channelDef.noteOff(message.note);
    }

    protected Synthesizer openNewSynth(Soundbank sounds) {
        try {
            Synthesizer midiSynth = MidiSystem.getSynthesizer();

            if(midiSynth.getMaxReceivers() != 0) {
                midiSynth.open();
                midiSynth.close();
                
                if (midiSynth instanceof com.sun.media.sound.SoftSynthesizer) {
                    Map<String, Object> params = new HashMap<>();
                    params.put("jitter correction", ModConfigs.CLIENT.jitterCorrection.get());
                    params.put("latency", ModConfigs.CLIENT.latency.get() * 1000);
                    params.put("format", new AudioFormat(
                        ModConfigs.CLIENT.synthSampleRate.get(), 
                        ModConfigs.CLIENT.synthBitRate.get(), 
                        2, true, false
                    ));

                    MIMIMod.LOGGER.debug("Applying Gervill settings: " + new Gson().toJson(params));

                    ((com.sun.media.sound.SoftSynthesizer) midiSynth).open(null, params);
                } else {
                    MIMIMod.LOGGER.warn("Synthesizer is not Gervill. Ignoring synth settings.");
                    midiSynth.open();
                }

                midiSynth.open();
                
                if(sounds != null && midiSynth.isSoundbankSupported(sounds)) {
                    midiSynth.loadAllInstruments(sounds);
                } else {
                    MIMIMod.LOGGER.warn("Synthesizer could not load Soundbank. Falling back to default.");
                }
                
                return midiSynth;
            }

            throw new MidiUnavailableException("Midi Synth '" + midiSynth.getDeviceInfo().getName() + "' cannot support any receivers.");
        } catch(MidiUnavailableException e) {
            MIMIMod.LOGGER.error(e);
        }

        return null;
    }

    protected Receiver openNewReceiver(Synthesizer synth) {
        if(synth != null) {
            try {
                return synth.getReceiver();
            } catch(MidiUnavailableException e) {
                MIMIMod.LOGGER.error(e);
            }
        }        

        return null;
    }

    protected Soundbank openSoundbank(String resourcePath) {
        if(resourcePath != null && !resourcePath.trim().isEmpty()) {
            try {
                return MidiSystem.getSoundbank(new BufferedInputStream(new FileInputStream(new File(resourcePath.trim()))));
            } catch(NullPointerException | IOException | InvalidMidiDataException e) {
                MIMIMod.LOGGER.error("Failed to load SoundFont. Error: ", e);
            }
        }

        return null;
    }
}
