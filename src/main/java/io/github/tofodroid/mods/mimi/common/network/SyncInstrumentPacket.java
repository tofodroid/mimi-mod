package io.github.tofodroid.mods.mimi.common.network;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import io.netty.handler.codec.DecoderException;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

public class SyncInstrumentPacket implements CustomPacketPayload {
    public static final ResourceLocation ID = new ResourceLocation(MIMIMod.MODID, SyncInstrumentPacket.class.getSimpleName().toLowerCase());
    public static final CustomPacketPayload.Type<SyncInstrumentPacket> TYPE = new Type<>(ID);

    public final UUID midiSource;
    public final String midiSourceName;
    public final Integer enabledChannelsInt;
    public final Boolean sysInput;
    public final Byte volume;
    public final InteractionHand handIn;

    public SyncInstrumentPacket(UUID midiSource, String midiSourceName, Integer enabledChannelsInt, Boolean sysInput, Byte volume, InteractionHand handIn) {
        this.midiSource = midiSource;
        this.midiSourceName = midiSourceName;
        this.enabledChannelsInt = enabledChannelsInt;
        this.sysInput = sysInput;
        this.volume = volume;
        this.handIn = handIn;
    }

    public SyncInstrumentPacket(ItemStack instrumentStack, InteractionHand handIn) {
        this.midiSource = MidiNbtDataUtils.getMidiSource(instrumentStack);
        this.midiSourceName = MidiNbtDataUtils.getMidiSourceName(instrumentStack, false);
        this.enabledChannelsInt = MidiNbtDataUtils.getEnabledChannelsInt(instrumentStack);
        this.sysInput = MidiNbtDataUtils.getSysInput(instrumentStack);
        this.volume = MidiNbtDataUtils.getInstrumentVolume(instrumentStack);
        this.handIn = handIn;
    }
    
    @Override
    public Type<? extends CustomPacketPayload> type() {
       return TYPE;
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
            UUID midiSource = null;
            if(buf.readBoolean()) {
                midiSource = buf.readUUID();
            }

            String midiSourceName = null;
            if(buf.readBoolean()) {
                midiSourceName = buf.readUtf(64);
            }

            Integer enabledChannelsInt = buf.readInt();
            Boolean sysInput = buf.readBoolean();
            Byte volume = buf.readByte();
            InteractionHand handIn = getInstrumentLocationHand(buf.readByte());

            return new SyncInstrumentPacket(midiSource, midiSourceName, enabledChannelsInt, sysInput,volume, handIn);
        } catch(IndexOutOfBoundsException e) {
            MIMIMod.LOGGER.error("SyncInstrumentPacket did not contain enough bytes. Exception: " + e);
            return null;
        } catch(DecoderException e) {
            MIMIMod.LOGGER.error("SyncInstrumentPacket contained invalid bytes. Exception: " + e);
            return null;
        }
    }

    public static void encodePacket(SyncInstrumentPacket pkt, FriendlyByteBuf buf) {
        if(pkt.midiSource != null) {
            buf.writeBoolean(true);
            buf.writeUUID(pkt.midiSource);
        } else {
            buf.writeBoolean(false);
        }
        
        if(pkt.midiSourceName != null) {
            buf.writeBoolean(true);
            buf.writeUtf(pkt.midiSourceName, 64);
        } else {
            buf.writeBoolean(false);
        }

        buf.writeInt(pkt.enabledChannelsInt);
        buf.writeBoolean(pkt.sysInput);
        buf.writeByte(pkt.volume);
        buf.writeByte(getInstrumentLocationByte(pkt.handIn));
    }
}
