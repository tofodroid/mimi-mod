package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.PacketBuffer;

public class SwitchboardStackUpdatePacket {
    public final String enabledChannelsString;
    public final String filterNoteString;
    public final UUID midiSource;

    public SwitchboardStackUpdatePacket(UUID midiSource, String enabledChannelsString, String filterNoteString) {
        this.enabledChannelsString = enabledChannelsString;
        this.midiSource = midiSource;
        this.filterNoteString = filterNoteString;
    }
    
    public static SwitchboardStackUpdatePacket decodePacket(PacketBuffer buf) {
        try {
            String enabledChannelsString = buf.readString(38);
            if(enabledChannelsString.trim().isEmpty()) {
                enabledChannelsString = null;
            }

            UUID midiSource = buf.readUniqueId();
            if(ItemMidiSwitchboard.NONE_SOURCE_ID.equals(midiSource)) {
                midiSource = null;
            }

            String filterNoteString = buf.readString(47);
            if(filterNoteString.trim().isEmpty()) {
                filterNoteString = null;
            }

            return new SwitchboardStackUpdatePacket(midiSource, enabledChannelsString, filterNoteString);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("SwitchboardStackUpdatePacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("SwitchboardStackUpdatePacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(SwitchboardStackUpdatePacket pkt, PacketBuffer buf) {
        buf.writeString(pkt.enabledChannelsString != null ? pkt.enabledChannelsString : "", 38);
        buf.writeUniqueId(pkt.midiSource != null ? pkt.midiSource : ItemMidiSwitchboard.NONE_SOURCE_ID);
        buf.writeString(pkt.filterNoteString != null ? pkt.filterNoteString : "", 47);
    }
}
