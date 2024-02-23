package io.github.tofodroid.mods.mimi.common.registry;

import io.github.tofodroid.mods.mimi.forge.common.registry.RegistryOverrides;

public abstract class RegistryOverrideProxy {
    public static <T> T getOrOverride(Class<T> clazz, String id, T dfault) {
        return RegistryOverrides.getOrOverride(clazz, id, dfault);
    }
}
