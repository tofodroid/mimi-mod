package io.github.tofodroid.mods.mimi.common.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;

import io.github.tofodroid.mods.mimi.server.midi.ServerMidiManager;
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
        ServerMidiManager.refreshServerSongs(true);
        source.sendSuccess(Component.literal("Server saved music reloaded. Found " + ServerMidiManager.getServerSongs().size() + " files."), true);
        return 0;
    }
}
