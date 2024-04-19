package io.github.tofodroid.mods.mimi.client.midi.synth;

import io.github.tofodroid.mods.mimi.forge.common.config.ModConfigs;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.phys.Vec3;

public abstract class MIMISynthUtils {
    
    public static Byte getVolumeForRelativeNotePosition(Vec3 playerPos, BlockPos notePos) {
        Vec3 notePosReal = new Vec3(notePos.getX()+0.5D, notePos.getY()+0.5D, notePos.getZ()+0.5D);
        return getVolumeForRelativeNoteDistance(distanceBetween(playerPos, notePosReal));
    }

    public static Byte getVolumeForRelativeNoteDistance(Double distance) {
        return getVelocityForRelativeNoteDistance(distance, true);
    }


    @SuppressWarnings("resource")
    public static Byte getVelocityForRelativeNoteDistance(Double distance, Boolean applyGameVolume) {
        Double volume = 127d;
        
        // 1. Adjust for distance
        if(distance > 0) {
            volume -= Math.floor((127 * Math.pow(distance,2.5)) / (Math.pow(distance,2.5) + Math.pow(64 - distance,2.5)));
        }

        // 2. Adjust for game volume
        if(applyGameVolume) {
            // Audio Volume is from 0-10
            Double mimiVolume = Double.valueOf(ModConfigs.CLIENT.audioDeviceVolume.get()) / 10.0d;
            volume *= mimiVolume;

            // Minecraft Settings Volumes
            Float catVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.RECORDS);
            catVolume *= Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
            volume *= catVolume.doubleValue();
        }

        // Clamp
        volume = volume < 0 ? 0 : volume > 127 ? 127 : volume;
        Byte byteVal = Integer.valueOf(volume.intValue()).byteValue();

        return byteVal;
    }

    public static Byte getLRPanForRelativeNotePosition(Vec3 playerPos, BlockPos notePos, Float playerHeadRoationYaw) {
        // Calculate
        Vec3 notePosReal = new Vec3(notePos.getX()+0.5D, playerPos.y(), notePos.getZ()+0.5D);
        Double distance2D = distanceBetween(playerPos, notePosReal);
        Double distanceMult = Math.max(0, Math.min(0.5, distance2D/64.0D));
        Double posAngle = angleBetween(playerPos, notePosReal);
        Double headAngle = (playerHeadRoationYaw.doubleValue() < 0 ? playerHeadRoationYaw.doubleValue() + 360 : playerHeadRoationYaw.doubleValue()) % 360;
        Double relativeVal = 64 * Math.sin(Math.toRadians((posAngle.doubleValue() - headAngle.doubleValue() + 630) % 360));
               relativeVal *= (distance2D <= 1.0D ? 0 : ((distance2D <= 2.0D ? 0.25 : 0.5) + distanceMult));

        // Clamp
        Integer lrPan = 63 + relativeVal.intValue();
                lrPan = lrPan < 0 ? 0 : lrPan > 127 ? 127 : lrPan;
        Byte byteVal = lrPan.byteValue();

        return byteVal;
    }

    public static Double distanceBetween(Vec3 source, Vec3 target) {
        return Math.sqrt(source.distanceToSqr(target));
    }

    public static Double angleBetween(Vec3 source, Vec3 target) {
        Double angle = (Double) Math.toDegrees(Math.atan2(target.z() - source.z(), target.x() - source.x()));
        return (angle < 0 ? angle + 360 : angle) % 360;
    }
}
