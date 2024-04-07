package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentHandheld;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MidiInputDeviceManager {
    private List<Pair<InteractionHand, ItemStack>> localInstrumentsToPlay = new ArrayList<>();

    protected Receiver activeReceiver = null;
    protected Transmitter activeTransmitter = null;
    protected MidiDevice activeDevice = null;

    private String selectedDeviceName = "";
    private String midiDeviceError = null;

    public MidiInputDeviceManager() {
        // Configure shutdown hook
        final MidiInputDeviceManager self = this;
        Runtime.getRuntime().addShutdownHook(new Thread() {
            public void run() {
                MIMIMod.LOGGER.info("Closing selected MIDI device...");
                ExecutorService ex = Executors.newSingleThreadExecutor();
                Future<?> future = ex.submit(() -> self.close());
                try {
                    future.get(10000, TimeUnit.MILLISECONDS);
                    ex.shutdownNow();
                    ex = null;
                    this.interrupt();
                } catch (Exception e){
                    MIMIMod.LOGGER.error("Java ran into an error closing the selected MIDI device. Error: " + e.getMessage());
                    if(!future.isDone()) {
                        future.cancel(true); //this method will stop the running underlying task
                    }
                    try {
                        ex.shutdownNow();
                        ex = null;
                        this.interrupt();
                    } catch(Exception e2) {
                        MIMIMod.LOGGER.error("Failed to stop executor: " + e2.getMessage());
                        ex = null;
                        this.interrupt();
                    }
                }
            }
        });
    }

    public Boolean isDeviceError() {
        return midiDeviceError != null;
    }

    public Boolean isDeviceSelected() {
        return this.selectedDeviceName != null && !this.selectedDeviceName.trim().isEmpty();
    }

    public Boolean isDeviceAvailable() {
        return this.activeTransmitter != null;
    }

    public String getSelectedDeviceName() {
        return selectedDeviceName;
    }

    public String getSelectedDeviceError() {
        return midiDeviceError;
    }

    public void saveDeviceSelection(MidiDevice device) {
        if(this.activeTransmitter == null) {
            selectedDeviceName = device.getDeviceInfo().getName();
            this.open();
        }
    }

    public List<MidiDevice> getAvailableDevices() {
        List<MidiDevice> devices = new ArrayList<>();

        // Devices
        for (int i = 0; i < MidiSystem.getMidiDeviceInfo().length; i++) {
            try {
                devices.add(MidiSystem.getMidiDevice(MidiSystem.getMidiDeviceInfo()[i]));
            } catch (MidiUnavailableException e) {
                MIMIMod.LOGGER.warn("Midi Device Error. Device will be skipped. Error: ", e);
            }
        }

        MIMIMod.LOGGER.info("Detected MIDI Input Devices: ");
        if(!devices.isEmpty()) {
            devices = devices.stream()
                .filter(d -> d.getClass().getName().equals("com.sun.media.sound.MidiInDevice"))
                .map(d -> {MIMIMod.LOGGER.info("    " + d.getDeviceInfo().getName() + "(" + d.getClass().getName().toString() + ")"); return d;})
                .collect(Collectors.toList());
        }

        return devices;
    }

    @SuppressWarnings("resource")
    public void handlePlayerTick(Player player) {
        if(!player.getUUID().equals(Minecraft.getInstance().player.getUUID())) {
            return;
        }

        if(player.isAlive() && player.isAddedToWorld()) {
            this.localInstrumentsToPlay = localInstrumentsToPlay(player);
        } else {
            this.localInstrumentsToPlay.clear();
        }
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
            .filter(instrumentStack -> MidiNbtDataUtils.getSysInput(instrumentStack.getRight()) && MidiNbtDataUtils.isChannelEnabled(instrumentStack.getRight(), channel))
            .collect(Collectors.toList());
    }

    protected void openTransmitter() {
        MIMIMod.LOGGER.info("Opening MIDI Input Device: '" + this.selectedDeviceName + "'");

        for(MidiDevice device : getAvailableDevices()) {
            if(device.getDeviceInfo().getName().equals(this.selectedDeviceName) && this.activeDevice == null) {
                MIMIMod.LOGGER.info("Found matching MIDI Input Device: '" + device.getDeviceInfo().getName() + "'");
                try {
                    if(device != null) {
                        MIMIMod.LOGGER.info("Retrieved device: " + device.getDeviceInfo().getName() + "(" + device.getClass().getName().toString() + "): " + device.getTransmitters().size() + " / " + device.getMaxTransmitters());

                        if(device.getMaxTransmitters() != 0 && (device.getMaxTransmitters() == -1 || device.getTransmitters().size() < device.getMaxTransmitters())) {
                            MIMIMod.LOGGER.info("Successfully retrieved device from MIDI system. Opening...");
                            activeDevice = device;
                            activeDevice.open();
                            activeTransmitter = device.getTransmitter();
                            activeReceiver = new MidiDeviceInputReceiver();
                            activeTransmitter.setReceiver(activeReceiver);
                            MIMIMod.LOGGER.info("Successfully opened MIDI Input Device.");
                        } else {
                            MIMIMod.LOGGER.error("Device is already in use or cannot support any Transmitters.");
                            midiDeviceError = "Device in use.";
                        }
                    }
                } catch(Exception e) {
                    MIMIMod.LOGGER.error("Failed to open MIDI Input Device: '" + this.selectedDeviceName + "'. Error: " + e.getMessage());
                    midiDeviceError = e.getMessage();
                    close();
                }

            }
        }

        if(!this.isDeviceAvailable()) {
            MIMIMod.LOGGER.error("Failed to open MIDI Input Device: '" + this.selectedDeviceName + "'. Error: Device not found.");
            midiDeviceError = "Device not found";
        }
    }

    public void open() {
        if(isDeviceSelected() && this.activeTransmitter == null) {
            this.openTransmitter();
        }
    }

    public void close() {
        try {
            if(activeReceiver != null) {
                MIMIMod.LOGGER.info("Attempting to close MIDI Input Device Receiver: " + this.selectedDeviceName);
                activeReceiver.close();
                activeReceiver = null;
                MIMIMod.LOGGER.info("Receiver closed.");
            }
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to close MIDI Input Device Receiver. Error: " + e.getMessage());
            activeReceiver = null;
        }

        try {
            if(activeTransmitter != null) {
                MIMIMod.LOGGER.info("Attempting to close MIDI Input Device Transmitter: " + this.selectedDeviceName);
                activeTransmitter.close();
                activeTransmitter = null;
                MIMIMod.LOGGER.info("Transmitter closed.");
            }
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to close MIDI Input Device Transmitter. Error: " + e.getMessage());
            activeTransmitter = null;
        }

        try {
            if(activeDevice != null) {
                MIMIMod.LOGGER.info("Attempting to close MIDI Input Device: " + this.selectedDeviceName);
                activeDevice.close();
                activeDevice = null;
                MIMIMod.LOGGER.info("Device closed.");
            }
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to close MIDI Input Device. Error: " + e.getMessage());
            activeDevice = null;
        }
    }
}
