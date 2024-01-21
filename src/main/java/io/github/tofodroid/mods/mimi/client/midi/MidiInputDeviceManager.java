package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentHandheld;
import io.github.tofodroid.mods.mimi.common.midi.AMidiInputSourceManager;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.forge.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MidiInputDeviceManager extends AMidiInputSourceManager {
    private List<Pair<InteractionHand, ItemStack>> localInstrumentsToPlay = new ArrayList<>();
    private String selectedDeviceName;
    private String midiDeviceError;
    private Boolean dirty = false;

    public MidiInputDeviceManager() {
        selectedDeviceName = ModConfigs.CLIENT.selectedMidiDevice.get();
    }

    public Boolean isDeviceSelected() {
        return this.selectedDeviceName != null && !this.selectedDeviceName.trim().isEmpty();
    }

    public Boolean isDeviceAvailable() {
        return this.activeTransmitter != null;
    }

    public Boolean isDirtyStatus() {
        return this.dirty;
    }

    public String getSelectedDeviceName() {
        return selectedDeviceName;
    }

    public String getSelectedDeviceError() {
        return midiDeviceError;
    }

    public void saveDeviceSelection(MidiDevice device) {
        ModConfigs.CLIENT.selectedMidiDevice.set(device.getDeviceInfo().getName());
        dirty = true;
    }

    public void clearDeviceSelection() {
        ModConfigs.CLIENT.selectedMidiDevice.set("");
        dirty = true;
    }

    public List<MidiDevice> getAvailableDevices() {
        List<MidiDevice> devices = new ArrayList<>();

        // Devices
        for (int i = 0; i < MidiSystem.getMidiDeviceInfo().length; i++) {
            try {
                devices.add(MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[i]));
            } catch (MidiUnavailableException e) {
                midiDeviceError = e.getMessage();
                MIMIMod.LOGGER.warn("Midi Device Error. Device will be skipped. Error: ", e);
            }
        }

        if(!devices.isEmpty()) {
            devices = devices.stream()
                .filter(d -> d.getMaxTransmitters() != 0)
                .filter(d -> !d.getClass().getName().contains("com.sun.media.sound.RealTimeSequencer"))
                .collect(Collectors.toList());
        }

        return devices;
    }

    public void handlePlayerTick(Player player) {
        if(!player.isLocalPlayer()) {
            return;
        }
        this.localInstrumentsToPlay = localInstrumentsToPlay(player);
    }
    
    protected List<Pair<InteractionHand, ItemStack>> localInstrumentsToPlay(Player player) {
        List<Pair<InteractionHand, ItemStack>> result = new ArrayList<>();

        // Check for seated instrument
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(player);
        if(instrumentEntity != null) {
            result.add(Pair.of(null, instrumentEntity.getInstrumentStack()));
        }

        // Check for held instruments
        ItemStack mainHand = ItemInstrumentHandheld.getEntityHeldInstrumentStack(player, InteractionHand.MAIN_HAND);
        if(mainHand != null) {
            result.add(Pair.of(InteractionHand.MAIN_HAND, mainHand));
        }

        ItemStack offHand = ItemInstrumentHandheld.getEntityHeldInstrumentStack(player, InteractionHand.OFF_HAND);
        if(offHand != null) {
            result.add(Pair.of(InteractionHand.OFF_HAND, offHand));
        }

        return result;
    }
    
    public List<Pair<InteractionHand, ItemStack>> getLocalInstrumentsForMidiDevice(Player player, Byte channel) {
        return localInstrumentsToPlay.stream()
            .filter(instrumentStack -> InstrumentDataUtils.getSysInput(instrumentStack.getRight()) && InstrumentDataUtils.isChannelEnabled(instrumentStack.getRight(), channel))
            .collect(Collectors.toList());
    }

    protected void openTransmitter() {
        if(isDeviceSelected()) {
            try {
                String oldVal = System.setProperty("javax.sound.midi.Transmitter", "#" + selectedDeviceName);

                activeTransmitter = MidiSystem.getTransmitter();
                activeTransmitter.setReceiver(new MidiDeviceInputReceiver());

                if(oldVal != null) {
                    System.setProperty("javax.sound.midi.Transmitter", oldVal);
                } else {
                    System.clearProperty("javax.sound.midi.Transmitter");
                }
            } catch(Exception e) {
                MIMIMod.LOGGER.error("Midi Device Error: ", e);
                close();
            }
        }
    }

    public void open() {
        if(this.activeTransmitter == null) {
            this.openTransmitter();
        }
    }
}
