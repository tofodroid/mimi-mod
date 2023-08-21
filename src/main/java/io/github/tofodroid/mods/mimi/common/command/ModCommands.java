package io.github.tofodroid.mods.mimi.common.command;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID)
public class ModCommands {

    @SubscribeEvent
    public static void register(RegisterCommandsEvent event) {
        event.getDispatcher().register(
            Commands.literal("mimi")
                .then(Commands.literal("server")
                    .then(Commands.literal("reload")
                        .requires(cs -> cs.getServer().isSingleplayer() || cs.hasPermission(3))
                        .executes(ctx -> reloadServerMusicList(ctx.getSource()))
                    )
                )
        );
	}

    private static int reloadServerMusicList(CommandSourceStack source) {
        MIMIMod.proxy.defaultMidiFiles().refresh();
        source.sendSuccess(() -> Component.literal("Server saved music reloaded. Found " + MIMIMod.proxy.defaultMidiFiles().getFileInfoPages(1) + " files"), true);
        return 0;
    }
}
