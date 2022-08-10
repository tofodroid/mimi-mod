package io.github.tofodroid.mods.mimi.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.StackedContentsCompatible;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.wrapper.SidedInvWrapper;

public abstract class AContainerTile extends BaseContainerBlockEntity implements WorldlyContainer, StackedContentsCompatible {
    protected final Integer INVENTORY_SIZE;
    protected NonNullList<ItemStack> items;

    public AContainerTile(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state);
        this.INVENTORY_SIZE = inventorySize;

        if(inventorySize > 0) {
            items = NonNullList.withSize(INVENTORY_SIZE, ItemStack.EMPTY);
        } else {
            items = NonNullList.withSize(0, ItemStack.EMPTY);
        }
    }

    @Override
	protected void saveAdditional(CompoundTag compound) {
		super.saveAdditional(compound);
        ContainerHelper.saveAllItems(compound, this.items);
	}

	@Override
	public void load(CompoundTag nbt) {
        super.load(nbt);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(nbt, this.items);
	}
    
    @Override
    public int getContainerSize() {
        return this.INVENTORY_SIZE;
    }

    @Override
    public boolean isEmpty() {
        return this.items.isEmpty();
    }

    @Override
    public ItemStack getItem(int i) {
        return this.items.get(i);
    }

    @Override
    public ItemStack removeItem(int i, int count) {
        return ContainerHelper.removeItem(this.getItems(), i, count);
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ContainerHelper.takeItem(this.getItems(), i);
    }

    @Override
    public void setItem(int i, ItemStack item) {
        this.items.set(i, item);
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
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, Direction side) {
        return true;
    }
    
    @Override
    @Nonnull
    public CompoundTag getUpdateTag() {
        return this.saveWithoutMetadata();
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        this.load(pkt.getTag());
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    
    LazyOptional<? extends IItemHandler> handlers[] = SidedInvWrapper.create(this, Direction.UP);

    @Override
    @Nonnull
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && facing != null && capability == CapabilityItemHandler.ITEM_HANDLER_CAPABILITY) {
            if (facing == Direction.UP)
                return handlers[0].cast();
        }
        return super.getCapability(capability, facing);
    }

    @Override
    public void invalidateCaps() {
        super.invalidateCaps();
        for (LazyOptional<? extends IItemHandler> handler : handlers) handler.invalidate();
    }

    @Override
    public void fillStackedContents(@Nonnull StackedContents helper) {
        for(ItemStack itemstack : this.items) {
            helper.accountStack(itemstack);
        }
    }
}