package io.github.tofodroid.mods.mimi.common.tile;

import java.util.HashMap;
import java.util.Map;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.registry.RegistryOverrideProxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class ModTiles {
    public static final Map<ResourceLocation, BlockEntityType<?>> BLOCK_ENTITIES = new HashMap<>();

    public static BlockEntityType<TileInstrument> INSTRUMENT = create(TileInstrument.REGISTRY_NAME, TileInstrument::new, ModBlocks.INSTRUMENTS.toArray(new BlockInstrument[ModBlocks.INSTRUMENTS.size()]));
    public static BlockEntityType<TileTransmitter> TRANSMITTER = create(TileTransmitter.REGISTRY_NAME, TileTransmitter::new, ModBlocks.TRANSMITTER);
    public static BlockEntityType<TileListener> LISTENER = create(TileListener.REGISTRY_NAME, TileListener::new, ModBlocks.LISTENER);
    public static BlockEntityType<TileReceiver> RECEIVER = create(TileReceiver.REGISTRY_NAME, TileReceiver::new, ModBlocks.RECEIVER);
    public static BlockEntityType<TileConductor> CONDUCTOR = create(TileConductor.REGISTRY_NAME, TileConductor::new, ModBlocks.CONDUCTOR);
    public static BlockEntityType<TileMechanicalMaestro> MECHANICALMAESTRO = create(TileMechanicalMaestro.REGISTRY_NAME, TileMechanicalMaestro::new, ModBlocks.MECHANICALMAESTRO);
    public static BlockEntityType<TileEffectEmitter> EFFECTEMITTER = create(TileEffectEmitter.REGISTRY_NAME, TileEffectEmitter::new, ModBlocks.EFFECTEMITTER);

    @SuppressWarnings("unchecked")
    private static <T extends BlockEntity> BlockEntityType<T> create(String id, BlockEntityType.BlockEntitySupplier<T> blockEntity, Block... blocks) {
        blockEntity = RegistryOverrideProxy.getOrOverride(BlockEntityType.BlockEntitySupplier.class, id, blockEntity);
        BlockEntityType<T> type = BlockEntityType.Builder.of(blockEntity, blocks).build(null);
        BLOCK_ENTITIES.put(new ResourceLocation(MIMIMod.MODID, id), type);
        return type;
    }
}
