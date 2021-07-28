package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ContainerReceiver extends ASwitchboardContainer
{		
	private static final int SWITCHBOARD_SLOT_POS_X = 109;
	private static final int SWITCHBOARD_SLOT_POS_Y = 141;
	protected static final int INVENTORY_PLAYER_START_X = 131;
	protected static final int INVENTORY_PLAYER_START_Y = 104;

	public ContainerReceiver(int id, PlayerInventory playerInventory, PacketBuffer extraData) {
		super(ModContainers.RECEIVER, id, playerInventory);
		this.targetInventory = (ItemStackHandler) playerInventory.player.world.getTileEntity(extraData.readBlockPos()).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
		this.addSlot(buildSwitchboardSlot(SWITCHBOARD_SLOT_POS_X, SWITCHBOARD_SLOT_POS_Y));
	}

	public ContainerReceiver(int id, PlayerInventory playerInventory, BlockPos pos) {
		super(ModContainers.RECEIVER, id, playerInventory);
		
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
}