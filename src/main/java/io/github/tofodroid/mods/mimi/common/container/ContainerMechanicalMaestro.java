package io.github.tofodroid.mods.mimi.common.container;

import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

public class ContainerMechanicalMaestro extends APlayerInventoryContainer {
	private static final int INSTRUMENT_SLOT_L_X = 46;
	private static final int INSTRUMENT_SLOT_L_Y = 35;
	private static final int INSTRUMENT_SLOT_M_X = 82;
	private static final int INSTRUMENT_SLOT_M_Y = 35;
	private static final int INSTRUMENT_SLOT_R_X = 118;
	private static final int INSTRUMENT_SLOT_R_Y = 35;

	private Container targetContainer;

	public ContainerMechanicalMaestro(int id, Inventory playerInventory) {
		this(id, playerInventory, new SimpleContainer(3));
	}

	public ContainerMechanicalMaestro(int id, Inventory playerInventory, Container targetContainer) {
		super(ModContainers.MECHANICALMAESTRO, id, playerInventory);
		this.targetContainer = targetContainer;
		this.addSlot(buildInstrumentSlot(targetContainer, INSTRUMENT_SLOT_L_X, INSTRUMENT_SLOT_L_Y, 0));
		this.addSlot(buildInstrumentSlot(targetContainer, INSTRUMENT_SLOT_M_X, INSTRUMENT_SLOT_M_Y, 1));
		this.addSlot(buildInstrumentSlot(targetContainer, INSTRUMENT_SLOT_R_X, INSTRUMENT_SLOT_R_Y, 2));
		this.targetContainer.startOpen(playerInventory.player);
	}
	
    protected Slot buildInstrumentSlot(Container targetContainer, int xPos, int yPos, int index) {
		return new Slot(targetContainer, index, xPos, yPos){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof IInstrumentItem;
            }
        };
    }

    @Override
    protected Integer getPlayerInventoryX() {
        return 10;
    }

    @Override
    protected Integer getPlayerInventoryY() {
        return 85;
    }
    
	@Override
	public ItemStack quickMoveStack(Player playerIn, int index) {
		// Below code taken from Vanilla Chest Container
		ItemStack itemstack = ItemStack.EMPTY;
		Slot slot = this.slots.get(index);
		if (slot != null && slot.hasItem()) {
			ItemStack itemstack1 = slot.getItem().copy();
			itemstack = itemstack1.copy();

			// Return Empty Stack if Cannot Merge
			if (index >= TARGET_CONTAINER_MIN_SLOT_ID) {
				// Target --> Player
				if (!this.moveItemStackTo(itemstack1, 0, TARGET_CONTAINER_MIN_SLOT_ID-1, false)) {
					return ItemStack.EMPTY;
				}
			} else {
				// Player --> Target
				if (!this.moveItemStackTo(itemstack1, TARGET_CONTAINER_MIN_SLOT_ID, TARGET_CONTAINER_MIN_SLOT_ID + 2, false)) {
					return ItemStack.EMPTY;
				}
			}

			if (itemstack1.isEmpty()) {
				slot.set(ItemStack.EMPTY);
			} else {
				slot.set(itemstack1);
			}
		}

	  return itemstack;
	}
	
	@Override
	public boolean stillValid(Player p_40195_) {
		return this.targetContainer.stillValid(p_40195_);
	}

	@Override
	public void removed(Player p_40197_) {
		super.removed(p_40197_);
		this.targetContainer.stopOpen(p_40197_);
	}
}
