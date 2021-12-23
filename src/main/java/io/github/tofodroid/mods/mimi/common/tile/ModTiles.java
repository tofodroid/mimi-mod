package io.github.tofodroid.mods.mimi.common.tile;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModTiles {
    public static BlockEntityType<TileInstrument> INSTRUMENT = null;
    public static BlockEntityType<TileReceiver> RECEIVER = null;
    public static BlockEntityType<TileListener> LISTENER = null;
    public static BlockEntityType<TileMechanicalMaestro> MECHANICALMAESTRO = null;
    public static BlockEntityType<TileConductor> CONDUCTOR = null;

    private static final List<BlockEntityType<?>> buildTileTypes() {
        List<BlockEntityType<?>> types = new ArrayList<>();
        INSTRUMENT = buildType(MIMIMod.MODID + ":instrument", BlockEntityType.Builder.of(TileInstrument::new, ModBlocks.INSTRUMENTS.toArray(new BlockInstrument[ModBlocks.INSTRUMENTS.size()])));
        types.add(INSTRUMENT);
        RECEIVER = buildType(MIMIMod.MODID + ":receiver", BlockEntityType.Builder.of(TileReceiver::new, ModBlocks.RECEIVER));
        types.add(RECEIVER);
        LISTENER = buildType(MIMIMod.MODID + ":listener", BlockEntityType.Builder.of(TileListener::new, ModBlocks.LISTENER));
        types.add(LISTENER);
        MECHANICALMAESTRO = buildType(MIMIMod.MODID + ":mechanicalmaestro", BlockEntityType.Builder.of(TileMechanicalMaestro::new, ModBlocks.MECHANICALMAESTRO));
        types.add(MECHANICALMAESTRO);
        CONDUCTOR = buildType(MIMIMod.MODID + ":conductor", BlockEntityType.Builder.of(TileConductor::new, ModBlocks.CONDUCTOR));
        types.add(CONDUCTOR);
        return types;
    }
    
    private static <T extends BlockEntity> BlockEntityType<T> buildType(String id, BlockEntityType.Builder<T> builder) {
        BlockEntityType<T> type = builder.build(null);
        type.setRegistryName(id);
        return type;
    }

    @SubscribeEvent
    public static void registerTypes(final RegistryEvent.Register<BlockEntityType<?>> event) {
        List<BlockEntityType<?>> types = buildTileTypes();
        types.forEach(type -> event.getRegistry().register(type));
    }
}
