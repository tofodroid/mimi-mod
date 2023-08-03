package io.github.tofodroid.mods.mimi.common.container;

import io.github.tofodroid.mods.mimi.common.container.slot.SlotDiskWriteResult;
import io.github.tofodroid.mods.mimi.common.item.ItemFloppyDisk;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;

public class ContainerDiskWriter extends APlayerInventoryContainer {
    private static final int DISK_SLOT_POS_X = 26;
    private static final int DISK_SLOT_POS_Y = 58;
    private static final int RESULT_SLOT_POS_X = 134;
    private static final int RESULT_SLOT_POS_Y = 58;

    private CraftingContainer craftingInventory = new TransientCraftingContainer(this, 1, 1);
    private ResultContainer resultInventory = new ResultContainer();

    public ContainerDiskWriter(MenuType<?> type, int id, Inventory playerInventory) {
        super(type, id, playerInventory);
        this.addSlot(buildDiskSlot(DISK_SLOT_POS_X, DISK_SLOT_POS_Y));
        this.addSlot(buildResultSlot(RESULT_SLOT_POS_X, RESULT_SLOT_POS_Y));
    }

    public ContainerDiskWriter(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
        super(ModContainers.DISKWRITER, id, playerInventory);
        this.addSlot(buildDiskSlot(DISK_SLOT_POS_X, DISK_SLOT_POS_Y));
        this.addSlot(buildResultSlot(RESULT_SLOT_POS_X, RESULT_SLOT_POS_Y));
    }

    @Override
    protected Integer getPlayerInventoryX() {
        return 8;
    }

    @Override
    protected Integer getPlayerInventoryY() {
        return 101;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index == TARGET_CONTAINER_MIN_SLOT_ID + 1) {
                itemstack1.getItem().onCraftedBy(itemstack1, playerIn.level(), playerIn);
                if (!this.moveItemStackTo(itemstack1, 0, TARGET_CONTAINER_MIN_SLOT_ID - 1, false)) {
                    return ItemStack.EMPTY;
                }
                slot.onQuickCraft(itemstack1, itemstack);
            } else if (index >= TARGET_CONTAINER_MIN_SLOT_ID) {
                // Matrix --> Player
                if (!this.moveItemStackTo(itemstack1, 0, TARGET_CONTAINER_MIN_SLOT_ID - 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player --> Target
                if (!this.moveItemStackTo(itemstack1, TARGET_CONTAINER_MIN_SLOT_ID, TARGET_CONTAINER_MIN_SLOT_ID + craftingInventory.getContainerSize(), false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(playerIn, itemstack1);
            if (index == TARGET_CONTAINER_MIN_SLOT_ID + 1) {
                playerIn.drop(itemstack1, false);
            }
        }

        return itemstack;
    }
    
    @Override
    public void slotsChanged(Container container) {
        if (container == craftingInventory && !this.playerInventory.player.level().isClientSide) {
            ServerPlayer serverplayer = (ServerPlayer)this.playerInventory.player;
            ItemStack itemstack = ItemStack.EMPTY;
            
            if(craftingInventory.getItem(0) == null || craftingInventory.getItem(0).isEmpty()) {
                this.resultInventory.setItem(0, itemstack);
            }
   
            this.resultInventory.setItem(0, itemstack);
            this.setRemoteSlot(TARGET_CONTAINER_MIN_SLOT_ID + 1, itemstack);
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), TARGET_CONTAINER_MIN_SLOT_ID + 1, itemstack));
        }
    }
  
    public void writeDisk(String midiUrl, String diskTitle, String diskAuthor) {
        if (!this.playerInventory.player.level().isClientSide) {
            ServerPlayer serverplayer = (ServerPlayer)this.playerInventory.player;
            ItemStack itemstack = craftingInventory.getItem(0).copy();   
            ItemFloppyDisk.setMidiUrl(itemstack, midiUrl);
            ItemFloppyDisk.setDiskTitle(itemstack, diskTitle);
            ItemFloppyDisk.setDiskAuthor(itemstack, diskAuthor);
            this.craftingInventory.setItem(0, ItemStack.EMPTY);
            this.resultInventory.setItem(0, itemstack);
            this.setRemoteSlot(TARGET_CONTAINER_MIN_SLOT_ID, ItemStack.EMPTY);
            this.setRemoteSlot(TARGET_CONTAINER_MIN_SLOT_ID + 1, itemstack);
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), TARGET_CONTAINER_MIN_SLOT_ID + 1, itemstack));
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), TARGET_CONTAINER_MIN_SLOT_ID, ItemStack.EMPTY));
        }
    }

    @Override
    public void removed(Player p_39389_) {
        super.removed(p_39389_);
        this.clearContainer(p_39389_, this.craftingInventory);
        this.clearContainer(p_39389_, this.resultInventory);
    }

    protected Slot buildResultSlot(int xPos, int yPos) {
        return new SlotDiskWriteResult(this.playerInventory.player, craftingInventory, this.resultInventory, 2, xPos, yPos);
    }

    protected Slot buildDiskSlot(int xPos, int yPos) {
        return new Slot(craftingInventory, 0, xPos, yPos) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemFloppyDisk && !ItemFloppyDisk.isWritten(stack);
            }
        };
    }
}
