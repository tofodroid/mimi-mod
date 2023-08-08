package io.github.tofodroid.mods.mimi.common.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileReceiver extends AConfigurableMidiTile {
    public TileReceiver(BlockPos pos, BlockState state) {
        super(ModTiles.RECEIVER, pos, state);
    }
}

