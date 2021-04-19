package io.github.tofodroid.mods.mimi.common.container;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.inventory.container.ContainerType;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModContainers {
    public static ContainerType<ContainerDiskRecorder> CONTAINER_TYPE_CONTAINER_DISK_RECORDER;
    
    @SubscribeEvent
    public static void registerContainers(final RegistryEvent.Register<ContainerType<?>> event) {
        CONTAINER_TYPE_CONTAINER_DISK_RECORDER = IForgeContainerType.create(ContainerDiskRecorder::createContainerClientSide);
        CONTAINER_TYPE_CONTAINER_DISK_RECORDER.setRegistryName("diskrecorder");
        event.getRegistry().register(CONTAINER_TYPE_CONTAINER_DISK_RECORDER);
    }
}
