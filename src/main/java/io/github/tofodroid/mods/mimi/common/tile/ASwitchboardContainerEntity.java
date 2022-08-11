package io.github.tofodroid.mods.mimi.common.tile;

import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ASwitchboardContainerEntity extends AContainerTile {
    public ASwitchboardContainerEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public ItemStack getSwitchboardStack() {
        return items.isEmpty() ? ItemStack.EMPTY : items.get(0);
    }
    
    public Boolean hasSwitchboard() {
        return !getSwitchboardStack().isEmpty();
    }
    
    @Override
	public void load(CompoundTag nbt) {
        super.load(nbt);

        // Util to migrate legacy inventory to new tag format
        ListTag legacyInv = nbt.getList("stacks", 10);
        ListTag newInv = nbt.getList("Items", 10);
        if((legacyInv != null && !legacyInv.isEmpty()) && (newInv == null || newInv.isEmpty())) {
            ListTag migrationInv = new ListTag();
            for(int i = 0; i < legacyInv.size(); i++) {
                CompoundTag invTag = legacyInv.getCompound(i);
                invTag.putByte("Slot", (byte)i);
                migrationInv.add(invTag);
            };
            nbt.put("Items", migrationInv);
        }
        nbt.remove("stacks");

        // Load
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, this.items);
	}

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, Direction side) {
        return stack.getItem().equals(ModItems.SWITCHBOARD);
    }
}
