package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockListener;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AConfigurableMidiTile extends AStaticInventoryTile implements INoteResponsiveTile<AConfigurableMidiTile> {
    public static final Integer SOURCE_STACK_SLOT = 0;
    protected Integer tickCount = 0;
    
    public static void doTick(Level world, BlockPos pos, BlockState state, AConfigurableMidiTile self) {
        self.tick(world, pos, state, self);
    }    

    public AConfigurableMidiTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, 1);
    }

    protected AConfigurableMidiTile(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state, inventorySize);
    }

    @Override
    public void setTickCount(Integer count) {
        this.tickCount = count;
    }

    @Override
    public Integer getTickCount() {
        return this.tickCount;
    }

    @Override
    public void execServerTick(ServerLevel world, BlockPos pos, BlockState state, AConfigurableMidiTile self) {
        if (state.getValue(BlockListener.POWER) != 0) {
            world.setBlock(pos, state.setValue(BlockListener.POWER, Integer.valueOf(0)), 3);
 
            for(Direction direction : Direction.values()) {
                world.updateNeighborsAt(pos.relative(direction), ModBlocks.LISTENER.get());
            }
        }
    }

    public void setSourceStack(ItemStack stack) {
        if(stack.getItem().getClass().equals(this.getBlockState().getBlock().asItem().getClass())) {
            this.setItem(SOURCE_STACK_SLOT, stack);
        }
    }

    public ItemStack getSourceStack() {
        if(items.isEmpty() || items.get(0) == null) {
            return ItemStack.EMPTY;
        } else {
            return items.get(SOURCE_STACK_SLOT);
        }
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        
        // Fallback for missing stack data
        if(this.items.get(0).isEmpty()) {
            MIMIMod.LOGGER.warn("ConfigurableMidiTile had no saved source stack! Re-initializing.");
            this.setSourceStack(this.initializeSourceStack(null));
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public boolean canTakeItemThroughFace(int slot, ItemStack stack, @Nullable Direction side) {
        return false;
    }

    @Override
    public ItemStack removeItem(int i, int count) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack removeItemNoUpdate(int i) {
        return ItemStack.EMPTY;
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    }
    
    @Override
    public Boolean shouldRespondToNote(Byte note, Byte instrumentId) {
        ItemStack sourceStack = getSourceStack();

        if(!sourceStack.isEmpty()) {
            return 
                (note == null || InstrumentDataUtils.isNoteFiltered(sourceStack, note)) && 
                (instrumentId == null || InstrumentDataUtils.isInstrumentFiltered(sourceStack, instrumentId))
            ;
        }

        return false;
    }

    @Override
    public Boolean shouldRespondToMessage(UUID sender, Byte channel, Byte note) {
        ItemStack sourceStack = getSourceStack();
        if(!sourceStack.isEmpty()) {
            return 
                InstrumentDataUtils.isChannelEnabled(sourceStack, channel) && shouldRespondToNote(note, null) 
                && (sender != null && sender.equals(InstrumentDataUtils.getMidiSource(sourceStack)));
        }
        return false;
    }

    protected ItemStack initializeSourceStack(CompoundTag stackTag) {
        ItemStack sourceStack = new ItemStack(this.getBlockState().getBlock().asItem(), 1);

        if(stackTag != null) {
            sourceStack.setTag(stackTag);
        } else {
            sourceStack.setTag(new CompoundTag());
        }

        return sourceStack;
    }
}
