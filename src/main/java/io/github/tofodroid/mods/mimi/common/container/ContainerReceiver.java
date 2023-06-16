package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class ContainerReceiver extends ASwitchboardContainer
{
	public ContainerReceiver(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
		super(ModContainers.RECEIVER, id, playerInventory);
		this.targetInventory =  playerInventory.player.level().getBlockEntity(extraData.readBlockPos()).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
		this.addSlot(buildSwitchboardSlot());
	}

	public ContainerReceiver(int id, Inventory playerInventory, BlockPos pos) {
		super(ModContainers.RECEIVER, id, playerInventory);
		
		this.targetInventory =  playerInventory.player.level().getBlockEntity(pos).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
		this.addSlot(buildSwitchboardSlot());
	}
}