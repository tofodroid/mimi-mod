package io.github.tofodroid.mods.mimi.forge;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.Proxy;
import io.github.tofodroid.mods.mimi.server.ServerProxy;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;

@Mod(MIMIMod.MODID)
public class MIMIModForge {
    static {
        MIMIMod.setProxy((Proxy)DistExecutor.safeRunForDist(() -> ClientProxy::new, () -> ServerProxy::new));
    }

    public MIMIModForge() {
        MIMIMod.preRegister();
    }
}
