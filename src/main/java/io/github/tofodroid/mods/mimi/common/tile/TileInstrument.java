package io.github.tofodroid.mods.mimi.common.tile;

import org.joml.Vector3d;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.entity.EntitySeat;
import io.github.tofodroid.mods.mimi.common.item.IColorableItem;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.server.midi.receiver.ServerMusicReceiverManager;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

public class TileInstrument extends AStaticInventoryTile {
    public static final String COLOR_TAG = "color";
    protected EntitySeat currentSeat = null;
    protected Integer color;

    public TileInstrument(BlockPos pos, BlockState state) {
        super(ModTiles.INSTRUMENT, pos, state, 1);
    }

    @SuppressWarnings("resource")
    public void attemptSit(Player player) {
        if(player.level().isClientSide) {
            return;
        }

        EntitySeat newSeat = EntitySeat.create(player.level(), this.getBlockPos(), this.getSeatOffset(getBlockState()), player);

        if(newSeat != null) {
            this.currentSeat = newSeat;
        }
    }

    public Player getCurrentPlayer() {
        if(this.currentSeat != null && !this.currentSeat.isRemoved()) {
            return this.currentSeat.getRider();
        }
        return null;
    }
    
    protected Vector3d getSeatOffset(BlockState state) {
        switch(state.getValue(BlockInstrument.DIRECTION)) {
            case NORTH:
                return new Vector3d(0.5, 0, 0.05);
            case SOUTH:
                return new Vector3d(0.5, 0, 0.95);
            case EAST:
                return new Vector3d(0.95, 0, 0.5);
            case WEST:
                return new Vector3d(0.05, 0, 0.5);
            default:
                return new Vector3d(0.5, 0, 0.05);
        }
    }

    public void setInstrumentStack(ItemStack stack) {
        if(stack.getItem() instanceof IInstrumentItem) {
            this.setItem(0, stack);
            
            if(this.blockInstrument().isColorable() && ((IColorableItem)stack.getItem()).hasColor(stack)) {
                this.color = ((IColorableItem)stack.getItem()).getColor(stack);
            }

            if(this.getCurrentPlayer() != null) {
                ServerMusicReceiverManager.loadEntityInstrumentReceivers(this.getCurrentPlayer());
            }
        }
    }

    public ItemStack getInstrumentStack() {
        if(items.isEmpty() || items.get(0) == null) {
            return ItemStack.EMPTY;
        } else {
            return items.get(0);
        }
    }

    public Byte getInstrumentId() {
        return this.blockInstrument().getInstrumentId();
    }

    public Boolean hasColor() {
        return color != null && this.blockInstrument().isColorable();
    }

    public Integer getColor() { 
        if(!this.blockInstrument().isColorable()) {
            return -1;
        }

        return hasColor() ? color : this.blockInstrument().getDefaultColor();
    }

    private BlockInstrument blockInstrument() {
        return (BlockInstrument)getBlockState().getBlock();
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);

        if(this.color != null) {
            compound.putInt(COLOR_TAG, color);
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        
        // Fallback for missing stack data
        if(this.items.get(0).isEmpty()) {
            MIMIMod.LOGGER.warn("TileInstrument had no saved instrument stack! Re-initializing.");
            this.setInstrumentStack(this.initializeInstrumentStack());
        }

        // START TEMPORARY LEGACY COMPATIBILITY CODE
        // Fallback for stack missing color
        if(compound.contains(COLOR_TAG) && !this.hasColor()) {
            this.color = compound.getInt(COLOR_TAG);
            ItemStack stack = this.getInstrumentStack();
            ((IColorableItem)stack.getItem()).setColor(stack, this.color);
            this.setInstrumentStack(stack);
        }
        // END TEMPORARY LEGACY COMPATIBILITY CODE
    }

    @Override
    public void loadItems(CompoundTag compound) {
        // START TEMPORARY LEGACY COMPATIBILITY CODE
        // Filter out switchboard items so that we can convert them
        ListTag listtag = compound.getList("Items", 10);
        ItemStack convertStack = null;

        if(listtag.size() > 0) {
            CompoundTag stackTag = listtag.getCompound(0);
            String itemId = stackTag.getString("id");

            if(itemId.equalsIgnoreCase("mimi:switchboard")) {
                convertStack = initializeInstrumentStack(InstrumentDataUtils.convertSwitchboardToDataTag(stackTag.getCompound("tag")));

                if(compound.contains(COLOR_TAG)) {
                    ((IColorableItem)convertStack.getItem()).setColor(convertStack, compound.getInt(COLOR_TAG));
                }
            }
        }

        ContainerHelper.loadAllItems(compound, items);

        if(convertStack != null) {  
            this.setInstrumentStack(convertStack);
        }
        // END TEMPORARY LEGACY COMPATIBILITY CODE
    }

    private ItemStack initializeInstrumentStack() {
        return this.initializeInstrumentStack(null);
    }

    private ItemStack initializeInstrumentStack(CompoundTag stackTag) {
        ItemStack instrumentStack = new ItemStack(this.blockInstrument().asItem(), 1);

        if(stackTag != null) {
            instrumentStack.setTag(stackTag);
        } else {
            instrumentStack.setTag(new CompoundTag());
        }

        return instrumentStack;
    }
}