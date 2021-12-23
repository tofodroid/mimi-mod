package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.container.slot.SlotDisabled;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;

public class ContainerInstrument extends ASwitchboardContainer {
    private static final int SWITCHBOARD_SLOT_POS_X = 127;
	private static final int SWITCHBOARD_SLOT_POS_Y = 219;
	protected static final int INVENTORY_PLAYER_START_X = 156;
	protected static final int INVENTORY_PLAYER_START_Y = 182;

	private final InteractionHand handIn;
	private final BlockPos tilePos;
	private final Boolean handheld;
	private final Byte instrumentId;

	public ContainerInstrument(int id, Inventory playerInventory) {
		super(ModContainers.INSTRUMENT, id, playerInventory);
		handIn = null;
		tilePos = null;
		handheld = null;
		instrumentId = null;
	}

	public ContainerInstrument(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
		super(ModContainers.INSTRUMENT, id, playerInventory);
		instrumentId = extraData.readByte();
		handheld = extraData.readBoolean();
		
		if(handheld) {
			tilePos = null;
			handIn = extraData.readBoolean() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
			targetInventory = ItemInstrument.getInventoryHandler(playerInventory.player.getItemInHand(handIn));
		} else {
			handIn = null;
			tilePos = extraData.readBlockPos();
			targetInventory = (ItemStackHandler) playerInventory.player.level.getBlockEntity(tilePos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
		}
		
		this.addSlot(buildSwitchboardSlot());
	}
	
	public ContainerInstrument(int id, Inventory playerInventory, Byte instrumentId, InteractionHand handIn) {
		super(ModContainers.INSTRUMENT, id, playerInventory);
		handheld = true;
		this.instrumentId = instrumentId;
		this.handIn = handIn;
		tilePos = null;
		targetInventory =  ItemInstrument.getInventoryHandler(playerInventory.player.getItemInHand(handIn));

		this.addSlot(buildSwitchboardSlot());
	}
	
	public ContainerInstrument(int id, Inventory playerInventory, Byte instrumentId, BlockPos tilPos) {
		super(ModContainers.INSTRUMENT, id, playerInventory);
		handheld = false;
		this.instrumentId = instrumentId;
		this.tilePos = tilPos;
		handIn = null;
		targetInventory = (ItemStackHandler) playerInventory.player.level.getBlockEntity(tilePos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
		
		this.addSlot(buildSwitchboardSlot());
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
	protected Integer getSwitchboardSlotX() {
		return SWITCHBOARD_SLOT_POS_X;
	}

	@Override
	protected Integer getSwitchboardSlotY() {
		return SWITCHBOARD_SLOT_POS_Y;
	}
	
    @Override
	protected Slot buildPlayerSlot(Inventory playerInventory, int slot, int xPos, int yPos) {
		if(playerInventory.getItem(slot).getItem() instanceof ItemInstrument) {
			return new SlotDisabled(playerInventory, slot, xPos, yPos);
		} else {
			return new Slot(playerInventory, slot, xPos, yPos);
		}
	}

	@Override
	public Boolean updateSelectedSwitchboard(ServerPlayer player, UUID newSourceId, String newSourceName, Byte newFilterOct, Byte newFilterNote, Boolean newInvertNoteOct, String newChannelString, Byte newInstrumentId, Boolean newInvertInstrument, Boolean newSysInput, Boolean newPublicBroadcast, Byte newBroadcastNote, Byte newVolume) {
		if(super.updateSelectedSwitchboard(player, newSourceId, newSourceName, newFilterOct, newFilterNote, newInvertNoteOct, newChannelString, newInstrumentId, newInvertInstrument, newSysInput, newPublicBroadcast, newBroadcastNote, newVolume)) {
			this.saveToInventory(player);
			return true;
		}

		return false;
	}
    
	@Override
	public void removed(Player player) {
		this.saveToInventory(player);
		super.removed(player);
	}

	public void saveToInventory(Player player) {
		if(handheld && handIn != null) {
			ItemStack instrumentStack = player.getItemInHand(handIn);
			if(instrumentStack.getItem() instanceof ItemInstrument) {
				CompoundTag tag = instrumentStack.getOrCreateTag();
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

	public InteractionHand getHandIn() {
		return handIn;
	}
}
