package io.github.tofodroid.mods.mimi.client.midi.synth;

import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundSource;

public abstract class MIMISynthUtils {
    
    public static Byte getVolumeForRelativeNotePosition(BlockPos playerPos, BlockPos notePos) {
        return getVolumeForRelativeNoteDistance(distanceBetween(playerPos, notePos));
    }

    @SuppressWarnings("resource")
    public static Byte getVolumeForRelativeNoteDistance(Double distance) {
        Double volume = 127d;
        
        // 1. Adjust for distance
        if(distance > 0) {
            volume -= Math.floor((127 * Math.pow(distance,2.5)) / (Math.pow(distance,2.5) + Math.pow(72 - distance,2.5)));
        }

        // 2. Adjust for game volume
        Float catVolume = Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.RECORDS);
              catVolume *= Minecraft.getInstance().options.getSoundSourceVolume(SoundSource.MASTER);
              volume *= catVolume.doubleValue();

        // Clamp
        volume = volume < 0 ? 0 : volume > 127 ? 127 : volume;
        Byte byteVal = Integer.valueOf(volume.intValue()).byteValue();

        return byteVal;
    }

    public static Byte getLRPanForRelativeNotePosition(BlockPos playerPos, BlockPos notePos, Float playerHeadRoationYaw) {
        // Calculate
        Float posAngle = angleBetween(playerPos, notePos);
        Float headAngle = playerHeadRoationYaw;
              headAngle = (headAngle < 0 ? headAngle + 360 : headAngle) % 360;
        Double relativeAngle = (posAngle.doubleValue() - headAngle.doubleValue() + 630) % 360;
        Double relVal = 64 * Math.sin(Math.toRadians(relativeAngle));
               relVal *= 0.5;

        // Clamp
        Integer lrPan = 63 + relVal.intValue();
                lrPan = lrPan < 0 ? 0 : lrPan > 127 ? 127 : lrPan;
        Byte byteVal = lrPan.byteValue();

        return byteVal;
    }

    public static Double distanceBetween(BlockPos source, BlockPos target) {
        return Math.sqrt(source.distSqr(target));
    }

    public static Float angleBetween(BlockPos source, BlockPos target) {
        Float angle = (float) Math.toDegrees(Math.atan2(target.getZ() - source.getZ(), target.getX() - source.getX()));
        return (angle < 0 ? angle + 360 : angle) % 360;
    }
}
