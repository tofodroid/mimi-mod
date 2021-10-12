package io.github.tofodroid.mods.mimi.common.container;

import java.util.Optional;

import io.github.tofodroid.mods.mimi.common.container.slot.SlotTuningResult;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentBlock;
import io.github.tofodroid.mods.mimi.common.recipe.TuningTableRecipe;

import net.minecraft.inventory.CraftResultInventory;
import net.minecraft.inventory.CraftingInventory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.inventory.container.Slot;
import net.minecraft.network.play.server.SSetSlotPacket;
import net.minecraft.network.PacketBuffer;

public class ContainerTuningTable extends APlayerInventoryContainer {
    private static final int INSTRUMENT_SLOT_POS_X = 27;
    private static final int INSTRUMENT_SLOT_POS_Y = 38;
    private static final int MODIFIER_SLOT_POS_X = 76;
    private static final int MODIFiER_SLOT_POS_Y = 38;
    private static final int RESULT_SLOT_POS_X = 134;
    private static final int RESULT_SLOT_POS_Y = 38;

    private CraftingInventory craftingInventory = new CraftingInventory(this, 1, 2);
    private CraftResultInventory resultInventory = new CraftResultInventory();

    public ContainerTuningTable(ContainerType<?> type, int id, PlayerInventory playerInventory) {
        super(type, id, playerInventory);
        this.addSlot(buildInstrumentSlot(INSTRUMENT_SLOT_POS_X, INSTRUMENT_SLOT_POS_Y));
        this.addSlot(buildModifierSlot(MODIFIER_SLOT_POS_X, MODIFiER_SLOT_POS_Y));
        this.addSlot(buildResultSlot(RESULT_SLOT_POS_X, RESULT_SLOT_POS_Y));
    }

    public ContainerTuningTable(int id, PlayerInventory playerInventory, PacketBuffer extraData) {
        super(ModContainers.TUNINGTABLE, id, playerInventory);
        this.addSlot(buildInstrumentSlot(INSTRUMENT_SLOT_POS_X, INSTRUMENT_SLOT_POS_Y));
        this.addSlot(buildModifierSlot(MODIFIER_SLOT_POS_X, MODIFiER_SLOT_POS_Y));
        this.addSlot(buildResultSlot(RESULT_SLOT_POS_X, RESULT_SLOT_POS_Y));
    }

    @Override
    protected Integer getPlayerInventoryX() {
        return 8;
    }

    @Override
    protected Integer getPlayerInventoryY() {
        return 93;
    }

    @Override
    public ItemStack transferStackInSlot(PlayerEntity playerIn, int index) {
        // Below code taken from Vanilla Chest Container
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            // Return Empty Stack if Cannot Merge
            if (index == TARGET_CONTAINER_MIN_SLOT_ID + 2) {
                // Result --> Player
                if (!this.mergeItemStack(itemstack1, 0, TARGET_CONTAINER_MIN_SLOT_ID - 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= TARGET_CONTAINER_MIN_SLOT_ID) {
                // Matrix --> Player
                if (!this.mergeItemStack(itemstack1, 0, TARGET_CONTAINER_MIN_SLOT_ID - 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player --> Target
                if (!this.mergeItemStack(itemstack1, TARGET_CONTAINER_MIN_SLOT_ID,
                        TARGET_CONTAINER_MIN_SLOT_ID + craftingInventory.getSizeInventory(), false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (itemstack1.isEmpty()) {
                slot.putStack(ItemStack.EMPTY);
            } else {
                slot.onTake(playerIn, itemstack1);
            }
        }

        return itemstack;
    }

    @Override
    public void onCraftMatrixChanged(IInventory inventoryIn) {
        updateCraftingResult(this.windowId, this.playerInventory.player.world, this.playerInventory.player,
                this.craftingInventory, this.resultInventory);
    }

    public void clear() {
        this.craftingInventory.clear();
        this.resultInventory.clear();
    }

    @Override
    public void onContainerClosed(PlayerEntity playerIn) {
        super.onContainerClosed(playerIn);
        this.clearContainer(playerIn, playerIn.world, this.craftingInventory);
    }

    protected static void updateCraftingResult(int id, World world, PlayerEntity player, CraftingInventory inventory, CraftResultInventory inventoryResult) {
        if (!world.isRemote) {
            ServerPlayerEntity serverplayerentity = (ServerPlayerEntity) player;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional<TuningTableRecipe> optional = world.getServer().getRecipeManager()
                    .getRecipe(TuningTableRecipe.TYPE, inventory, world);
            if (optional.isPresent()) {
                TuningTableRecipe recipe = optional.get();
                if (inventoryResult.canUseRecipe(world, serverplayerentity, recipe)) {
                    itemstack = recipe.getCraftingResult(inventory);
                }
            }

            inventoryResult.setInventorySlotContents(0, itemstack);
            serverplayerentity.connection.sendPacket(new SSetSlotPacket(id, TARGET_CONTAINER_MIN_SLOT_ID + 2, itemstack));
        }
    }

    protected Slot buildResultSlot(int xPos, int yPos) {
        return new SlotTuningResult(this, craftingInventory, resultInventory, 2, xPos, yPos);
    }

    protected Slot buildModifierSlot(int xPos, int yPos) {
        return new Slot(craftingInventory, 1, xPos, yPos);
    }

    protected Slot buildInstrumentSlot(int xPos, int yPos) {
        return new Slot(craftingInventory, 0, xPos, yPos) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ItemInstrument || stack.getItem() instanceof ItemInstrumentBlock;
            }
        };
    }
}
