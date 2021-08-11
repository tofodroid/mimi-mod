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
    public final Boolean invertNoteOct;
    public final String enabledChannelsString;
    public final Byte instrumentId;
    public final Boolean invertInstrument;
    public final Boolean sysInput;

    public SwitchboardStackUpdatePacket(UUID midiSource, Byte filterOct, Byte filterNote, Boolean invertNoteOct, String enabledChannelsString, Byte instrumentId, Boolean invertInstrument, Boolean sysInput) {
        this.midiSource = midiSource;
        this.filterOct = filterOct;
        this.filterNote = filterNote;
        this.invertNoteOct = invertNoteOct;
        this.enabledChannelsString = enabledChannelsString;
        this.instrumentId = instrumentId;
        this.invertInstrument = invertInstrument;
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
            Boolean invertNoteOct = buf.readBoolean();

            String enabledChannelsString = buf.readString(38);
            if(enabledChannelsString.trim().isEmpty()) {
                enabledChannelsString = null;
            }

            Byte instrumentId = buf.readByte();
            Boolean invertInstrument = buf.readBoolean();

            Boolean sysInput = buf.readBoolean();

            return new SwitchboardStackUpdatePacket(midiSource, filterOct, filterNote, invertNoteOct, enabledChannelsString, instrumentId, invertInstrument, sysInput);
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
        buf.writeBoolean(pkt.invertNoteOct);
        buf.writeString(pkt.enabledChannelsString != null ? pkt.enabledChannelsString : "", 38);
        buf.writeByte(pkt.instrumentId);
        buf.writeBoolean(pkt.invertInstrument);
        buf.writeBoolean(pkt.sysInput);
    }
}
