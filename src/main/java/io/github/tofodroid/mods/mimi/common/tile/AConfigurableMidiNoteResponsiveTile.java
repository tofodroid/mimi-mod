package io.github.tofodroid.mods.mimi.common.tile;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
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

        this.enabledChannels = InstrumentDataUtils.getEnabledChannelsInt(this.getSourceStack());
        this.filteredNotes = InstrumentDataUtils.getFilterNotes(InstrumentDataUtils.getFilterNote(this.getSourceStack()), InstrumentDataUtils.getFilterOct(this.getSourceStack()));
        this.filteredInstrument = InstrumentDataUtils.getFilterInstrument(this.getSourceStack());
        this.invertFilterInstrument = InstrumentDataUtils.getInvertInstrument(this.getSourceStack());
        this.invertFilterNoteOct = InstrumentDataUtils.getInvertNoteOct(this.getSourceStack());
    }

    protected void execServerTick(ServerLevel world, BlockPos pos, BlockState state) {/* Default: No-op */};
    public abstract Boolean onTrigger(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId);
    public abstract Boolean shouldTriggerFromMidiEvent(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId);
}
