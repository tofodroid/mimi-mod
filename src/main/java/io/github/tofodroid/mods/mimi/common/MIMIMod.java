package io.github.tofodroid.mods.mimi.common;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import io.github.tofodroid.mods.mimi.common.config.ConfigProxy;


public class MIMIMod {
    public static final String MODID = "mimi";
    public static final Logger LOGGER = LogManager.getLogger();
    protected static Proxy proxy;

    public static Proxy getProxy() {
        return MIMIMod.proxy;
    }
    
    public static void setProxy(Proxy inProxy) {
        MIMIMod.proxy = inProxy;
    }

    public static void preRegister() {
        ConfigProxy.registerConfigs();
    }

    public static void postRegister() {
        MIMIMod.proxy.init();
    }
}
