package io.github.tofodroid.mods.mimi.common.container;

import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.SlotItemHandler;

public class ContainerMechanicalMaestro extends APlayerInventoryContainer {
	private static final int INSTRUMENT_SLOT_L_X = 46;
	private static final int INSTRUMENT_SLOT_L_Y = 35;
	private static final int INSTRUMENT_SLOT_M_X = 82;
	private static final int INSTRUMENT_SLOT_M_Y = 35;
	private static final int INSTRUMENT_SLOT_R_X = 118;
	private static final int INSTRUMENT_SLOT_R_Y = 35;

	protected IItemHandler targetInventory;
	private final BlockPos tilePos;

    @SuppressWarnings("null")
	public ContainerMechanicalMaestro(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
		super(ModContainers.BROADCASTER, id, playerInventory);
		tilePos = extraData.readBlockPos();
		this.targetInventory =  playerInventory.player.level().getBlockEntity(tilePos).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
		this.addSlot(buildInstrumentSlot(INSTRUMENT_SLOT_L_X, INSTRUMENT_SLOT_L_Y));
		this.addSlot(buildInstrumentSlot(INSTRUMENT_SLOT_M_X, INSTRUMENT_SLOT_M_Y));
		this.addSlot(buildInstrumentSlot(INSTRUMENT_SLOT_R_X, INSTRUMENT_SLOT_R_Y));
	}

    @SuppressWarnings("null")
	public ContainerMechanicalMaestro(int id, Inventory playerInventory, BlockPos pos) {
		super(ModContainers.BROADCASTER, id, playerInventory);
		tilePos = pos;
		this.targetInventory =  playerInventory.player.level().getBlockEntity(tilePos).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
		this.addSlot(buildInstrumentSlot(INSTRUMENT_SLOT_L_X, INSTRUMENT_SLOT_L_Y));
		this.addSlot(buildInstrumentSlot(INSTRUMENT_SLOT_M_X, INSTRUMENT_SLOT_M_Y));
		this.addSlot(buildInstrumentSlot(INSTRUMENT_SLOT_R_X, INSTRUMENT_SLOT_R_Y));
	}

	public TileMechanicalMaestro getMechanicalMaestroTile() {
		BlockEntity ent = playerInventory.player.level().getBlockEntity(tilePos);

		if(ent != null && ent instanceof TileMechanicalMaestro) {
			return (TileMechanicalMaestro)ent;
		}
		return null;
	}
	
	
    protected Slot buildInstrumentSlot(int xPos, int yPos) {
        return new SlotItemHandler(targetInventory, 1, xPos, yPos) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof IInstrumentItem;
            }
        };
    }
		
    @Override
    protected Integer getPlayerInventoryX() {
        return 183;
    }

    @Override
    protected Integer getPlayerInventoryY() {
        return 149;
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
				if (!this.moveItemStackTo(itemstack1, TARGET_CONTAINER_MIN_SLOT_ID, TARGET_CONTAINER_MIN_SLOT_ID + 1, false)) {
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
	
	public IItemHandler getTargetInventory() {
		return targetInventory;
	}
}
