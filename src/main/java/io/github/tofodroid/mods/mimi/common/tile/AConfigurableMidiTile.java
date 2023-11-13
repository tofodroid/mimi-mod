package io.github.tofodroid.mods.mimi.common.tile;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AConfigurableMidiTile extends AStaticInventoryTile {
    public static final Integer SOURCE_STACK_SLOT = 0;
    
    public AConfigurableMidiTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, 1);
    }

    protected AConfigurableMidiTile(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public void setSourceStack(ItemStack stack) {
        if(stack.getItem().getClass().equals(this.getBlockState().getBlock().asItem().getClass())) {
            this.setItem(SOURCE_STACK_SLOT, stack);
        }
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
            MIMIMod.LOGGER.warn("ConfigurableMidiTile had no saved source stack! Re-initializing.");
            this.setSourceStack(this.initializeSourceStack(null));
        }
    }
    
    @Override
    public void loadItems(CompoundTag compound) {
        // START TEMPORARY LEGACY COMPATIBILITY CODE
        // Filter out switchboard items so that we can convert them
        ListTag listtag = compound.getList("Items", 10);
        ItemStack convertStack = null;

        if(listtag.size() > 0) {
            CompoundTag stackTag = listtag.getCompound(0);
            String itemId = stackTag.getString("id");

            if(itemId.equalsIgnoreCase("mimi:switchboard")) {
                MIMIMod.LOGGER.info("Converting Configurable MIDI Tile from Switchboard.");
                convertStack = initializeSourceStack(InstrumentDataUtils.convertSwitchboardToDataTag(stackTag.getCompound("tag")));
            }
        }

        ContainerHelper.loadAllItems(compound, items);

        if(convertStack != null) {  
            this.setSourceStack(convertStack);
        }
        // END TEMPORARY LEGACY COMPATIBILITY CODE
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
