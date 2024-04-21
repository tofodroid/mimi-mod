package io.github.tofodroid.mods.mimi.neoforge.common.tile;

import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class NeoForgeTileMechanicalMaestro extends TileMechanicalMaestro {
    // LazyOptional<? extends IItemHandler> handlers[] = SidedInvWrapper.create(this, Direction.UP);

    public NeoForgeTileMechanicalMaestro(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    // @Override
    // public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
    //     if (!this.remove && facing != null && capability == Capabilities.ItemHandler.BLOCK) {
    //         if (facing == Direction.UP)
    //             return handlers[0].cast();
    //     }
    //     return super.getCapability(capability, facing);
    // }

    // @Override
    // public void invalidateCaps() {
    //     super.invalidateCaps();
    //     for (LazyOptional<? extends IItemHandler> handler : handlers) handler.invalidate();
    // }
}
