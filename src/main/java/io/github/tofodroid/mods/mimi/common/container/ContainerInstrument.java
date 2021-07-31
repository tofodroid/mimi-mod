package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;

public class ContainerInstrument extends ASwitchboardContainer {
    private static final int SWITCHBOARD_SLOT_POS_X = 134;
	private static final int SWITCHBOARD_SLOT_POS_Y = 219;
	protected static final int INVENTORY_PLAYER_START_X = 156;
	protected static final int INVENTORY_PLAYER_START_Y = 182;

	private final Hand handIn;
	private final BlockPos tilePos;
	private final Boolean handheld;
	private final Byte instrumentId;

	public ContainerInstrument(int id, PlayerInventory playerInventory) {
		super(ModContainers.INSTRUMENT, id, playerInventory);
		handIn = null;
		tilePos = null;
		handheld = null;
		instrumentId = null;
	}

	public ContainerInstrument(int id, PlayerInventory playerInventory, PacketBuffer extraData) {
		super(ModContainers.INSTRUMENT, id, playerInventory);
		instrumentId = extraData.readByte();
		handheld = extraData.readBoolean();
		
		if(handheld) {
			tilePos = null;
			handIn = extraData.readBoolean() ? Hand.MAIN_HAND : Hand.OFF_HAND;
			targetInventory = ItemInstrument.getInventoryHandler(playerInventory.player.getHeldItem(handIn));
		} else {
			handIn = null;
			tilePos = extraData.readBlockPos();
			targetInventory = (ItemStackHandler) playerInventory.player.world.getTileEntity(tilePos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
		}
		
		this.addSlot(buildSwitchboardSlot(SWITCHBOARD_SLOT_POS_X, SWITCHBOARD_SLOT_POS_Y));
	}
	
	public ContainerInstrument(int id, PlayerInventory playerInventory, Byte instrumentId, Hand handIn) {
		super(ModContainers.INSTRUMENT, id, playerInventory);
		handheld = true;
		this.instrumentId = instrumentId;
		this.handIn = handIn;
		tilePos = null;
		targetInventory =  ItemInstrument.getInventoryHandler(playerInventory.player.getHeldItem(handIn));

		this.addSlot(buildSwitchboardSlot(SWITCHBOARD_SLOT_POS_X, SWITCHBOARD_SLOT_POS_Y));
	}
	
	public ContainerInstrument(int id, PlayerInventory playerInventory, Byte instrumentId, BlockPos tilPos) {
		super(ModContainers.INSTRUMENT, id, playerInventory);
		handheld = false;
		this.instrumentId = instrumentId;
		this.tilePos = tilPos;
		handIn = null;
		targetInventory = (ItemStackHandler) playerInventory.player.world.getTileEntity(tilePos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
		
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

	@Override
	public Boolean updateSelectedSwitcboard(ServerPlayerEntity player, UUID newSourceId, String newChannelString, String newNoteString) {
		if(super.updateSelectedSwitcboard(player, newSourceId, newChannelString, newNoteString)) {
			this.saveToInventory(player);
			return true;
		}

		return false;
	}
    
	@Override
	public void onContainerClosed(PlayerEntity player) {
		this.saveToInventory(player);
		super.onContainerClosed(player);
	}

	public void saveToInventory(PlayerEntity player) {
		if(handheld && handIn != null) {
			ItemStack instrumentStack = player.getHeldItem(handIn);
			if(instrumentStack.getItem() instanceof ItemInstrument) {
				CompoundNBT tag = instrumentStack.getOrCreateTag();
				tag.put(ItemInstrument.INVENTORY_TAG, targetInventory.serializeNBT());
			}
		}
	}

	public Byte getInstrumentId() {
		return instrumentId;
	}

	public Boolean isHandheld() {
		return handheld;
	}

	public BlockPos getTilePos() {
		return tilePos;
	}

	public Hand getHandIn() {
		return handIn;
	}
}