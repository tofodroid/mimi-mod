package io.github.tofodroid.mods.mimi.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ModCommands {
    public static final LiteralArgumentBuilder<CommandSourceStack> COMMANDS = Commands.literal("mimi")
        .then(Commands.literal("server")
            .then(Commands.literal("reload")
                .requires(cs -> cs.getServer().isSingleplayer() || cs.hasPermission(3))
                .executes(ctx -> reloadServerMusicList(ctx.getSource()))
            )
        );

    private static int reloadServerMusicList(CommandSourceStack source) {
        MIMIMod.getProxy().serverMidiFiles().refresh();
        source.sendSuccess(() -> Component.literal("Server saved music reloaded. Found " + MIMIMod.getProxy().serverMidiFiles().getSongCount() + " files"), true);
        return 0;
    }
}
