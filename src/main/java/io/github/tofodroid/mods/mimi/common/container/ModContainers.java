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
    public static ContainerType<ContainerReceiver> RECEIVER = null;
    public static ContainerType<ContainerInstrument> INSTRUMENT = null;

    private static final List<ContainerType<?>> buildTileTypes() {
        List<ContainerType<?>> types = new ArrayList<>();
        RECEIVER = buildType("receiver", ContainerReceiver::new);
        INSTRUMENT = buildType("instrument", ContainerInstrument::new);
        types.add(RECEIVER);
        types.add(INSTRUMENT);
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
