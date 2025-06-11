package io.github.tofodroid.mods.mimi.client.midi;

import javax.sound.midi.MidiMessage;
import javax.sound.midi.Receiver;
import javax.sound.midi.ShortMessage;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.api.event.MidiEventType;
import io.github.tofodroid.mods.mimi.common.api.event.broadcast.BroadcastEvent;
import io.github.tofodroid.mods.mimi.common.api.event.note.NoteEvent;
import io.github.tofodroid.mods.mimi.common.config.ConfigProxy;
import io.github.tofodroid.mods.mimi.common.network.MidiDeviceBroadcastPacket;
import io.github.tofodroid.mods.mimi.common.network.NoteEventPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkProxy;
import io.github.tofodroid.mods.mimi.util.EntityUtils;
import io.github.tofodroid.mods.mimi.util.MathUtils;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public class MidiDeviceInputReceiver implements Receiver {
    public static final Integer MAX_MIDI_DEVICE_VOLUME = 10;

    private volatile boolean open = true;

    public void send(MidiMessage msg, long timeStamp) {
        if(open && msg instanceof ShortMessage) {
            handleMessage((ShortMessage)msg);
        }
    }

    public void close() {
        open = false;
    }

    protected void handleMessage(ShortMessage message) {
        Player player = Minecraft.getInstance().player;

        if(player != null && MIMIMod.getProxy().isClient()) {
            BroadcastEvent event = BroadcastEvent.fromShortMessage(message, player.getUUID(), player.getLevel().dimension(), EntityUtils.getEntityHeadPos(player), 16, MathUtils.addClamped(message.getData2(), ConfigProxy.getMidiDeviceVelocity(), 0, 127));

            if(event != null) {
                ((ClientProxy)MIMIMod.getProxy()).getMidiData().inputDeviceManager.getLocalInstrumentsForMidiDevice(player, Integer.valueOf(message.getChannel()).byteValue()).forEach(instrumentStack -> {
                    playInstrument(event, player, instrumentStack.getLeft(), instrumentStack.getRight());
                });

                if(((ClientProxy)MIMIMod.getProxy()).getMidiData().inputDeviceManager.getTransmitMidiInput()) {
                    MidiDeviceBroadcastPacket packet = MidiDeviceBroadcastPacket.fromBroadcastEvent(event);
                    NetworkProxy.sendToServer(packet);
                }
            }
        }
    }

    private void playInstrument(BroadcastEvent event, Player player, InteractionHand hand, ItemStack instrument) {
        // Apply Instrument Volume Setting
        NoteEventPacket packet = NoteEventPacket.fromNoteEvent(new NoteEvent(
                event.type, 
                true,
                MidiNbtDataUtils.getInstrumentId(instrument),
                hand,
                event.note,
                event.type == MidiEventType.NOTE_ON ? MidiNbtDataUtils.applyInstrumentVolume(instrument, event.velocity) : event.velocity,
                event.senderId,
                event.dimension,
                event.pos,
                event.eventTime
        ));

        NetworkProxy.sendToServer(packet);
        ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().handleLocalPacketInstant(packet);
    }
}
