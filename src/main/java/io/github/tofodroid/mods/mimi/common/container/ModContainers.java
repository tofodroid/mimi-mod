package io.github.tofodroid.mods.mimi.common.container;

import java.util.HashMap;
import java.util.Map;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;

public class ModContainers {
    public static final Map<ResourceLocation, MenuType<?>> MENU_TYPES = new HashMap<>();

    public static final MenuType<ContainerTuningTable> TUNINGTABLE = create("tuningtable", ContainerTuningTable::new);
    public static final MenuType<ContainerMechanicalMaestro> MECHANICALMAESTRO = create("mechanicalmaestro", ContainerMechanicalMaestro::new);

    private static <T extends AbstractContainerMenu> MenuType<T> create(String id, MenuType.MenuSupplier<T> factory) {
        MenuType<T> type = new MenuType<>(factory, FeatureFlags.DEFAULT_FLAGS);
        MENU_TYPES.put(new ResourceLocation(MIMIMod.MODID, id), type);
        return type;
    }
}
