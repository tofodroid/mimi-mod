package io.github.tofodroid.mods.mimi.neoforge;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.server.ServerProxy;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(MIMIMod.MODID)
public class MIMIModNeoForge {
    static {
        if(FMLEnvironment.dist == Dist.CLIENT) {
            MIMIMod.setProxy(new ClientProxy());
        } else {
            MIMIMod.setProxy(new ServerProxy());
        }
    }

    public MIMIModNeoForge() {
        MIMIMod.preRegister();
    }
}
