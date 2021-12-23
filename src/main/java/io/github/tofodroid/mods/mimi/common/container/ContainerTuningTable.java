package io.github.tofodroid.mods.mimi.common.container;

import java.util.Optional;

import io.github.tofodroid.mods.mimi.common.container.slot.SlotTuningResult;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentBlock;
import io.github.tofodroid.mods.mimi.common.recipe.TuningTableRecipe;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.ResultSlot;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ContainerTuningTable extends APlayerInventoryContainer {
    private static final int INSTRUMENT_SLOT_POS_X = 27;
    private static final int INSTRUMENT_SLOT_POS_Y = 38;
    private static final int MODIFIER_SLOT_POS_X = 76;
    private static final int MODIFiER_SLOT_POS_Y = 38;
    private static final int RESULT_SLOT_POS_X = 134;
    private static final int RESULT_SLOT_POS_Y = 38;

    private CraftingContainer craftingInventory = new CraftingContainer(this, 1, 2);
    private ResultContainer resultInventory = new ResultContainer();

    public ContainerTuningTable(MenuType<?> type, int id, Inventory playerInventory) {
        super(type, id, playerInventory);
        this.addSlot(buildInstrumentSlot(INSTRUMENT_SLOT_POS_X, INSTRUMENT_SLOT_POS_Y));
        this.addSlot(buildModifierSlot(MODIFIER_SLOT_POS_X, MODIFiER_SLOT_POS_Y));
        this.addSlot(buildResultSlot(RESULT_SLOT_POS_X, RESULT_SLOT_POS_Y));
    }

    public ContainerTuningTable(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
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
    public ItemStack quickMoveStack(Player playerIn, int index) {
        // Below code taken from Vanilla Chest Container
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            // Return Empty Stack if Cannot Merge
            if (index == TARGET_CONTAINER_MIN_SLOT_ID + 2) {
                // Result --> Player
                if (!this.moveItemStackTo(itemstack1, 0, TARGET_CONTAINER_MIN_SLOT_ID - 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index >= TARGET_CONTAINER_MIN_SLOT_ID) {
                // Matrix --> Player
                if (!this.moveItemStackTo(itemstack1, 0, TARGET_CONTAINER_MIN_SLOT_ID - 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else {
                // Player --> Target
                if (!this.moveItemStackTo(itemstack1, TARGET_CONTAINER_MIN_SLOT_ID,
                        TARGET_CONTAINER_MIN_SLOT_ID + craftingInventory.getContainerSize(), false)) {
                    return ItemStack.EMPTY;
                }
            }

            if (!itemstack.isEmpty()) {
                slot.onTake(playerIn, itemstack1);
            } else {
              slot.set(ItemStack.EMPTY);  
            }
        }

        return itemstack;
    }

    /*
    @Override
    public void onCraftMatrixChanged(Inventory inventoryIn) {
        updateCraftingResult(this.windowId, this.playerInventory.player.level, this.playerInventory.player,
                this.craftingInventory, this.resultInventory);
    }
    */

    public void clear() {
        this.craftingInventory.clearContent();
        this.resultInventory.clearContent();
    }

    @Override
    public void removed(Player playerIn) {
        super.removed(playerIn);
        //'this.clearContainer(playerIn, playerIn.level, this.craftingInventory.clearContent(););
    }

    /*
    protected static void updateCraftingResult(int id, Level world, Player player, CraftingContainer inventory, ResultContainer inventoryResult) {
        if (!world.isClientSide) {
            ServerPlayer serverplayerentity = (ServerPlayer) player;
            ItemStack itemstack = ItemStack.EMPTY;
            Optional<TuningTableRecipe> optional = world.getServer().getRecipeManager()
                    .getRecipeFor(TuningTableRecipe.TYPE, inventory, world);
            if (optional.isPresent()) {
                TuningTableRecipe recipe = optional.get();
                if (inventoryResult.stillValid(serverplayerentity)) {
                    itemstack = recipe.getResultItem();
                }
            }

            inventoryResult.setItem(0, itemstack);
            serverplayerentity.connection.sendPacket(new Packet(id, TARGET_CONTAINER_MIN_SLOT_ID + 2, itemstack));
        }
    }
    */

    protected Slot buildResultSlot(int xPos, int yPos) {
        return new ResultSlot(this.playerInventory.player, craftingInventory, this.resultInventory, 2, xPos, yPos);
    }

    protected Slot buildModifierSlot(int xPos, int yPos) {
        return new Slot(craftingInventory, 1, xPos, yPos);
    }

    protected Slot buildInstrumentSlot(int xPos, int yPos) {
        return new Slot(craftingInventory, 0, xPos, yPos) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemInstrument || stack.getItem() instanceof ItemInstrumentBlock;
            }
        };
    }
}
