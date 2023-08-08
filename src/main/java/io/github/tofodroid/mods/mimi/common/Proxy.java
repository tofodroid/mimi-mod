package io.github.tofodroid.mods.mimi.common;

import net.minecraft.Util;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public interface Proxy {
    public void init(final FMLCommonSetupEvent event);

    public Boolean isClient();

    public Long getServerStartEpoch();

    default public Long getBaselineBufferMs() {
        return 100l;
    }

    default public Long getCurrentServerMillis() {
        return Util.getEpochMillis() - getServerStartEpoch();
    }
}
