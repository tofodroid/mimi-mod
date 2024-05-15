package io.github.tofodroid.mods.mimi.common.tile;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AConfigurableTile extends AStaticInventoryTile {
    public static final Integer SOURCE_STACK_SLOT = 0;
    
    public AConfigurableTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, 1);
    }

    protected AConfigurableTile(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public void setSourceStack(ItemStack stack) {
        if(stack.getItem().getClass().equals(this.getBlockState().getBlock().asItem().getClass())) {
            this.setItem(SOURCE_STACK_SLOT, stack);
            this.onSourceStackChanged();
        }
    }

    protected void onSourceStackChanged() {
        // Default no-op
    }

    public ItemStack getSourceStack() {
        if(items.isEmpty() || items.get(0) == null) {
            return ItemStack.EMPTY;
        } else {
            return items.get(SOURCE_STACK_SLOT);
        }
    }
    
    @Override
    public void onItemsLoaded() {
        // Fallback for missing stack
        if(this.items.get(0).isEmpty()) {
            MIMIMod.LOGGER.warn(this.getClass().getSimpleName() + " is missing stack! Re-initializing.");
            this.initializeSourceStack(null);
        }
    }


    protected ItemStack initializeSourceStack(CompoundTag stackTag) {
        ItemStack sourceStack = new ItemStack(this.getBlockState().getBlock().asItem(), 1);
        return sourceStack;
    }
}
