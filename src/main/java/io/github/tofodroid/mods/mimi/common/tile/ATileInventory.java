package io.github.tofodroid.mods.mimi.common.tile;

import io.github.tofodroid.mods.mimi.common.inventory.SwitchboardInventoryStackHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;

public abstract class ATileInventory extends BlockEntity implements MenuProvider {
    public static final String INVENTORY_TAG = "inv";

    protected final Integer INVENTORY_SIZE;
    protected LazyOptional<? extends ItemStackHandler> inventory;
    
    public ATileInventory(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state);
        this.INVENTORY_SIZE = inventorySize;

        if(inventorySize > 0) {
            inventory = buildInventory();
        } else {
            inventory = LazyOptional.empty();
        }
    }

    public LazyOptional<? extends ItemStackHandler> buildInventory() {
        return LazyOptional.of(() -> new SwitchboardInventoryStackHandler(INVENTORY_SIZE));
    }

    @Override
    public <T> net.minecraftforge.common.util.LazyOptional<T> getCapability(net.minecraftforge.common.capabilities.Capability<T> cap, @javax.annotation.Nullable net.minecraft.core.Direction side) {
        if (cap == net.minecraftforge.items.CapabilityItemHandler.ITEM_HANDLER_CAPABILITY )
           return inventory.cast();
        return super.getCapability(cap, side);
     }

    @Override
    public CompoundTag getUpdateTag() {
        return save(super.getUpdateTag());
    }

    @Override
    public CompoundTag save(CompoundTag compound) {
        super.save(compound);

        this.inventory.ifPresent(inv -> compound.put(INVENTORY_TAG, inv.serializeNBT()));

        return compound;
    }

    @Override
    public void load(CompoundTag nbt) {
        super.load(nbt);

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
