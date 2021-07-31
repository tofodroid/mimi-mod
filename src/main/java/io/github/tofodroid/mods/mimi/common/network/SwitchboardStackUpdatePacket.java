package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.PacketBuffer;

public class SwitchboardStackUpdatePacket {
    public final UUID midiSource;
    public final Byte filterOct;
    public final Byte filterNote;
    public final String enabledChannelsString;
    public final Byte instrumentId;
    public final Boolean sysInput;

    public SwitchboardStackUpdatePacket(UUID midiSource, Byte filterOct, Byte filterNote, String enabledChannelsString, Byte instrumentId, Boolean sysInput) {
        this.midiSource = midiSource;
        this.filterOct = filterOct;
        this.filterNote = filterNote;
        this.enabledChannelsString = enabledChannelsString;
        this.instrumentId = instrumentId;
        this.sysInput = sysInput;
    }
    
    public static SwitchboardStackUpdatePacket decodePacket(PacketBuffer buf) {
        try {
            UUID midiSource = buf.readUniqueId();
            if(ItemMidiSwitchboard.NONE_SOURCE_ID.equals(midiSource)) {
                midiSource = null;
            }

            Byte filterOct = buf.readByte();
            Byte filterNote = buf.readByte();

            String enabledChannelsString = buf.readString(38);
            if(enabledChannelsString.trim().isEmpty()) {
                enabledChannelsString = null;
            }

            Byte instrumentId = buf.readByte();

            Boolean sysInput = buf.readBoolean();

            return new SwitchboardStackUpdatePacket(midiSource, filterOct, filterNote, enabledChannelsString, instrumentId, sysInput);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("SwitchboardStackUpdatePacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("SwitchboardStackUpdatePacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(SwitchboardStackUpdatePacket pkt, PacketBuffer buf) {
        buf.writeUniqueId(pkt.midiSource != null ? pkt.midiSource : ItemMidiSwitchboard.NONE_SOURCE_ID);
        buf.writeByte(pkt.filterOct);
        buf.writeByte(pkt.filterNote);
        buf.writeString(pkt.enabledChannelsString != null ? pkt.enabledChannelsString : "", 38);
        buf.writeByte(pkt.instrumentId);
        buf.writeBoolean(pkt.sysInput);
    }
}
