package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

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

    default Boolean shouldHaveEntity() {
        return true;
    }

    public Integer getTickCount();
    public void setTickCount(Integer count);
    public Boolean shouldRespondToNote(Byte note, Byte instrumentId);
    public Boolean shouldRespondToMessage(UUID sender, Byte channel, Byte note);
    public void execServerTick(ServerLevel world, BlockPos pos, BlockState state, T self);
}
