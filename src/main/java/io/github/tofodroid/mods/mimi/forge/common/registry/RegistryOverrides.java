package io.github.tofodroid.mods.mimi.forge.common.registry;

import java.util.Map;

import io.github.tofodroid.mods.mimi.forge.common.tile.ForgeTileMechanicalMaestro;
import net.minecraft.world.level.block.entity.BlockEntityType;

public abstract class RegistryOverrides {
    public static final Map<String, BlockEntityType.BlockEntitySupplier<?>> BLOCK_ENTITES = Map.ofEntries(
        Map.entry("mechanicalmaestro", ForgeTileMechanicalMaestro::new)
    );

    public static final Map<Class<?>, Map<String, ?>> OVERRIDES = Map.of(
        BlockEntityType.BlockEntitySupplier.class, BLOCK_ENTITES
    );

    @SuppressWarnings("unchecked")
    public static <T> T getOrOverride(Class<T> clazz, String id, T dfault) {
        Map<String, T> map = (Map<String, T>)OVERRIDES.get(clazz);
        return map != null ? map.getOrDefault(id, dfault) : dfault;
    }
}
