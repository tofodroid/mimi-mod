package io.github.tofodroid.mods.mimi.common.container;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.network.IContainerFactory;

@Mod.EventBusSubscriber(modid=MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModContainers {
    public static MenuType<ContainerListener> LISTENER = null;
    public static MenuType<ContainerReceiver> RECEIVER = null;
    public static MenuType<ContainerInstrument> INSTRUMENT = null;
    public static MenuType<ContainerMechanicalMaestro> MECHANICALMAESTRO = null;
    public static MenuType<ContainerConductor> CONDUCTOR = null;
    public static MenuType<ContainerTuningTable> TUNINGTABLE = null;

    private static final List<MenuType<?>> buildTileTypes() {
        List<MenuType<?>> types = new ArrayList<>();
        LISTENER = buildType("listener", ContainerListener::new);
        types.add(LISTENER);
        RECEIVER = buildType("receiver", ContainerReceiver::new);
        types.add(RECEIVER);
        INSTRUMENT = buildType("instrument", ContainerInstrument::new);
        types.add(INSTRUMENT);
        MECHANICALMAESTRO = buildType("mechanicalmaestro", ContainerMechanicalMaestro::new);
        types.add(MECHANICALMAESTRO);
        CONDUCTOR = buildType("conductor", ContainerConductor::new);
        types.add(CONDUCTOR);
        TUNINGTABLE = buildType("tuningtable", ContainerTuningTable::new);
        types.add(TUNINGTABLE);
        return types;
    }
    
    private static <T extends AbstractContainerMenu> MenuType<T> buildType(String id, IContainerFactory<T> factory) {
        MenuType<T> type = IForgeMenuType.create(factory);
        type.setRegistryName(id);
        return type;
    }

    @SubscribeEvent
    public static void registerTypes(final RegistryEvent.Register<MenuType<?>> event) {
        List<MenuType<?>> types = buildTileTypes();
        types.forEach(type -> event.getRegistry().register(type));
    }
}
