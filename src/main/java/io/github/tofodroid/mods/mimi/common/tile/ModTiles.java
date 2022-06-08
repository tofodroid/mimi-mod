package io.github.tofodroid.mods.mimi.common.tile;

import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.registries.RegisterEvent;

public class ModTiles {
    public static BlockEntityType<TileInstrument> INSTRUMENT = null;
    public static BlockEntityType<TileReceiver> RECEIVER = null;
    public static BlockEntityType<TileListener> LISTENER = null;
    public static BlockEntityType<TileMechanicalMaestro> MECHANICALMAESTRO = null;
    public static BlockEntityType<TileConductor> CONDUCTOR = null;
    
    private static <T extends BlockEntity> BlockEntityType<T> registerType(String id, BlockEntityType.Builder<T> builder, RegisterEvent.RegisterHelper<BlockEntityType<?>> event) {
        BlockEntityType<T> type = builder.build(null);
        event.register(id, type);
        return type;
    }

    public static void submitRegistrations(final RegisterEvent.RegisterHelper<BlockEntityType<?>> event) {
        INSTRUMENT = registerType("instrument", BlockEntityType.Builder.of(TileInstrument::new, ModBlocks.INSTRUMENTS.toArray(new BlockInstrument[ModBlocks.INSTRUMENTS.size()])), event);
        RECEIVER = registerType("receiver", BlockEntityType.Builder.of(TileReceiver::new, ModBlocks.RECEIVER), event);
        LISTENER = registerType("listener", BlockEntityType.Builder.of(TileListener::new, ModBlocks.LISTENER), event);
        MECHANICALMAESTRO = registerType("mechanicalmaestro", BlockEntityType.Builder.of(TileMechanicalMaestro::new, ModBlocks.MECHANICALMAESTRO), event);
        CONDUCTOR = registerType("conductor", BlockEntityType.Builder.of(TileConductor::new, ModBlocks.CONDUCTOR), event);
    }
}
