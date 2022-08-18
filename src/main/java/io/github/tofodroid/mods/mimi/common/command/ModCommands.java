package io.github.tofodroid.mods.mimi.common.command;

import java.util.List;

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ModConfigs;
import io.github.tofodroid.mods.mimi.common.midi.MidiFileCacheManager;
import io.github.tofodroid.mods.mimi.util.RemoteMidiUrlUtils;
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
                    .then(Commands.literal("list")
                        .then(Commands.argument("page", IntegerArgumentType.integer(1, 99999))
                            .executes(ctx -> getServerMusicList(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))
                        )
                        .executes(ctx -> getServerMusicList(ctx.getSource(), 1))
                    )
                    .then(Commands.literal("reload")
                        .requires(cs -> cs.getServer().isSingleplayer() || cs.hasPermission(3))
                        .executes(ctx -> reloadServerMusicList(ctx.getSource()))
                    )
                    .then(Commands.literal("add")
                        .requires(cs -> cs.getServer().isSingleplayer() || cs.hasPermission(3))
                        .then(Commands.argument("url", StringArgumentType.string())
                            .then(Commands.argument("name", StringArgumentType.string())
                                .executes(ctx -> addServerSong(ctx.getSource(), StringArgumentType.getString(ctx, "url"), StringArgumentType.getString(ctx, "name")))
                            )
                        )
                    )
                    .then(Commands.literal("remove")
                    .requires(cs -> cs.getServer().isSingleplayer() || cs.hasPermission(3))
                        .then(Commands.argument("name", StringArgumentType.string())
                            .executes(ctx -> removeServerSong(ctx.getSource(), StringArgumentType.getString(ctx, "name")))
                        )
                    )
                )
                .then(Commands.literal("cache")
                    .requires(cs -> cs.getServer().isSingleplayer() || cs.hasPermission(3))
                    .then(Commands.literal("size")
                        .executes(ctx -> getServerCacheSize(ctx.getSource()))
                    )
                    .then(Commands.literal("set")
                        .then(Commands.argument("size", IntegerArgumentType.integer(0, 999))
                            .executes(ctx -> setServerCacheSize(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "size")))
                        )
                    )
                    .then(Commands.literal("prune")
                        .executes(ctx -> pruneServerCache(ctx.getSource()))
                    )
                    .then(Commands.literal("clear")
                        .executes(ctx -> emptyServerCache(ctx.getSource()))
                    )
                    .then(Commands.literal("reload")
                        .executes(ctx -> reloadServerCache(ctx.getSource()))
                    )
                )
                .then(Commands.literal("web")
                    .then(Commands.literal("list")
                        .then(Commands.argument("page", IntegerArgumentType.integer(1, 99999))
                            .executes(ctx -> listWebHosts(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "page")))
                        )
                        .executes(ctx -> listWebHosts(ctx.getSource(), 1))
                    )
                    .then(Commands.literal("status")
                        .executes(ctx -> getWebStatus(ctx.getSource()))
                    )
                    .then(Commands.literal("add")
                        .requires(cs -> cs.getServer().isSingleplayer() || cs.hasPermission(3))
                        .then(Commands.argument("host", StringArgumentType.string())
                            .executes(ctx -> addWebHost(ctx.getSource(), StringArgumentType.getString(ctx, "host")))
                        )
                    )
                    .then(Commands.literal("remove")
                        .requires(cs -> cs.getServer().isSingleplayer() || cs.hasPermission(3))
                        .then(Commands.argument("number", IntegerArgumentType.integer(1))
                                .executes(ctx -> removeWebHost(ctx.getSource(), IntegerArgumentType.getInteger(ctx, "number")))
                        )
                    )
                    .then(Commands.literal("set")
                        .requires(cs -> cs.getServer().isSingleplayer() || cs.hasPermission(3))
                        .then(Commands.argument("enabled", BoolArgumentType.bool())
                                .executes(ctx -> setWebStatus(ctx.getSource(), BoolArgumentType.getBool(ctx, "enabled")))
                        )
                    )
                )
        );
	}

    private static int getServerMusicList(CommandSourceStack source, Integer pageNum) {
        pageNum--;
        Integer numPages = MidiFileCacheManager.getServerFileNamesPages(5);
        List<String> fileNames = MidiFileCacheManager.getServerFileNames(5, pageNum);

        if(fileNames.isEmpty()) {
            source.sendSuccess(Component.literal("No files found"), false);
        } else if(pageNum >= numPages) {
            source.sendFailure(Component.literal("Invalid page number. Max: " + numPages));
            return 1;
        } else {
            source.sendSuccess(Component.literal("Server Music (" + (pageNum+1) + "/" + numPages + ")"), false);
            Integer resultNum = pageNum * 5;
            for(String file : fileNames) {
                source.sendSuccess(Component.literal((resultNum+1) + ". " + file), false);
                resultNum++;
            }
        }
        return 0;
    }

    private static int reloadServerMusicList(CommandSourceStack source) {
        MidiFileCacheManager.refreshServerSequenceMap();
        source.sendSuccess(Component.literal("Server saved music reloaded. Found " + MidiFileCacheManager.getServerFileNamesPages(1) + " files"), true);
        return 0;
    }

    
    private static int addServerSong(CommandSourceStack source, String url, String name) {
        url = url.strip();
        name = name.strip();

        if(!ModConfigs.COMMON.allowServerCommands.get()) {
            source.sendFailure(Component.literal("This command is currently disabled by the config file."));
            return 1;
        } else if(url.isEmpty() || url.length() > 256) {
            source.sendFailure(Component.literal("Supplied URL has invalid length. Max: 256 characters"));
            return 1;
        } else if(!RemoteMidiUrlUtils.validateMidiUrl(url)) {
            source.sendFailure(Component.literal("Supplied URL is not a valid MIDI URL"));
            return 1;
        } else if(name.length() < 3 || name.length() > 64) {
            source.sendFailure(Component.literal("Supplied name has invalid length. Min: 3 characters. Max: 64 characters"));
            return 1;
        } else if(!RemoteMidiUrlUtils.validateFilename(name)) {
            source.sendFailure(Component.literal("Supplied name is not a valid name. Name must use only letters, numbers, '-', and '_', and must start and end with a letter or number."));
            return 1;
        } else if(MidiFileCacheManager.hasServerFile(name)) {
            source.sendFailure(Component.literal("A server MIDI file already exists with that name."));
            return 1;
        }

        String resultName = MidiFileCacheManager.loadNewServerMusic(url, name);

        if(resultName == null) {
            source.sendFailure(Component.literal("Failed to download and save MIDI from supplied URL"));
            return 1;
        }

        source.sendSuccess(Component.literal("New Server MIDI downloaded and saved as '" + name + "'"), true);
        return 0;
    }

    private static int removeServerSong(CommandSourceStack source, String name) {
        name = name.strip();

        if(!ModConfigs.COMMON.allowServerCommands.get()) {
            source.sendFailure(Component.literal("This command is currently disabled by the config file."));
            return 1;
        } else if(name.length() < 3 || name.length() > 64) {
            source.sendFailure(Component.literal("Supplied name has invalid length. Min: 3 characters. Max: 64 characters"));
            return 1;
        } else if(!RemoteMidiUrlUtils.validateFilename(name)) {
            source.sendFailure(Component.literal("Supplied name is not a valid name. Name must use only letters, numbers, '-', and '_'"));
            return 1;
        } else if(!MidiFileCacheManager.hasServerFile(name)) {
            source.sendFailure(Component.literal("No server MIDI found with supplied name."));
            return 1;
        }

        if(!MidiFileCacheManager.removeServerMusic(name)) {
            source.sendFailure(Component.literal("Failed to delete server MIDI file. Check the logs for more info"));
            return 1;
        }
        
        source.sendSuccess(Component.literal("Successfully deleted server MIDI '" + name + "'"), true);
        return 0;
    }

    private static int getServerCacheSize(CommandSourceStack source) {
        source.sendSuccess(Component.literal("Server music cache: " + MidiFileCacheManager.getCachedFileNamePages(1) + "/" + ModConfigs.COMMON.serverMusicCacheSize.get() + " files"), false);
        return 0;
    }

    private static int setServerCacheSize(CommandSourceStack source, Integer size) {
        ModConfigs.COMMON.serverMusicCacheSize.set(size);
        source.sendSuccess(Component.literal("Server music cache size set to: " + ModConfigs.COMMON.serverMusicCacheSize.get() + " files"), true);
        return 0;
    }

    private static int pruneServerCache(CommandSourceStack source) {
        MidiFileCacheManager.pruneSequenceCache();
        source.sendSuccess(Component.literal("Server music cache pruned."), true);
        return 0;
    }

    private static int emptyServerCache(CommandSourceStack source) {
        MidiFileCacheManager.pruneSequenceCache();
        source.sendSuccess(Component.literal("Server music cache cleared"), true);
        return 0;
    }

    private static int reloadServerCache(CommandSourceStack source) {
        MidiFileCacheManager.refreshSequenceCacheMaps();
        source.sendSuccess(Component.literal("Server music cache reloaded. Found " + MidiFileCacheManager.getCachedFileNamePages(1) + " files."), true);
        return 0;
    }

    private static int listWebHosts(CommandSourceStack source, Integer pageNum) {
        pageNum--;

        List<String> hosts = ModConfigs.COMMON.getAllowedHostsList();
        Integer numPages = Double.valueOf(Math.ceil((double)hosts.size() / 5d)).intValue();
        
        if(hosts.isEmpty()) {
            source.sendSuccess(Component.literal("Server allows all web hosts"), false);
            return 0;
        } else if(pageNum >= numPages) {
            source.sendFailure(Component.literal("Invalid page number. Max: " + numPages));
            return 1;
        }

        source.sendSuccess(Component.literal("Allowed Hosts (" + (pageNum+1) + "/" + numPages + ")"), false);
        for(int i = pageNum*5; i < (pageNum*5 + 5); i++) {
            if(i >= hosts.size()) {
                return 0;
            }
            source.sendSuccess(Component.literal((i+1) + ". " + hosts.get(i)), false);
        }
        
        return 0;
    }

    private static int getWebStatus(CommandSourceStack source) {
        source.sendSuccess(Component.literal("Server Web MIDI is: " + (ModConfigs.COMMON.allowWebMidi.get() ? "enabled" : "disabled")), false);
        return 0;
    }
    
    private static int addWebHost(CommandSourceStack source, String host) {
        host = host.strip();

        if(!ModConfigs.COMMON.allowWebCommands.get()) {
            source.sendFailure(Component.literal("This command is currently disabled by the config file."));
            return 1;
        } else if(!RemoteMidiUrlUtils.validHostString(host)) {
            source.sendFailure(Component.literal("Provided host is invalid. Host must use the format of: http[s]://<host>.<extension>. Ex: https://bitmidi.com"));
            return 1;
        }
        ModConfigs.COMMON.addAllowedHost(host);
        source.sendSuccess(Component.literal("Host '" + host + "' added to Allowed Hosts."), true);
        return 0;
    }

    private static int removeWebHost(CommandSourceStack source, Integer number) {
        number--;

        if(!ModConfigs.COMMON.allowWebCommands.get()) {
            source.sendFailure(Component.literal("This command is currently disabled by the config file."));
            return 1;
        } else if(ModConfigs.COMMON.getAllowedHostsList().isEmpty()) {
            source.sendFailure(Component.literal("Allowed hosts list is already empty"));
            return 1;
        } else if(number >= ModConfigs.COMMON.getAllowedHostsList().size()) {
            source.sendFailure(Component.literal("Provided host number is too large. Max: " + ModConfigs.COMMON.getAllowedHostsList().size()));
            return 1;
        }
        String removedHost = ModConfigs.COMMON.getAllowedHostsList().get(number);
        ModConfigs.COMMON.removeAllowedHost(number);
        source.sendSuccess(Component.literal("Allowed Host '" + removedHost + "' removed."), true);
        return 0;
    }
    
    private static int setWebStatus(CommandSourceStack source, Boolean enable) {
        if(!ModConfigs.COMMON.allowWebCommands.get()) {
            source.sendFailure(Component.literal("This command is currently disabled by the config file."));
            return 1;
        }

        source.sendSuccess(Component.literal("Server Web MIDI set to: " + (ModConfigs.COMMON.allowWebMidi.get() ? "enabled" : "disabled")), true);
        return 0;
    }
}
