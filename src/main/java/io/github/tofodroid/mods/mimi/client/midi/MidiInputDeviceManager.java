package io.github.tofodroid.mods.mimi.client.midi;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.midi.MidiInputSourceManager;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
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
public class MidiInputDeviceManager extends MidiInputSourceManager {
    private List<Object> localInstrumentToPlay = new ArrayList<>();
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

        this.localInstrumentToPlay = localInstrumentsToPlay(event.player);
    }
    
    protected List<Object> localInstrumentsToPlay(Player player) {
        List<Object> result = new ArrayList<>();

        // Check for seated instrument
        TileInstrument instrumentEntity = BlockInstrument.getTileInstrumentForEntity(player);
        if(instrumentEntity != null && instrumentEntity.hasSwitchboard()) {
            result.add(instrumentEntity);
        }

        // Check for held instruments
        ItemStack mainHand = ItemInstrument.getEntityHeldInstrumentStack(player, InteractionHand.MAIN_HAND);
        if(mainHand != null && ItemInstrument.hasSwitchboard(mainHand)) {
            result.add(mainHand);
        }

        ItemStack offHand = ItemInstrument.getEntityHeldInstrumentStack(player, InteractionHand.OFF_HAND);
        if(offHand != null &&  ItemInstrument.hasSwitchboard(offHand)) {
            result.add(offHand);
        }

        return result;
    }
    
    public List<Pair<Byte,ItemStack>> getLocalInstrumentsForMidiDevice(Player player, Byte channel) {
        return localInstrumentToPlay.stream().map(data -> {
            ItemStack switchStack = ItemStack.EMPTY;
            Byte instrumentId = null;

            if(data instanceof ItemStack) {
                switchStack = ItemInstrument.getSwitchboardStack((ItemStack)data);
                instrumentId = ItemInstrument.getInstrumentId((ItemStack)data);
            } else if(data instanceof TileInstrument) {
                switchStack = ((TileInstrument)data).getSwitchboardStack();
                instrumentId = ((TileInstrument)data).getInstrumentId();
            }

            if(ModItems.SWITCHBOARD.equals(switchStack.getItem()) && ItemMidiSwitchboard.getSysInput(switchStack) && ItemMidiSwitchboard.isChannelEnabled(switchStack, channel)) {
                return new ImmutablePair<>(instrumentId,switchStack);
            }

            return null;
        })
        .filter(b -> b != null).collect(Collectors.toList());
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
