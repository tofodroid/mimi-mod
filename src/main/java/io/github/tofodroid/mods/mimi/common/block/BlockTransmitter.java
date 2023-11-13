package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockTransmitter extends AContainerBlock<TileTransmitter> {
    public static final String REGISTRY_NAME = "transmitterblock";

    public BlockTransmitter() {
        super(Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
    }

    @Override
    public BlockEntityType<TileTransmitter> getTileType() {
        return ModTiles.TRANSMITTER;
    }    
}
