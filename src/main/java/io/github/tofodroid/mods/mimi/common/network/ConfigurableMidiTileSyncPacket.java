package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import io.netty.handler.codec.DecoderException;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.ItemStack;

public class ConfigurableMidiTileSyncPacket {
    public final BlockPos tilePos;
    public final UUID midiSource;
    public final String midiSourceName;
    public final Byte filterOct;
    public final Byte filterNote;
    public final Boolean invertNoteOct;
    public final Byte instrumentId;
    public final String enabledChannelsString;
    public final Boolean invertInstrument;
    public final Boolean publicBroadcast;

    public ConfigurableMidiTileSyncPacket(BlockPos tilePos, UUID midiSource, String midiSourceName, Byte filterOct, Byte filterNote, Boolean invertNoteOct, String enabledChannelsString, Byte instrumentId, Boolean invertInstrument, Boolean publicBroadcast) {
        this.tilePos = tilePos;
        this.midiSource = midiSource;
        this.midiSourceName = midiSourceName;
        this.filterOct = filterOct;
        this.filterNote = filterNote;
        this.invertNoteOct = invertNoteOct;
        this.enabledChannelsString = enabledChannelsString;
        this.instrumentId = instrumentId;
        this.invertInstrument = invertInstrument;
        this.publicBroadcast = publicBroadcast;
    }
    
    public ConfigurableMidiTileSyncPacket(ItemStack sourceStack, BlockPos tilePos) {
        this.tilePos = tilePos;
        this.midiSource = InstrumentDataUtils.getMidiSource(sourceStack);
        this.midiSourceName = InstrumentDataUtils.getMidiSourceName(sourceStack);
        this.filterOct = InstrumentDataUtils.getFilterOct(sourceStack);
        this.filterNote = InstrumentDataUtils.getFilterNote(sourceStack);
        this.invertNoteOct = InstrumentDataUtils.getInvertNoteOct(sourceStack);
        this.enabledChannelsString = InstrumentDataUtils.getEnabledChannelsString(sourceStack);
        this.instrumentId = InstrumentDataUtils.getFilterInstrument(sourceStack);
        this.invertInstrument = InstrumentDataUtils.getInvertInstrument(sourceStack);
        this.publicBroadcast = InstrumentDataUtils.getPublicBroadcast(sourceStack);
    }

    public static ConfigurableMidiTileSyncPacket decodePacket(FriendlyByteBuf buf) {
         try {
            BlockPos tilePos = buf.readBlockPos();
            UUID midiSource = buf.readUUID();
            String midiSourceName = buf.readUtf(64);
            Byte filterOct = buf.readByte();
            Byte filterNote = buf.readByte();
            Boolean invertNoteOct = buf.readBoolean();
            String enabledChannelsString = buf.readUtf(38);
            Byte instrumentId = buf.readByte();
            Boolean invertInstrument = buf.readBoolean();
            Boolean publicBroadcast = buf.readBoolean();

            return new ConfigurableMidiTileSyncPacket(tilePos, midiSource, midiSourceName, filterOct, filterNote, invertNoteOct, enabledChannelsString, instrumentId, invertInstrument, publicBroadcast);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("ConfigurableMidiTileSyncPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("ConfigurableMidiTileSyncPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }
    
    public static void encodePacket(ConfigurableMidiTileSyncPacket pkt, FriendlyByteBuf buf) {
        buf.writeBlockPos(pkt.tilePos);
        buf.writeUUID(pkt.midiSource);
        buf.writeUtf(pkt.midiSourceName, 64);
        buf.writeByte(pkt.filterOct);
        buf.writeByte(pkt.filterNote);
        buf.writeBoolean(pkt.invertNoteOct);
        buf.writeUtf(pkt.enabledChannelsString, 38);
        buf.writeByte(pkt.instrumentId);
        buf.writeBoolean(pkt.invertInstrument);
        buf.writeBoolean(pkt.publicBroadcast);
    }
}
