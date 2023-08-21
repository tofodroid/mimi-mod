package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentHandheld;
import io.github.tofodroid.mods.mimi.common.midi.AMidiInputSourceManager;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.api.distmarker.Dist;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MidiInputDeviceManager extends AMidiInputSourceManager {
    private List<ItemStack> localInstrumentsToPlay = new ArrayList<>();
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

    @SubscribeEvent
    public void handleTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT || !event.player.isLocalPlayer()) {
            return;
        }

        this.localInstrumentsToPlay = localInstrumentsToPlay(event.player);
    }
    
    protected List<ItemStack> localInstrumentsToPlay(Player player) {
        List<ItemStack> result = new ArrayList<>();

        // Check for seated instrument
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(player);
        if(instrumentEntity != null) {
            result.add(instrumentEntity.getInstrumentStack());
        }

        // Check for held instruments
        ItemStack mainHand = ItemInstrumentHandheld.getEntityHeldInstrumentStack(player, InteractionHand.MAIN_HAND);
        if(mainHand != null) {
            result.add(mainHand);
        }

        ItemStack offHand = ItemInstrumentHandheld.getEntityHeldInstrumentStack(player, InteractionHand.OFF_HAND);
        if(offHand != null) {
            result.add(offHand);
        }

        return result;
    }
    
    public List<ItemStack> getLocalInstrumentsForMidiDevice(Player player, Byte channel) {
        return localInstrumentsToPlay.stream()
            .filter(instrumentStack -> InstrumentDataUtils.getSysInput(instrumentStack) && InstrumentDataUtils.isChannelEnabled(instrumentStack, channel))
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
