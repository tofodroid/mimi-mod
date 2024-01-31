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
    public void load(CompoundTag compound) {
        super.load(compound);
        
        // Fallback for missing stack data
        if(this.items.get(0).isEmpty()) {
            MIMIMod.LOGGER.warn("ConfigurableTile had no saved source stack! Re-initializing.");
            this.setSourceStack(this.initializeSourceStack(null));
        } else {
            this.onSourceStackChanged();
        }
    }

    protected ItemStack initializeSourceStack(CompoundTag stackTag) {
        ItemStack sourceStack = new ItemStack(this.getBlockState().getBlock().asItem(), 1);

        if(stackTag != null) {
            sourceStack.setTag(stackTag);
        } else {
            sourceStack.setTag(new CompoundTag());
        }

        return sourceStack;
    }
}
