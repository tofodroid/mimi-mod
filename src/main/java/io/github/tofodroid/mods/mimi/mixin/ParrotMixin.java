package io.github.tofodroid.mods.mimi.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.animal.Parrot;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Parrot.class)
public abstract class ParrotMixin extends Entity {
    public ParrotMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Shadow
    private BlockPos jukebox;
    @Shadow
    private boolean partyParrot;

    @Unique
    private Instant mimiNoteTime;

    @Inject(method = "setRecordPlayingNearby", at = @At("HEAD"))
    public void captureSet(BlockPos pos, boolean playing, CallbackInfo ci) {
        if(!this.level().getBlockState(pos).is(Blocks.JUKEBOX)) {
            this.mimiNoteTime = Instant.now();
        }
    }
    
    @Inject(method = "isPartyParrot", at = @At("RETURN"), cancellable = true)
    private void isPartyParrotMimi(CallbackInfoReturnable<Boolean> cir) {
        this.mimiNoteTime = this.mimiNoteTime != null && Math.abs(ChronoUnit.SECONDS.between(Instant.now(), this.mimiNoteTime)) < 1 ? this.mimiNoteTime : null;
        cir.setReturnValue(cir.getReturnValue() || this.mimiNoteTime != null);
    }
}