package io.github.tofodroid.mods.mimi.server.events.broadcast.producer;

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
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import io.github.tofodroid.mods.mimi.server.network.ServerMidiUploadManager;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

public abstract class ServerTransmitterManager {
    private static final Map<UUID, Set<UUID>> MIDI_LOAD_CACHE_MAP = new HashMap<>();
    private static ExecutorService pool;
    private static ExecutorService executor;
    private static Boolean shuttingDown = false;

    private static ATransmitterBroadcastProducer getTransmitter(UUID id) {
        ABroadcastProducer producer = BroadcastManager.getBroadcastProducer(id);

        if(producer instanceof ATransmitterBroadcastProducer) {
            return (ATransmitterBroadcastProducer)producer;
        }
        return null;
    }

    public static void createTransmitter(ServerPlayer player) {
        BroadcastManager.registerProducer(new PlayerBroadcastProducer(player));
    }

    public static void createTransmitter(TileTransmitter tile) {
        BroadcastManager.registerProducer(new TileTransmitterBroadcastProducer(tile));
    }

    public static void executeTaskOnMidiThread(Runnable runnable, Consumer<Exception> onFailed) {
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

    public static void onServerAboutToStart() {
        shuttingDown = false;
    }

    public static void onServerStopping() {
        shuttingDown = true;

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
        ATransmitterBroadcastProducer transmitter = getTransmitter(musicPlayerId);

        if(transmitter != null) {
            return new ServerMusicPlayerSongListPacket(musicPlayerId, transmitter.getCurrentSongsSorted(), transmitter.getCurrentFavoriteIndicies());
        }

        return null;
    }
    
    public static ServerMusicPlayerStatusPacket createStatusPacket(UUID musicPlayerId) {
        ATransmitterBroadcastProducer transmitter = getTransmitter(musicPlayerId);

        if(transmitter != null) {
            return transmitter.getStatus();
        }

        return null;
    }

    public static Boolean handleCommand(TransmitterControlPacket message) {
        ATransmitterBroadcastProducer musicPlayer = ServerTransmitterManager.getTransmitter(message.transmitterId);
        Boolean shouldRefreshSongs = false;

        if(musicPlayer != null) {
            switch(message.control) {
                case PLAY:
                    executeTaskOnMidiThread(() -> {
                        musicPlayer.play();
                    }, (e) -> {
                        MIMIMod.LOGGER.error("MIMI failed to play transmitter: " + musicPlayer.ownerId.toString(), e);
                    });
                    break;
                case PAUSE:
                    executeTaskOnMidiThread(() -> {
                        musicPlayer.pause();
                    }, (e) -> {
                        MIMIMod.LOGGER.error("MIMI failed to pause transmitter: " + musicPlayer.ownerId.toString(), e);
                    });
                    break;
                case STOP:
                    executeTaskOnMidiThread(() -> {
                        musicPlayer.stop();
                    }, (e) -> {
                        MIMIMod.LOGGER.error("MIMI failed to stop transmitter: " + musicPlayer.ownerId.toString(), e);
                    });
                    break;
                case RESTART:
                    executeTaskOnMidiThread(() -> {
                        musicPlayer.stop();
                        musicPlayer.play();
                    }, (e) -> {
                        MIMIMod.LOGGER.error("MIMI failed to restart transmitter: " + musicPlayer.ownerId.toString(), e);
                    });
                    break;
                case SEEK:
                    executeTaskOnMidiThread(() -> {
                        musicPlayer.seek(message.controlData.get());
                    }, (e) -> {
                        MIMIMod.LOGGER.error("MIMI failed to seek transmitter: " + musicPlayer.ownerId.toString(), e);
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

    public static void refreshSongs(UUID playerId) {
        executeTaskOnMidiThread(() -> {
            ATransmitterBroadcastProducer handler = getTransmitter(playerId);
            
            if(handler != null) {
                handler.refreshSongs();
            }
        }, (e) -> {
            MIMIMod.LOGGER.error("MIMI failed to refresh transmitter songs for ID: " + playerId.toString(), e);
        });
    }


    public static void onServerSongsRefreshed() {
        executeTaskOnMidiThread(() -> {
            for(ATransmitterBroadcastProducer transmitter : BroadcastManager.getBroadcastProducersByType(ATransmitterBroadcastProducer.class)) {
                transmitter.refreshSongs();
            }
        }, (e) -> {
            MIMIMod.LOGGER.error("MIMI failed to refresh transmitter songs", e);
        });
    }

    public static void onSelectedSongChange(UUID musicPlayerId, BasicMidiInfo newInfo) {
        ATransmitterBroadcastProducer player = getTransmitter(musicPlayerId);

        if(player != null) {
            executeTaskOnMidiThread(() -> {
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
                ATransmitterBroadcastProducer player = getTransmitter(musicPlayerId);
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
                ATransmitterBroadcastProducer player = getTransmitter(musicPlayerId);
                if(player != null) {
                    executeTaskOnMidiThread(() -> {
                        player.finishLoadSequence(info, sequence);
                    }, (e) -> {
                        MIMIMod.LOGGER.error("MIMI failed to load song into transmitter with ID: " + musicPlayerId, e);
                    });
                }
            }
            MIDI_LOAD_CACHE_MAP.remove(info.fileId);
        }
    }
    

    public static void onLivingDeath(LivingEntity entity) {
        if(!(entity instanceof ServerPlayer) ){
            return;
        }
        
        executeTaskOnMidiThread(() -> {
            ATransmitterBroadcastProducer transmitter = getTransmitter(entity.getUUID());

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
        
        executeTaskOnMidiThread(() -> {
            ATransmitterBroadcastProducer transmitter = getTransmitter(entity.getUUID());

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

        executeTaskOnMidiThread(() -> {
            ATransmitterBroadcastProducer transmitter = getTransmitter(entity.getUUID());
            if(transmitter != null) {
                transmitter.allNotesOff();
            }
        }, (e) -> {
            MIMIMod.LOGGER.error("MIMI failed to stop transmitter with ID: " + entity.getUUID(), e);
        });
    }
}
