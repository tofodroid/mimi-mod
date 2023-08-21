package io.github.tofodroid.mods.mimi.client.midi;

import java.util.UUID;
import io.github.tofodroid.mods.mimi.common.midi.ATransmitterManager;
import net.minecraft.client.Minecraft;

public class EnderTransmitterManager extends ATransmitterManager {
    private Boolean shuffled = null;
    private LoopMode loopMode = null;
    private UUID playerId = null;

    @Override
    public Boolean supportsLocal() {
        return true;
    }

    @Override
    @SuppressWarnings({"null", "resource"})
    public UUID transmitterId() {
        if(playerId == null) { 
            this.playerId = Minecraft.getInstance().player.getUUID();
        }
        return this.playerId;
    }

    @Override
    public Boolean isShuffled() {
        if(this.shuffled == null) {
            this.shuffled = false;
        }
        return this.shuffled;
    }

    @Override
    public LoopMode loopMode() {
        if(this.loopMode == null) {
            this.loopMode = LoopMode.NONE;
        }
        return this.loopMode;
    }

    @Override
    public void toggleShuffled() {
        this.shuffled = !this.shuffled;
    }

    @Override
    public void setLoopMode(LoopMode mode) {
        this.loopMode = mode;
    }
    
}
