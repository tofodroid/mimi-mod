package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ContainerAdvListener extends ASwitchboardContainer
{		
	private static final int SWITCHBOARD_SLOT_POS_X = 109;
	private static final int SWITCHBOARD_SLOT_POS_Y = 96;
	protected static final int INVENTORY_PLAYER_START_X = 131;
	protected static final int INVENTORY_PLAYER_START_Y = 59;

	public ContainerAdvListener(int id, PlayerInventory playerInventory, PacketBuffer extraData) {
		super(ModContainers.ADVLISTENER, id, playerInventory);
		this.targetInventory = (ItemStackHandler) playerInventory.player.world.getTileEntity(extraData.readBlockPos()).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
		this.addSlot(buildSwitchboardSlot(SWITCHBOARD_SLOT_POS_X, SWITCHBOARD_SLOT_POS_Y));
	}

	public ContainerAdvListener(int id, PlayerInventory playerInventory, BlockPos pos) {
		super(ModContainers.ADVLISTENER, id, playerInventory);
		this.targetInventory = (ItemStackHandler) playerInventory.player.world.getTileEntity(pos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
		this.addSlot(buildSwitchboardSlot(SWITCHBOARD_SLOT_POS_X, SWITCHBOARD_SLOT_POS_Y));
	}
	
	@Override
	protected Integer getPlayerInventoryX() {
		return INVENTORY_PLAYER_START_X;
	}

	@Override
	protected Integer getPlayerInventoryY() {
		return INVENTORY_PLAYER_START_Y;
	}
	
	public ItemStack getSelectedInstrument() {
		return this.getSlot(APlayerInventoryContainer.TARGET_CONTAINER_MIN_SLOT_ID+1).getStack();
	}
}