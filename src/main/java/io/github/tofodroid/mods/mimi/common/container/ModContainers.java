package io.github.tofodroid.mods.mimi.common.container;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.inventory.container.Container;
import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.network.IContainerFactory;

@Mod.EventBusSubscriber(modid=MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModContainers {
    public static ContainerType<ContainerListener> LISTENER = null;
    public static ContainerType<ContainerReceiver> RECEIVER = null;
    public static ContainerType<ContainerInstrument> INSTRUMENT = null;
    public static ContainerType<ContainerMechanicalMaestro> MECHANICALMAESTRO = null;

    private static final List<ContainerType<?>> buildTileTypes() {
        List<ContainerType<?>> types = new ArrayList<>();
        LISTENER = buildType("listener", ContainerListener::new);
        types.add(LISTENER);
        RECEIVER = buildType("receiver", ContainerReceiver::new);
        types.add(RECEIVER);
        INSTRUMENT = buildType("instrument", ContainerInstrument::new);
        types.add(INSTRUMENT);
        MECHANICALMAESTRO = buildType("mechanicalmaestro", ContainerMechanicalMaestro::new);
        types.add(MECHANICALMAESTRO);
        return types;
    }
    
    private static <T extends Container> ContainerType<T> buildType(String id, IContainerFactory<T> factory) {
        ContainerType<T> type = IForgeContainerType.create(factory);
        type.setRegistryName(id);
        return type;
    }

    @SubscribeEvent
    public static void registerTypes(final RegistryEvent.Register<ContainerType<?>> event) {
        List<ContainerType<?>> types = buildTileTypes();
        types.forEach(type -> event.getRegistry().register(type));
    }
}
