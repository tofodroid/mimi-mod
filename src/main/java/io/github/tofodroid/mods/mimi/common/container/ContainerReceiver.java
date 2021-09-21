package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

public class ContainerReceiver extends ASwitchboardContainer
{
	public ContainerReceiver(int id, PlayerInventory playerInventory, PacketBuffer extraData) {
		super(ModContainers.RECEIVER, id, playerInventory);
		this.targetInventory = (ItemStackHandler) playerInventory.player.world.getTileEntity(extraData.readBlockPos()).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
		this.addSlot(buildSwitchboardSlot());
	}

	public ContainerReceiver(int id, PlayerInventory playerInventory, BlockPos pos) {
		super(ModContainers.RECEIVER, id, playerInventory);
		
		this.targetInventory = (ItemStackHandler) playerInventory.player.world.getTileEntity(pos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
		this.addSlot(buildSwitchboardSlot());
	}
}