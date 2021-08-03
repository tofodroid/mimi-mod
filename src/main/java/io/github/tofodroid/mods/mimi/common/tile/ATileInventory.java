package io.github.tofodroid.mods.mimi.common.tile;

import net.minecraft.block.BlockState;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public abstract class ATileInventory extends TileEntity implements INamedContainerProvider {
    public static final String INVENTORY_TAG = "inv";

    protected final Integer INVENTORY_SIZE;
    protected LazyOptional<ItemStackHandler> inventory;
    
    public ATileInventory(TileEntityType<?> type, Integer inventorySize) {
        super(type);
        this.INVENTORY_SIZE = inventorySize;
        inventory = LazyOptional.of(() -> new ItemStackHandler(INVENTORY_SIZE));
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

        this.inventory.ifPresent(inv -> compound.put(INVENTORY_TAG, inv.serializeNBT()));

        return compound;
    }

    @Override
    public void read(BlockState state, CompoundNBT nbt) {
        super.read(state, nbt);

        this.inventory.ifPresent(inv -> inv.deserializeNBT(nbt.getCompound(INVENTORY_TAG)));

        if(!this.inventory.isPresent()) {
            this.inventory = LazyOptional.of(() -> new ItemStackHandler(INVENTORY_SIZE));
        }
    }

    public ItemStackHandler getInventory() {
        return this.inventory.orElse(null);
    }

    public void setInventory(ItemStackHandler handler) {
        if(handler != null) {
            this.inventory = LazyOptional.of(() -> handler);
        }
    }
}
