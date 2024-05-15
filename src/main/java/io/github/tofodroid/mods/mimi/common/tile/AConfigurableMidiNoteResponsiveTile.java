package io.github.tofodroid.mods.mimi.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AConfigurableMidiNoteResponsiveTile extends AConfigurableMidiTile {
    protected Boolean firstTick = true;
    protected Integer enabledChannels;
    protected Integer filterOctMin;
    protected Integer filterOctMax;
    protected Byte filterNote;
    protected Byte filteredInstrument;
    protected Boolean invertFilterNoteOct;

    public static void doTick(Level world, BlockPos pos, BlockState state, AConfigurableMidiNoteResponsiveTile self) {
        self.tick(world, pos, state);
    }
    
    public AConfigurableMidiNoteResponsiveTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, 1);
    }

    protected AConfigurableMidiNoteResponsiveTile(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state, inventorySize);
    }

    protected void tick(Level world, BlockPos pos, BlockState state) {
        if(world instanceof ServerLevel) {
            this.execServerTick((ServerLevel)world, pos, state);

            if(firstTick) {
                this.cacheMidiSettings();
                this.firstTick = false;
                this.onFirstTick((ServerLevel)world, pos, state);
            }
        }
    }
    
	@Override
	public void loadAdditional(CompoundTag nbt, HolderLookup.Provider pRegistries) {
        super.loadAdditional(nbt, pRegistries);
        this.cacheMidiSettings();
    }

    @Override
    public void setSourceStack(ItemStack stack) {
        super.setSourceStack(stack);
        this.cacheMidiSettings();
    }
    
    @Override
    public void setRemoved() {
        super.setRemoved();

        if(!this.getLevel().isClientSide()) {
            this.firstTick = true;
        }
    }
 
    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
    
        if(!this.getLevel().isClientSide()) {
            this.firstTick = true;
        }
    }

    public Integer getEnabledChannelsInt() {
        return this.enabledChannels;
    }

    public Byte getFilteredInstrument() {
        return this.filteredInstrument;
    }

    protected void cacheMidiSettings() {
        if(this.getSourceStack() == null) return;

        this.enabledChannels = MidiNbtDataUtils.getEnabledChannelsInt(this.getSourceStack());
        Byte filterOct = MidiNbtDataUtils.getFilterOct(this.getSourceStack());
        this.filterOctMin = filterOct * 12;
        this.filterOctMax = (filterOct+1) * 12;
        this.filterNote = MidiNbtDataUtils.getFilterNote(this.getSourceStack());
        this.filteredInstrument = MidiNbtDataUtils.getFilterInstrument(this.getSourceStack());
        this.invertFilterNoteOct = MidiNbtDataUtils.getInvertNoteOct(this.getSourceStack());
    }

    protected void execServerTick(ServerLevel world, BlockPos pos, BlockState state) {/* Default: No-op */};
    protected void onFirstTick(ServerLevel world, BlockPos pos, BlockState state) {/* Default: No-op */};
    
    public abstract void onNoteOn(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId, Long noteTime);
    public abstract void onNoteOff(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId, Long noteTime);
    public abstract void onAllNotesOff(@Nullable Byte channel, @Nullable Byte instrumentId, Long noteTime);
    public abstract Boolean shouldTriggerFromNoteOn(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId);
    public abstract Boolean shouldTriggerFromNoteOff(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId);
    public abstract Boolean shouldTriggerFromAllNotesOff(@Nullable Byte channel, @Nullable Byte instrumentId);
}
