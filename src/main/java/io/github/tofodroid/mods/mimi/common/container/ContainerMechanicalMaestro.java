package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.SlotItemHandler;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentBlock;

public class ContainerMechanicalMaestro extends ASwitchboardContainer {
	private static final int SWITCHBOARD_SLOT_POS_Y = 211;
	private static final int INSTRUMENT_SLOT_POS_X = 127;
	private static final int INSTRUMENT_SLOT_POS_Y = 174;
	
	private final BlockPos tilePos;

	public ContainerMechanicalMaestro(int id, Inventory playerInventory, FriendlyByteBuf extraData) {
		super(ModContainers.MECHANICALMAESTRO, id, playerInventory);
		tilePos = extraData.readBlockPos();
		this.targetInventory =  playerInventory.player.level().getBlockEntity(tilePos).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
		this.addSlot(buildSwitchboardSlot());
		this.addSlot(buildInstrumentSlot(INSTRUMENT_SLOT_POS_X, INSTRUMENT_SLOT_POS_Y));
	}

	public ContainerMechanicalMaestro(int id, Inventory playerInventory, BlockPos pos) {
		super(ModContainers.MECHANICALMAESTRO, id, playerInventory);
		tilePos = pos;
		this.targetInventory =  playerInventory.player.level().getBlockEntity(tilePos).getCapability(ForgeCapabilities.ITEM_HANDLER).orElseThrow(NullPointerException::new);
		this.addSlot(buildSwitchboardSlot());
		this.addSlot(buildInstrumentSlot(INSTRUMENT_SLOT_POS_X, INSTRUMENT_SLOT_POS_Y));
	}
	
	@Override
	protected Integer getSwitchboardSlotY() {
		return SWITCHBOARD_SLOT_POS_Y;
	}
	
	public BlockPos getTilePos() {
		return tilePos;
	}

    protected Slot buildInstrumentSlot(int xPos, int yPos) {
        return new SlotItemHandler(targetInventory, 1, xPos, yPos) {
            @Override
            public boolean mayPlace(ItemStack stack) {
                return stack.getItem() instanceof ItemInstrument || stack.getItem() instanceof ItemInstrumentBlock;
            }
        };
    }
}