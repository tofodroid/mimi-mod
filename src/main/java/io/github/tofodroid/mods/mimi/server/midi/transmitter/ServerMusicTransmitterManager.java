package io.github.tofodroid.mods.mimi.server.midi.transmitter;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.sound.midi.Sequence;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.midi.BasicMidiInfo;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerSongListPacket;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacket;
import io.github.tofodroid.mods.mimi.common.network.TransmitterControlPacket;
import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;
import io.github.tofodroid.mods.mimi.server.ServerExecutor;
import io.github.tofodroid.mods.mimi.server.network.ServerMidiUploadManager;
import io.github.tofodroid.mods.mimi.util.EntityUtils;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public abstract class ServerMusicTransmitterManager {
    private static final Set<UUID> PLAYING_LIST = new HashSet<>();
    private static final Map<UUID,AServerMusicTransmitter> PLAYER_MAP = new HashMap<>();
    private static final Map<UUID, Set<UUID>> MIDI_LOAD_CACHE_MAP = new HashMap<>();
    private static ExecutorService pool;
    private static ExecutorService executor;
    private static Boolean hasPlaying = false;
    private static Boolean shuttingDown = false;

    public static Boolean isPlaying(UUID id) {
        return PLAYING_LIST.contains(id);
    }

    public static void addPlaying(UUID id) {
        PLAYING_LIST.add(id);
        hasPlaying = true;
    }

    public static void removePlaying(UUID id) {
        PLAYING_LIST.remove(id);
        hasPlaying = !PLAYING_LIST.isEmpty();
    }

    private static AServerMusicTransmitter getMusicPlayer(UUID id) {
        return PLAYER_MAP.get(id);
    }

    public static Boolean hasTransmitters() {
        return !PLAYER_MAP.isEmpty();
    }

    public static Boolean hasPlayingTransmitters() {
        return hasPlaying;
    }

    public static void configureMidiThread() {
        if(!shuttingDown) {
            if(pool == null) {
                pool = Executors.newFixedThreadPool(1);
            }

            if(executor == null) {
                executor = Executors.newSingleThreadExecutor();
            }
        }
    }

    public static void onPlayerLoggedIn(ServerPlayer player) {
        getOrCreateTransmitter(player);
    }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        removeTransmitter(player.getUUID());
    }

    public static void onServerAboutToStart() {
        MIMIMod.LOGGER.info("MIMI - SERVER STARTING");
        shuttingDown = false;
    }

    public static void onServerStopping() {
        shuttingDown = true;
        MIMIMod.LOGGER.info("MIMI - SERVER STOPPING");

        clearMusicPlayers();

        pool.shutdown();
        try {
            pool.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to orderly shutdown MIDI pool. Error: " + e.getMessage());
            try {
                pool.shutdownNow();
                pool.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch(Exception e2) {
                MIMIMod.LOGGER.error("Failed to force shutdown MIDI pool. Error: " + e.getMessage());
            }
        }
        pool = null;

        executor.shutdown();
        try {
            executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to orderly shutdown MIDI executor. Error: " + e.getMessage());
            try {
                executor.shutdownNow();
                executor.awaitTermination(5000, TimeUnit.MILLISECONDS);
            } catch(Exception e2) {
                MIMIMod.LOGGER.error("Failed to force shutdown MIDI executor. Error: " + e.getMessage());
            }
        }
        executor = null;
    }
    
    public static ServerMusicPlayerSongListPacket createListPacket(UUID musicPlayerId) {
        AServerMusicTransmitter transmitter = PLAYER_MAP.get(musicPlayerId);

        if(transmitter != null) {
            return new ServerMusicPlayerSongListPacket(musicPlayerId, transmitter.getCurrentSongsSorted(), transmitter.getCurrentFavoriteIndicies());
        }

        return null;
    }
    
    public static ServerMusicPlayerStatusPacket createStatusPacket(UUID musicPlayerId) {
        AServerMusicTransmitter transmitter = PLAYER_MAP.get(musicPlayerId);

        if(transmitter != null) {
            return transmitter.getStatus();
        }

        return null;
    }

    public static Boolean handleCommand(TransmitterControlPacket message) {
        AServerMusicTransmitter musicPlayer = ServerMusicTransmitterManager.getMusicPlayer(message.transmitterId);
        Boolean shouldRefreshSongs = false;

        if(musicPlayer != null) {
            switch(message.control) {
                case PLAY:
                    executeMidiTask(() -> {
                        musicPlayer.play();
                    }, (e) -> {
                        MIMIMod.LOGGER.error("MIMI failed to play transmitter: " + musicPlayer.id.toString(), e);
                    });
                    break;
                case PAUSE:
                    executeMidiTask(() -> {
                        musicPlayer.pause();
                    }, (e) -> {
                        MIMIMod.LOGGER.error("MIMI failed to pause transmitter: " + musicPlayer.id.toString(), e);
                    });
                    break;
                case STOP:
                    executeMidiTask(() -> {
                        musicPlayer.stop();
                    }, (e) -> {
                        MIMIMod.LOGGER.error("MIMI failed to stop transmitter: " + musicPlayer.id.toString(), e);
                    });
                    break;
                case RESTART:
                    executeMidiTask(() -> {
                        musicPlayer.stop();
                        musicPlayer.play();
                    }, (e) -> {
                        MIMIMod.LOGGER.error("MIMI failed to restart transmitter: " + musicPlayer.id.toString(), e);
                    });
                    break;
                case SEEK:
                    executeMidiTask(() -> {
                        musicPlayer.seek(message.controlData.get());
                    }, (e) -> {
                        MIMIMod.LOGGER.error("MIMI failed to seek transmitter: " + musicPlayer.id.toString(), e);
                    });
                    break;
                case PREV:
                    musicPlayer.previous();
                    break;
                case NEXT:
                    musicPlayer.next();
                    break;
                case SHUFFLE:
                    musicPlayer.toggleShuffled();
                    shouldRefreshSongs = true;
                    break;
                case LOOP_M:
                    musicPlayer.cycleLoopMode();
                    break;
                case FAVE_M:
                    musicPlayer.cycleFavoriteMode();
                    shouldRefreshSongs = true;
                    break;
                case SOURCE_M:
                    musicPlayer.cycleSourceMode();
                    shouldRefreshSongs = true;
                    break;
                case MARKFAVE:
                    musicPlayer.toggleSongFavorite();
                    shouldRefreshSongs = true;
                    break;
                default:
                    break;
            }

            return shouldRefreshSongs;
        }

        return false;
    }

    private static void executeMidiTask(Runnable runnable, Consumer<Exception> onFailed) {
        configureMidiThread();

        if(!shuttingDown && pool != null && executor != null) {
            pool.execute(() -> {
                Future<?> future = executor.submit(runnable);

                try {
                    future.get(10000, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    future.cancel(true);
                    onFailed.accept(e);
                }
            });
        }
    }
    
    public static AServerMusicTransmitter getOrCreateTransmitter(ServerPlayer player) {
        AServerMusicTransmitter handler = PLAYER_MAP.get(player.getUUID());

        if(handler == null) {
            handler = new PlayerTransmitterMusicTransmitter(player);
            PLAYER_MAP.put(player.getUUID(), handler);
            
            executeMidiTask(() -> {
                PLAYER_MAP.get(player.getUUID()).onLoad();
            }, (e) -> {
                MIMIMod.LOGGER.error("MIMI failed to start player transmitter: " + player.getUUID().toString(), e);
            });
        }

        return handler;
    }
    
    public static AServerMusicTransmitter getOrCreateTransmitter(TileTransmitter tile) {
        AServerMusicTransmitter handler = PLAYER_MAP.get(tile.getUUID());

        if(handler == null) {
            handler = new TileTransmitterMusicTransmitter(tile);
            PLAYER_MAP.put(tile.getUUID(), handler);

            executeMidiTask(() -> {
                PLAYER_MAP.get(tile.getUUID()).onLoad();
            }, (e) -> {
                MIMIMod.LOGGER.error("MIMI failed to start tile transmitter: " + tile.getUUID().toString(), e);
            });
        }

        return handler;
    }

    public static void removeTransmitter(UUID id) {
        executeMidiTask(() -> {
            AServerMusicTransmitter handler = PLAYER_MAP.remove(id);
            
            if(handler != null) {
                handler.close();
            }
            removePlaying(id);
        }, (e) -> {
            MIMIMod.LOGGER.error("MIMI failed to remove Transmitter with ID: " + id.toString(), e);
        });
    }
    
    public static void clearMusicPlayers() {
        executeMidiTask(() -> {
            for(UUID id : PLAYER_MAP.keySet()) {
                AServerMusicTransmitter player = PLAYER_MAP.get(id);

                if(player != null) {
                    player.close();
                }
            }
            PLAYING_LIST.clear();
            hasPlaying = false;
            PLAYER_MAP.clear();
        }, (e) -> {
            MIMIMod.LOGGER.error("MIMI failed clear transmitters", e);
        });
    }

    public static void onServerTick() {
        for(UUID playerId : PLAYER_MAP.keySet()) {
            ServerPlayer player = ServerExecutor.getServerPlayerById(playerId);
            if(player != null && !EntityUtils.playerHasActiveTransmitter(player)) {
                executeMidiTask(() -> {
                    PLAYER_MAP.get(player.getUUID()).stop();
                }, (e) -> {
                    MIMIMod.LOGGER.error("MIMI failed stop player transmitter with ID: " + player.getUUID().toString(), e);
                });
            }
        }
    }

    public static void refreshSongs(UUID playerId) {
        executeMidiTask(() -> {
            AServerMusicTransmitter handler = PLAYER_MAP.get(playerId);
            
            if(handler != null) {
                handler.refreshSongs();
            }
        }, (e) -> {
            MIMIMod.LOGGER.error("MIMI failed to refresh transmitter songs for ID: " + playerId.toString(), e);
        });
    }

    public static void onServerSongsRefreshed() {
        executeMidiTask(() -> {
            for(AServerMusicTransmitter transmitter : PLAYER_MAP.values()) {
                transmitter.refreshSongs();
            }
        }, (e) -> {
            MIMIMod.LOGGER.error("MIMI failed to refresh transmitter songs", e);
        });
    }

    public static void onLivingDeath(LivingEntity entity) {
        if(!(entity instanceof ServerPlayer) ){
            return;
        }
        
        executeMidiTask(() -> {
            AServerMusicTransmitter transmitter = getMusicPlayer(entity.getUUID());

            if(transmitter != null) {
                transmitter.stop();
            }
        }, (e) -> {
            MIMIMod.LOGGER.error("MIMI failed to stop transmitter with ID: " + entity.getUUID(), e);
        });
    }

    public static void onEntityChangeDimension(Entity entity) {
        if(!(entity instanceof ServerPlayer)) {
            return;
        }
        
        executeMidiTask(() -> {
            AServerMusicTransmitter transmitter = getMusicPlayer(entity.getUUID());

            if(transmitter != null) {
                transmitter.allNotesOff();
            }
        }, (e) -> {
            MIMIMod.LOGGER.error("MIMI failed to stop transmitter with ID: " + entity.getUUID(), e);
        });
    }

    public static void onEntityTeleport(Entity entity) {
        if(!(entity instanceof ServerPlayer)) {
            return;
        }

        executeMidiTask(() -> {
            AServerMusicTransmitter transmitter = getMusicPlayer(entity.getUUID());
            if(transmitter != null) {
                transmitter.allNotesOff();
            }
        }, (e) -> {
            MIMIMod.LOGGER.error("MIMI failed to stop transmitter with ID: " + entity.getUUID(), e);
        });
    }

    public static void onSelectedSongChange(UUID musicPlayerId, BasicMidiInfo newInfo) {
        AServerMusicTransmitter player = getMusicPlayer(musicPlayerId);

        if(player != null) {
            executeMidiTask(() -> {
                player.loadSong(newInfo);
            }, (e) -> {
                MIMIMod.LOGGER.error("MIMI failed to load song into transmitter with ID: " + musicPlayerId, e);
            });
        }
    }

    public static void startLoadSequence(UUID musicPlayerId, UUID clientId, BasicMidiInfo info) {
        if(MIDI_LOAD_CACHE_MAP.containsKey(info.fileId)) {
            MIDI_LOAD_CACHE_MAP.get(info.fileId).add(musicPlayerId);
        } else {
            MIDI_LOAD_CACHE_MAP.put(info.fileId, new HashSet<>());
            MIDI_LOAD_CACHE_MAP.get(info.fileId).add(musicPlayerId);
            ServerMidiUploadManager.startUploadRequest(clientId, info);
        }
    }

    public static void onSequenceUploadFailed(BasicMidiInfo info) {
        if(MIDI_LOAD_CACHE_MAP.containsKey(info.fileId)) {
            for(UUID musicPlayerId : MIDI_LOAD_CACHE_MAP.get(info.fileId)) {
                AServerMusicTransmitter player = getMusicPlayer(musicPlayerId);
                if(player != null) {
                    player.onSequenceLoadFailed(info);
                }
            }
            MIDI_LOAD_CACHE_MAP.remove(info.fileId);
        }
    }

    public static void onFinishUploadSequence(BasicMidiInfo info, Sequence sequence) {
        if(MIDI_LOAD_CACHE_MAP.containsKey(info.fileId)) {
            for(UUID musicPlayerId : MIDI_LOAD_CACHE_MAP.get(info.fileId)) {
                AServerMusicTransmitter player = getMusicPlayer(musicPlayerId);
                if(player != null) {
                    executeMidiTask(() -> {
                        player.finishLoadSequence(info, sequence);
                    }, (e) -> {
                        MIMIMod.LOGGER.error("MIMI failed to load song into transmitter with ID: " + musicPlayerId, e);
                    });
                }
            }
            MIDI_LOAD_CACHE_MAP.remove(info.fileId);
        }
    }
}
