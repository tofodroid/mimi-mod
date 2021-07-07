package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.instruments.InstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.network.InstrumentTileDataUpdatePacket;

import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;

public class TileInstrument extends TileEntity {
    public static final String INSTRUMENT_ID_TAG = "instrument";

    private Byte instrumentId;
    private UUID maestro;
    private String acceptedChannelsString;

    public TileInstrument() {
        super(ModTiles.INSTRUMENT);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);

        if(compound.contains(INSTRUMENT_ID_TAG)) {
            this.instrumentId = compound.getByte(INSTRUMENT_ID_TAG);
        }

        if(compound.contains(InstrumentDataUtil.MAESTRO_TAG)) {
            this.maestro = compound.getUniqueId(InstrumentDataUtil.MAESTRO_TAG);
        } else {
            this.maestro = null;
        }

        if(compound.contains(InstrumentDataUtil.LISTEN_CHANNELS_TAG)) {
            this.acceptedChannelsString = compound.getString(InstrumentDataUtil.LISTEN_CHANNELS_TAG);
        } else {
            this.acceptedChannelsString = null;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        
        if(this.instrumentId != null) {
            compound.putByte(INSTRUMENT_ID_TAG, this.instrumentId);
        }

        if(this.maestro != null) {
            compound.putUniqueId(InstrumentDataUtil.MAESTRO_TAG, this.maestro);
        }
                
        if(this.acceptedChannelsString != null && !this.acceptedChannelsString.isEmpty()) {
            compound.putString(InstrumentDataUtil.LISTEN_CHANNELS_TAG, this.acceptedChannelsString);
        }

        return compound;
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return this.write(new CompoundNBT());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT nbt) {
        this.read(state, nbt);
    }
    
    @Override
	public SUpdateTileEntityPacket getUpdatePacket() {
		CompoundNBT nbt = new CompoundNBT();
		this.write(nbt);
		return new SUpdateTileEntityPacket(this.getPos(), 0, nbt);
	}
    
	@Override
	public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket packet) {
		this.read(this.getWorld().getBlockState(packet.getPos()), packet.getNbtCompound());
	}

    public Byte getInstrumentId() {
        return instrumentId;
    }

    public void setInstrumentId(Byte instrumentId) {
        this.instrumentId = instrumentId;
    }

    public String getAcceptedChannelsString() {
        return acceptedChannelsString;
    }

    public void setAcceptedChannelsString(String acceptedChannelsString) {
        this.acceptedChannelsString = acceptedChannelsString;
    }

    public UUID getMaestro() {
        return maestro;
    }

    public void setMaestro(UUID maestro) {
        this.maestro = maestro;
    }

    public static InstrumentTileDataUpdatePacket getSyncPacket(TileInstrument e) {
        return new InstrumentTileDataUpdatePacket(
            e.getPos(),
            e.getMaestro(), 
            e.getAcceptedChannelsString()
        );
    }
}
