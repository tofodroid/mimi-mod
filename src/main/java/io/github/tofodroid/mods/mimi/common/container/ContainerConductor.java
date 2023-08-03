package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

public class ContainerConductor extends ASwitchboardContainer
{
	public ContainerConductor(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
		super(ModContainers.CONDUCTOR, id, playerInventory);
		this.targetInventory = playerInventory.player.level().getBlockEntity(extraData.readBlockPos()).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
		this.addSlot(buildSwitchboardSlot());
	}

	public ContainerConductor(int id, Inventory playerInventory, BlockPos pos) {
		super(ModContainers.CONDUCTOR, id, playerInventory);
		this.targetInventory = playerInventory.player.level().getBlockEntity(pos).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
		this.addSlot(buildSwitchboardSlot());
	}
}