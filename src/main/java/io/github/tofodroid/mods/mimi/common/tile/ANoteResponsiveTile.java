package io.github.tofodroid.mods.mimi.common.tile;

import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class ANoteResponsiveTile extends ASwitchboardContainerEntity implements BlockEntityTicker<ANoteResponsiveTile> {
    public static final Integer UPDATE_EVERY_TICKS = 8;

    protected Integer tickCount = 0;

    public ANoteResponsiveTile(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public static void doTick(Level world, BlockPos pos, BlockState state, ANoteResponsiveTile self) {
        self.tick(world, pos, state, self);
    }

    @Override
    public void tick(Level world, BlockPos pos, BlockState state, ANoteResponsiveTile self) {
        if(tickCount >= UPDATE_EVERY_TICKS) {
            tickCount = 0;
            if(!world.isClientSide && !this.isRemoved()) {
                if(this.shouldHaveEntity()) {
                    EntityNoteResponsiveTile.create(world, pos);
                } else {
                    EntityNoteResponsiveTile.remove(world, pos);
                }
            }
        } else {
            tickCount ++;
        }        
    }

    protected abstract Boolean shouldHaveEntity();
}
