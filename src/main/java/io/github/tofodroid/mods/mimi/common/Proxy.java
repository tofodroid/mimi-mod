package io.github.tofodroid.mods.mimi.common;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

public interface Proxy {    
    public void init(final FMLCommonSetupEvent event);

    public Boolean isClient();
}
