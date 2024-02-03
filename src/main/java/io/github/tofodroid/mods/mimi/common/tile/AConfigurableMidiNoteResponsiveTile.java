package io.github.tofodroid.mods.mimi.common.tile;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AConfigurableMidiNoteResponsiveTile extends AConfigurableMidiTile {
    protected Integer enabledChannels;
    protected List<Byte> filteredNotes;
    protected Byte filterOct;
    protected Byte filterNote;
    protected Byte filteredInstrument;
    protected Boolean invertFilterNoteOct;
    protected Boolean invertFilterInstrument;

    public AConfigurableMidiNoteResponsiveTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, 1);
    }

    protected AConfigurableMidiNoteResponsiveTile(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state, inventorySize);
    }

    protected void tick(Level world, BlockPos pos, BlockState state) {
        if(world instanceof ServerLevel) {
            this.execServerTick((ServerLevel)world, pos, state);
        }
    }
    
	@Override
	public void load(CompoundTag nbt) {
        super.load(nbt);
        this.cacheMidiSettings();
    }

    @Override
    public void setSourceStack(ItemStack stack) {
        super.setSourceStack(stack);
        this.cacheMidiSettings();
    }

    protected void cacheMidiSettings() {
        if(this.getSourceStack() == null) return;

        this.enabledChannels = MidiNbtDataUtils.getEnabledChannelsInt(this.getSourceStack());
        this.filterOct = MidiNbtDataUtils.getFilterOct(this.getSourceStack());
        this.filterNote = MidiNbtDataUtils.getFilterNote(this.getSourceStack());
        this.filteredNotes = MidiNbtDataUtils.getFilterNotes(this.filterNote, this.filterOct);
        this.filteredInstrument = MidiNbtDataUtils.getFilterInstrument(this.getSourceStack());
        this.invertFilterInstrument = MidiNbtDataUtils.getInvertInstrument(this.getSourceStack());
        this.invertFilterNoteOct = MidiNbtDataUtils.getInvertNoteOct(this.getSourceStack());
    }

    protected void execServerTick(ServerLevel world, BlockPos pos, BlockState state) {/* Default: No-op */};
    
    public abstract void onNoteOn(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId);
    public abstract void onNoteOff(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId);
    public abstract void onAllNotesOff(@Nullable Byte channel, @Nullable Byte instrumentId);
    public abstract Boolean shouldTriggerFromNoteOn(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId);
    public abstract Boolean shouldTriggerFromNoteOff(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId);
    public abstract Boolean shouldTriggerFromAllNotesOff(@Nullable Byte channel, @Nullable Byte instrumentId);
}
