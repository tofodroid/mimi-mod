package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.container.ContainerReceiver;
import io.github.tofodroid.mods.mimi.common.data.CommonDataUtil;
import io.github.tofodroid.mods.mimi.common.network.ReceiverTileDataUpdatePacket;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class TileReceiver extends TileEntity {
    /* CONTAINER
    implements INamedContainerProvider {
    public LazyOptional<ItemStackHandler> inventory;
    
    public TileReceiver() {
        super(ModTiles.RECEIVER);
        inventory = LazyOptional.of(() -> new ItemStackHandler(1));
    }

    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return new ContainerReceiver(id, playerInventory, inventory.orElseThrow(NullPointerException::new));
    }

    @Override
    public ITextComponent getDisplayName() {
        return new TranslationTextComponent("container.compactstorage.compact_chest");
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> cap, Direction side) {
        if(cap == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            return inventory.cast();
        }

        return super.getCapability(cap, side);
    }

    @Override
    public SUpdateTileEntityPacket getUpdatePacket() {
        return new SUpdateTileEntityPacket(getPos(), 0, this.write(new CompoundNBT()));
    }

    @Override
    public void onDataPacket(NetworkManager net, SUpdateTileEntityPacket pkt) {
        this.read(world.getBlockState(pkt.getPos()), pkt.getNbtCompound());
    }

    @Override
    public CompoundNBT getUpdateTag() {
        return write(super.getUpdateTag());
    }

    @Override
    public void handleUpdateTag(BlockState state, CompoundNBT tag) {
        read(world.getBlockState(pos), tag);
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);

        this.inventory.ifPresent(inv -> compound.put("Inventory", inv.serializeNBT()));

        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);

        //reads the data from nbt to our ItemStackHandler
        this.inventory.ifPresent(inv -> inv.deserializeNBT(nbt.getCompound("Inventory")));

        if(!this.inventory.isPresent()) {
            this.inventory = LazyOptional.of(() -> new ItemStackHandler(1));
        }
    }
    */

    public static final String NOTE_TAG = "FILTER_NOTES";

    private UUID midiSource;
    private String acceptedChannelsString;
    private String filterNoteString;

    public TileReceiver() {
        super(ModTiles.RECEIVER);
    }

    @Override
    public void read(BlockState state, CompoundNBT compound) {
        super.read(state, compound);

        if(compound.contains(CommonDataUtil.SOURCE_TAG)) {
            this.midiSource = compound.getUniqueId(CommonDataUtil.SOURCE_TAG);
        } else {
            this.midiSource = null;
        }

        if(compound.contains(NOTE_TAG)) {
            this.filterNoteString = compound.getString(NOTE_TAG);
        } else {
            this.filterNoteString = null;
        }

        if(compound.contains(CommonDataUtil.LISTEN_CHANNELS_TAG)) {
            this.acceptedChannelsString = compound.getString(CommonDataUtil.LISTEN_CHANNELS_TAG);
        } else {
            this.acceptedChannelsString = null;
        }
    }

    @Override
    public CompoundNBT write(CompoundNBT compound) {
        super.write(compound);
        
        if(this.midiSource != null) {
            compound.putUniqueId(CommonDataUtil.SOURCE_TAG, this.midiSource);
        }

        if(this.filterNoteString != null && !this.filterNoteString.isEmpty()) {
            compound.putString(NOTE_TAG, this.filterNoteString);
        }
                
        if(this.acceptedChannelsString != null && !this.acceptedChannelsString.isEmpty()) {
            compound.putString(CommonDataUtil.LISTEN_CHANNELS_TAG, this.acceptedChannelsString);
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

    public String getAcceptedChannelsString() {
        return acceptedChannelsString;
    }

    public void setAcceptedChannelsString(String acceptedChannelsString) {
        this.acceptedChannelsString = acceptedChannelsString;
    }

    public String getFilterNoteString() {
        return filterNoteString;
    }

    public void setFilterNoteString(String filterNoteString) {
        this.filterNoteString = filterNoteString;
    }

    public UUID getMidiSource() {
        return midiSource;
    }

    public void setMidiSource(UUID midiSource) {
        this.midiSource = midiSource;
    }

    public static ReceiverTileDataUpdatePacket getSyncPacket(TileReceiver e) {
        return new ReceiverTileDataUpdatePacket(
            e.getPos(),
            e.getMidiSource(), 
            e.getAcceptedChannelsString(),
            e.getFilterNoteString() 
        );
    }
}
