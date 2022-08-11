package io.github.tofodroid.mods.mimi.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public abstract class BlockUtils {
    public static void addRandomNoteParticleToBlock(BlockState p_221932_, Level p_221933_, BlockPos p_221934_, RandomSource p_221935_, Double oddsOutOfOne) {
        double d0 = (double)p_221934_.getX() + 0.5D;
        double d1 = (double)p_221934_.getY();
        double d2 = (double)p_221934_.getZ() + 0.5D;
        if (p_221935_.nextDouble() < oddsOutOfOne) {
            Double dirVal = p_221935_.nextDouble();
            Direction direction;
            if(dirVal < 0.25D) {
                direction = Direction.NORTH;
            } else if(dirVal < 0.5D) {
                direction = Direction.EAST;
            } else if(dirVal < 0.75D) {
                direction = Direction.SOUTH;
            } else {
                direction = Direction.WEST;
            }
            
            Direction.Axis direction$axis = direction.getAxis();
            double d4 = p_221935_.nextDouble() * 0.6D - 0.3D;
            double d5 = direction$axis == Direction.Axis.X ? (double)direction.getStepX() * 0.62D : d4;
            double d6 = p_221935_.nextDouble() * 6.0D / 16.0D;
            double d7 = direction$axis == Direction.Axis.Z ? (double)direction.getStepZ() * 0.62D : d4;
            p_221933_.addParticle(ParticleTypes.NOTE, d0 + d5, d1 + d6, d2 + d7, p_221935_.nextDouble(), p_221935_.nextDouble(), p_221935_.nextDouble());
        }
    }
}
