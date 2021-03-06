package io.github.tofodroid.mods.mimi.common.tile;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;

import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid=MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModTiles {
    public static TileEntityType<TileInstrument> INSTRUMENT = null;
    public static TileEntityType<TileReceiver> RECEIVER = null;
    public static TileEntityType<TileListener> LISTENER = null;
    public static TileEntityType<TileMechanicalMaestro> MECHANICALMAESTRO = null;

    private static final List<TileEntityType<?>> buildTileTypes() {
        List<TileEntityType<?>> types = new ArrayList<>();
        INSTRUMENT = buildType(MIMIMod.MODID + ":instrument", TileEntityType.Builder.create(TileInstrument::new, ModBlocks.INSTRUMENTS.toArray(new BlockInstrument[ModBlocks.INSTRUMENTS.size()])));
        types.add(INSTRUMENT);
        RECEIVER = buildType(MIMIMod.MODID + ":receiver", TileEntityType.Builder.create(TileReceiver::new, ModBlocks.RECEIVER));
        types.add(RECEIVER);
        LISTENER = buildType(MIMIMod.MODID + ":listener", TileEntityType.Builder.create(TileListener::new, ModBlocks.LISTENER));
        types.add(LISTENER);
        MECHANICALMAESTRO = buildType(MIMIMod.MODID + ":mechanicalmaestro", TileEntityType.Builder.create(TileMechanicalMaestro::new, ModBlocks.MECHANICALMAESTRO));
        types.add(MECHANICALMAESTRO);
        return types;
    }
    
    private static <T extends TileEntity> TileEntityType<T> buildType(String id, TileEntityType.Builder<T> builder) {
        TileEntityType<T> type = builder.build(null);
        type.setRegistryName(id);
        return type;
    }

    @SubscribeEvent
    public static void registerTypes(final RegistryEvent.Register<TileEntityType<?>> event) {
        List<TileEntityType<?>> types = buildTileTypes();
        types.forEach(type -> event.getRegistry().register(type));
    }
}
