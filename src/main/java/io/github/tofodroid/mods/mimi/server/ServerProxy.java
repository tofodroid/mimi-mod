package io.github.tofodroid.mods.mimi.server;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.Proxy;
import net.minecraft.Util;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public class ServerProxy implements Proxy {
    private final Long serverStartEpoch = Util.getEpochMillis();

    @Override
    public void init(FMLCommonSetupEvent event) { }

    @Override
    public Boolean isClient() {
        return false;
    }

    @Override
        public Long getServerStartEpoch() {
        MIMIMod.LOGGER.info("TIME SYNC SERVER:\n\tServer Start Millis: " + this.serverStartEpoch);
        return serverStartEpoch;
    }
}
