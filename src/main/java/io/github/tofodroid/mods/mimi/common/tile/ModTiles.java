package io.github.tofodroid.mods.mimi.common.tile;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegisterEvent;

public class ModTiles {
    public static BlockEntityType<TileBroadcaster> BROADCASTER = null;
    public static BlockEntityType<TileInstrument> INSTRUMENT = null;
    
    private static <T extends BlockEntity> BlockEntityType<T> registerType(String id, BlockEntityType.Builder<T> builder, RegisterEvent.RegisterHelper<BlockEntityType<?>> event) {
        BlockEntityType<T> type = builder.build(null);
        event.register(id, type);
        return type;
    }

    public static void submitRegistrations(final RegisterEvent.RegisterHelper<BlockEntityType<?>> event) {
        BROADCASTER = registerType("broadcaster", BlockEntityType.Builder.of(TileBroadcaster::new, ModBlocks.BROADCASTER.get()), event);
        INSTRUMENT = registerType("blockinstrument", BlockEntityType.Builder.of(TileInstrument::new, ModBlocks.getBlockInstruments().toArray(new BlockInstrument[ModBlocks.getBlockInstruments().size()])), event);
    }
}
