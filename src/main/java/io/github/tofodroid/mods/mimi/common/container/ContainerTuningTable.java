package io.github.tofodroid.mods.mimi.common.container;

import java.util.Optional;

import io.github.tofodroid.mods.mimi.common.container.slot.SlotTuningResult;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentHandheld;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentBlock;
import io.github.tofodroid.mods.mimi.common.recipe.ModRecipes;
import io.github.tofodroid.mods.mimi.common.recipe.TuningTableRecipe;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.game.ClientboundContainerSetSlotPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.inventory.ResultContainer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.TransientCraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

public class ContainerTuningTable extends APlayerInventoryContainer {
    private static final int INSTRUMENT_SLOT_POS_X = 27;
    private static final int INSTRUMENT_SLOT_POS_Y = 38;
    private static final int MODIFIER_SLOT_POS_X = 76;
    private static final int MODIFiER_SLOT_POS_Y = 38;
    private static final int RESULT_SLOT_POS_X = 134;
    private static final int RESULT_SLOT_POS_Y = 38;

    private CraftingContainer craftingInventory = new TransientCraftingContainer(this, 1, 2);
    private ResultContainer resultInventory = new ResultContainer();

    public ContainerTuningTable(int id, Inventory playerInventory) {
        super(ModContainers.TUNINGTABLE, id, playerInventory);
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
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);

        if (slot != null && slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();

            if (index == TARGET_CONTAINER_MIN_SLOT_ID + 2) {
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
            if (index == TARGET_CONTAINER_MIN_SLOT_ID + 2) {
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
            Optional<RecipeHolder<TuningTableRecipe>> optional = serverplayer.level().getServer().getRecipeManager().getRecipeFor(ModRecipes.TUNING_TYPE, this.craftingInventory.asCraftInput(), serverplayer.level());
            
            if (optional.isPresent()) {
               if (this.resultInventory.setRecipeUsed(serverplayer.level(), serverplayer, optional.get())) {
                  itemstack = optional.get().value().assemble(this.craftingInventory.asCraftInput(), null);
               }
            }
   
            this.resultInventory.setItem(0, itemstack);
            this.setRemoteSlot(TARGET_CONTAINER_MIN_SLOT_ID + 2, itemstack);
            serverplayer.connection.send(new ClientboundContainerSetSlotPacket(this.containerId, this.incrementStateId(), TARGET_CONTAINER_MIN_SLOT_ID + 2, itemstack));
        }
    }

    @Override
    public void removed(Player p_39389_) {
        super.removed(p_39389_);
        this.clearContainer(p_39389_, this.craftingInventory);
    }

    protected Slot buildResultSlot(int xPos, int yPos) {
        return new SlotTuningResult(this.playerInventory.player, craftingInventory, this.resultInventory, 2, xPos, yPos);
    }

    protected Slot buildModifierSlot(int xPos, int yPos) {
        return new Slot(craftingInventory, 1, xPos, yPos);
    }

    protected Slot buildInstrumentSlot(int xPos, int yPos) {
        return new Slot(craftingInventory, 0, xPos, yPos) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemInstrumentHandheld || stack.getItem() instanceof ItemInstrumentBlock;
            }
        };
    }
}
