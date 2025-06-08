package io.github.tofodroid.mods.mimi.mixin;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import io.github.tofodroid.mods.mimi.common.api.event.note.NoteEvent;
import io.github.tofodroid.mods.mimi.common.config.ConfigProxy;

import javax.annotation.Nullable;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {

    @Shadow public abstract void addParticle(ParticleOptions options, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed);
    @Shadow protected abstract void notifyNearbyEntities(Level level, BlockPos pos, boolean playing);

    @Shadow @Nullable private ClientLevel level;

    @Shadow @Nullable protected abstract Particle addParticleInternal(ParticleOptions options, boolean force, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed);

    @Inject(method = "levelEvent", at = @At("HEAD"), cancellable = true)
    private void onMIMINoteEvent(int type, BlockPos pos, int data, CallbackInfo ci) {
        if(ConfigProxy.noteParticlesEnabled() && type == NoteEvent.MIMI_NOTE_PLAYING_LEVEL_EVENT_ID) {
            Vec3 vec3 = Vec3.atBottomCenterOf(pos).add(0.0, 1.2F, 0.0);
            Float noteVal = ((data + 6) % 12f) / 12f;
            addParticle(ParticleTypes.NOTE, vec3.x() + level.getRandom().nextDouble() - 0.5d, vec3.y(), vec3.z() + level.getRandom().nextDouble() - 0.5d, noteVal, 0.0, 0.0);
            notifyNearbyEntities(level, pos, true);
            ci.cancel();
        }
    }

}