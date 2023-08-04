package io.github.tofodroid.mods.mimi.common.tile;

import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.IDyeableItem;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileInstrument extends AInventoryTile {
    public static final String COLOR_TAG = "color";
    protected Integer color;

    public TileInstrument(BlockPos pos, BlockState state) {
        super(ModTiles.INSTRUMENT, pos, state, 1);
        items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
    }

    public void setInstrumentStack(ItemStack stack) {
        if(stack.getItem() instanceof IInstrumentItem) {
            this.setItem(0, stack);
            
            if(this.blockInstrument().isDyeable()) {
                this.color = ((IDyeableItem)stack.getItem()).getColor(stack);
            }
        }
    }

    public ItemStack getInstrumentStack() {
        if(items.isEmpty() || items.get(0) == null) {
            return ItemStack.EMPTY;
        } else {
            return items.get(0);
        }
    }

    public Byte getInstrumentId() {
        return this.blockInstrument().getInstrumentId();
    }

    public Boolean hasColor() {
        return color != null && this.blockInstrument().isDyeable();
    }

    public Integer getColor() { 
        if(!this.blockInstrument().isDyeable()) {
            return -1;
        }

        return hasColor() ? color : this.blockInstrument().getDefaultColor();
    }

    private BlockInstrument blockInstrument() {
        return (BlockInstrument)getBlockState().getBlock();
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        if(this.color != null) {
            compound.putInt(COLOR_TAG, color);
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);

        if(compound.contains(COLOR_TAG)) {
            this.color = compound.getInt(COLOR_TAG);
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public ItemStack removeItem(int i, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    }
}