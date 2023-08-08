package io.github.tofodroid.mods.mimi.common.tile;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

public class TileListener extends AConfigurableMidiTile {
    public TileListener(BlockPos pos, BlockState state) {
        super(ModTiles.LISTENER, pos, state);
    }
}
