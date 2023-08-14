package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.state.BlockState;

public abstract interface INoteResponsiveTile<T extends BlockEntity> extends BlockEntityTicker<T> {
    public static final Integer UPDATE_EVERY_TICKS = 8;

    default void tick(Level world, BlockPos pos, BlockState state, T self) {
        if(this.getTickCount() >= UPDATE_EVERY_TICKS) {
            this.setTickCount(0);
            if(!world.isClientSide && !self.isRemoved()) {
                if(this.shouldHaveEntity()) {
                    EntityNoteResponsiveTile.create(world, pos);
                } else {
                    EntityNoteResponsiveTile.remove(world, pos);
                }
            }
        } else {
            this.setTickCount(this.getTickCount()+1);
        }

        if(world instanceof ServerLevel) {
            this.execServerTick((ServerLevel)world, pos, state, self);
        }
    }

    default void execServerTick(ServerLevel world, BlockPos pos, BlockState state, T self) { /* Default no-op */ };

    default Boolean onMidiEvent(@Nullable UUID sender, @Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        if(this.shouldTriggerFromMidiEvent(sender, channel, note, velocity, instrumentId)) {
            return this.onTrigger(sender, channel, note, velocity, instrumentId);
        }
        return false;
    }

    public Boolean shouldHaveEntity();
    public Boolean onTrigger(@Nullable UUID sender, @Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId);
    public Boolean shouldTriggerFromMidiEvent(@Nullable UUID sender, @Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId);
    public Integer getTickCount();
    public void setTickCount(Integer count);
}
