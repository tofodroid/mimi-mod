package io.github.tofodroid.mods.mimi.common.container;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraftforge.common.extensions.IForgeMenuType;
import net.minecraftforge.network.IContainerFactory;
import net.minecraftforge.registries.RegisterEvent;

public class ModContainers {
    public static MenuType<ContainerListener> LISTENER = null;
    public static MenuType<ContainerReceiver> RECEIVER = null;
    public static MenuType<ContainerInstrument> INSTRUMENT = null;
    public static MenuType<ContainerMechanicalMaestro> MECHANICALMAESTRO = null;
    public static MenuType<ContainerConductor> CONDUCTOR = null;
    public static MenuType<ContainerTuningTable> TUNINGTABLE = null;
    public static MenuType<ContainerDiskWriter> DISKWRITER = null;
    public static MenuType<ContainerBroadcaster> BROADCASTER = null;
    public static MenuType<ContainerTransmitter> TRANSMITTER = null;
    
    private static <T extends AbstractContainerMenu> MenuType<T> registerType(String id, IContainerFactory<T> factory, final RegisterEvent.RegisterHelper<MenuType<?>> event) {
        MenuType<T> type = IForgeMenuType.create(factory);
        event.register(id, type);
        return type;
    }

    public static void submitRegistrations(final RegisterEvent.RegisterHelper<MenuType<?>> event) {
        LISTENER = registerType("listener", ContainerListener::new, event);
        RECEIVER = registerType("receiver", ContainerReceiver::new, event);
        INSTRUMENT = registerType("instrument", ContainerInstrument::new, event);
        MECHANICALMAESTRO = registerType("mechanicalmaestro", ContainerMechanicalMaestro::new, event);
        CONDUCTOR = registerType("conductor", ContainerConductor::new, event);
        TUNINGTABLE = registerType("tuningtable", ContainerTuningTable::new, event);
        DISKWRITER = registerType("diskwriter", ContainerDiskWriter::new, event);
        BROADCASTER = registerType("broadcaster", ContainerBroadcaster::new, event);
        TRANSMITTER = registerType("transmitter", ContainerTransmitter::new, event);
    }
}
