package io.github.tofodroid.mods.mimi.common.tile;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public abstract class ASwitchboardContainerEntity extends BaseContainerBlockEntity implements WorldlyContainer {
    protected final Integer INVENTORY_SIZE;
    protected NonNullList<ItemStack> items;
    protected LazyOptional<SidedInvWrapper> wrapper = LazyOptional.of(() -> new SidedInvWrapper(this, Direction.UP));

    public ASwitchboardContainerEntity(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state);
        this.INVENTORY_SIZE = inventorySize;

        if(inventorySize > 0) {
            items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        } else {
            items = NonNullList.withSize(0, ItemStack.EMPTY);
        }
    }

    public ItemStack getSwitchboardStack() {
        return items.isEmpty() ? ItemStack.EMPTY : items.get(0);
    }
    
    public Boolean hasSwitchboard() {
        return !getSwitchboardStack().isEmpty();
    }

    @Override
	protected void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
		
		ListTag list = new ListTag();
		for(ItemStack stack : items) {
			CompoundTag stackCmp = new CompoundTag();
			stack.save(stackCmp);
			list.add(stackCmp);
		}
		compound.put("stacks", list);
	}

	@Override
	public void load(CompoundTag nbt) {
		super.load(nbt);

		ListTag list = nbt.getList("stacks", 10);
		for(int i = 0; i < list.size(); i++)
			items.set(i, ItemStack.of(list.getCompound(i)));
	}
    
    @Override
    public int getContainerSize() {
        return this.INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return items.isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        return this.getItems().get(i);
    }

    @Override
    public ItemStack removeItem(int i, int count) {
        ItemStack itemstack = ContainerHelper.removeItem(this.getItems(), i, count);
        if (!itemstack.isEmpty()) {
            this.setChanged();
        }
        return itemstack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(this.getItems(), i);
    }

    @Override
    public void setItem(int i, ItemStack item) {
        this.getItems().set(i, item);
        if (item.getCount() > this.getMaxStackSize()) {
            item.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player playerEntity) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return !(playerEntity.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) > 64.0D);
        }
    }

    public void dropContent() {
        this.getItems().forEach(stack -> {
            Containers.dropItemStack(this.level, this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), stack);
        });
        this.clearContent();
    }

    @Override
    public void clearContent() {
        this.getItems().clear();
    }

    @Override
    public int[] getSlotsForFace(Direction p_19238_) {
        int[] slots = new int[INVENTORY_SIZE];
        
        for(int i = 0; i < INVENTORY_SIZE; i++) {
            slots[i] = i;
        }
        return slots;
    }

    @Override
    public boolean canPlaceItemThroughFace(int p_19235_, ItemStack p_19236_, Direction p_19237_) {
        return true;
    }

    @Override
    public boolean canTakeItemThroughFace(int p_19239_, ItemStack p_19240_, Direction p_19241_) {
        return true;
    }

    @Override
	public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
		if(!remove && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY)
			return wrapper.cast();

		return super.getCapability(capability, facing);
	}

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

}
