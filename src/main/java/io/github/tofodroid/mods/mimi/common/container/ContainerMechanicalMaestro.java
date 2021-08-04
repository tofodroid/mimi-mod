package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentBlock;

public class ContainerMechanicalMaestro extends ASwitchboardContainer
{		
	private static final int INSTRUMENT_SLOT_POS_X = 103;
	private static final int INSTRUMENT_SLOT_POS_Y = 70;
	private static final int SWITCHBOARD_SLOT_POS_X = 103;
	private static final int SWITCHBOARD_SLOT_POS_Y = 98;
	protected static final int INVENTORY_PLAYER_START_X = 135;
	protected static final int INVENTORY_PLAYER_START_Y = 31;
	
	private final BlockPos tilePos;

	public ContainerMechanicalMaestro(int id, PlayerInventory playerInventory, PacketBuffer extraData) {
		super(ModContainers.MECHANICALMAESTRO, id, playerInventory);
		tilePos = extraData.readBlockPos();
		this.targetInventory = (ItemStackHandler) playerInventory.player.world.getTileEntity(tilePos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
		this.addSlot(buildSwitchboardSlot(SWITCHBOARD_SLOT_POS_X, SWITCHBOARD_SLOT_POS_Y));
		this.addSlot(buildInstrumentSlot(INSTRUMENT_SLOT_POS_X, INSTRUMENT_SLOT_POS_Y));
	}

	public ContainerMechanicalMaestro(int id, PlayerInventory playerInventory, BlockPos pos) {
		super(ModContainers.MECHANICALMAESTRO, id, playerInventory);
		tilePos = pos;
		this.targetInventory = (ItemStackHandler) playerInventory.player.world.getTileEntity(tilePos).getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY).orElseThrow(NullPointerException::new);
		this.addSlot(buildSwitchboardSlot(SWITCHBOARD_SLOT_POS_X, SWITCHBOARD_SLOT_POS_Y));
		this.addSlot(buildInstrumentSlot(INSTRUMENT_SLOT_POS_X, INSTRUMENT_SLOT_POS_Y));
	}
	
	@Override
	protected Integer getPlayerInventoryX() {
		return INVENTORY_PLAYER_START_X;
	}

	@Override
	protected Integer getPlayerInventoryY() {
		return INVENTORY_PLAYER_START_Y;
	}

	public BlockPos getTilePos() {
		return tilePos;
	}

    protected Slot buildInstrumentSlot(int xPos, int yPos) {
        return new SlotItemHandler(targetInventory, 1, xPos, yPos) {
            @Override
            public boolean isItemValid(ItemStack stack) {
                return stack.getItem() instanceof ItemInstrument || stack.getItem() instanceof ItemInstrumentBlock;
            }
        };
    }
}