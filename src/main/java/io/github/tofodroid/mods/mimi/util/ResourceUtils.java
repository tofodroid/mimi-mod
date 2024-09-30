package io.github.tofodroid.mods.mimi.util;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.resources.ResourceLocation;

public class ResourceUtils {
    public static ResourceLocation newModLocation(String path) {
        return ResourceUtils.newLocation(MIMIMod.MODID, path);
    }

    public static ResourceLocation newRootLocation(String path) {
        return ResourceLocation.withDefaultNamespace(path);
    }

    public static ResourceLocation parseLocation(String location) {
        return ResourceLocation.parse(location);
    }
    
    public static ResourceLocation newLocation(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}
