package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class SyncInstrumentPacket {
    public final UUID midiSource;
    public final String midiSourceName;
    public final String enabledChannelsString;
    public final Boolean sysInput;
    public final Byte volume;
    public final InteractionHand handIn;

    public SyncInstrumentPacket(UUID midiSource, String midiSourceName, String enabledChannelsString, Boolean sysInput, Byte volume, InteractionHand handIn) {
        this.midiSource = midiSource;
        this.midiSourceName = midiSourceName;
        this.enabledChannelsString = enabledChannelsString;
        this.sysInput = sysInput;
        this.volume = volume;
        this.handIn = handIn;
    }

    public SyncInstrumentPacket(ItemStack instrumentStack, InteractionHand handIn) {
        this.midiSource = InstrumentDataUtils.getMidiSource(instrumentStack);
        this.midiSourceName = InstrumentDataUtils.getMidiSourceName(instrumentStack);
        this.enabledChannelsString = InstrumentDataUtils.getEnabledChannelsString(instrumentStack);
        this.sysInput = InstrumentDataUtils.getSysInput(instrumentStack);
        this.volume = InstrumentDataUtils.getInstrumentVolume(instrumentStack);
        this.handIn = handIn;
    }

    public static Byte getInstrumentLocationByte(InteractionHand handIn) {
        if(handIn == InteractionHand.MAIN_HAND) {
            return 0;
        } else if(handIn == InteractionHand.OFF_HAND) {
            return 1;
        }
        return 2;
    }

    public static InteractionHand getInstrumentLocationHand(Byte byteIn) {
        if(byteIn == 0) {
            return InteractionHand.MAIN_HAND;
        } else if(byteIn == 1) {
            return InteractionHand.OFF_HAND;
        }
        return null;
    }

    public static SyncInstrumentPacket decodePacket(FriendlyByteBuf buf) {
        try {
            UUID midiSource = buf.readUUID();
            String midiSourceName = buf.readUtf(64);
            if(InstrumentDataUtils.NONE_SOURCE_ID.equals(midiSource)) {
                midiSource = null;
            }

            String enabledChannelsString = buf.readUtf(38);
            if(enabledChannelsString.trim().isEmpty()) {
                enabledChannelsString = null;
            }

            Boolean sysInput = buf.readBoolean();
            Byte volume = buf.readByte();
            InteractionHand handIn = getInstrumentLocationHand(buf.readByte());

            return new SyncInstrumentPacket(midiSource, midiSourceName, enabledChannelsString, sysInput,volume, handIn);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("SyncInstrumentPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("SyncInstrumentPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(SyncInstrumentPacket pkt, FriendlyByteBuf buf) {
        buf.writeUUID(pkt.midiSource != null ? pkt.midiSource : InstrumentDataUtils.NONE_SOURCE_ID);
        buf.writeUtf(pkt.midiSourceName, 64);
        buf.writeUtf(pkt.enabledChannelsString != null ? pkt.enabledChannelsString : "", 38);
        buf.writeBoolean(pkt.sysInput);
        buf.writeByte(pkt.volume);
        buf.writeByte(getInstrumentLocationByte(pkt.handIn));
    }
}
