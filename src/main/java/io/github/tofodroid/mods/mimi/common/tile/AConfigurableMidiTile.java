package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AConfigurableMidiTile extends AConfigurableTile {
    protected UUID id;

    public AConfigurableMidiTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, 1);
    }

    protected AConfigurableMidiTile(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state, inventorySize);
    }

    protected Boolean isValid() {
        return !(this.isRemoved() || this.level == null);
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
                convertStack = initializeSourceStack(MidiNbtDataUtils.convertSwitchboardToDataTag(stackTag.getCompound("tag")));
            }
        }

        ContainerHelper.loadAllItems(compound, items);

        if(convertStack != null) {  
            this.setSourceStack(convertStack);
        }
        // END TEMPORARY LEGACY COMPATIBILITY CODE
    }

    public UUID getUUID() {
        if(this.id == null) {
            String idString = getClass().getSimpleName() + this.getBlockPos().getX() + "-" + this.getBlockPos().getY() + "-" + this.getBlockPos().getZ();
            this.id = UUID.nameUUIDFromBytes(idString.getBytes());
        }
        return this.id;
    }
}
