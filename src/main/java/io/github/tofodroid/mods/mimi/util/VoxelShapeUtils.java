package io.github.tofodroid.mods.mimi.util;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

public abstract class VoxelShapeUtils {

    public static Map<Direction, VoxelShape> generateFacingShape(VoxelShape source) {
        Map<Direction, VoxelShape> result = new HashMap<>();

        result.put(Direction.SOUTH, source);
        result.put(Direction.WEST, horizontalRotateShape90(source));
        result.put(Direction.NORTH, horizontalRotateShape90(result.get(Direction.WEST)));
        result.put(Direction.EAST, horizontalRotateShape90(result.get(Direction.NORTH)));
        
        return result;
    }

    // Credit to forum user wyn_price - https://forums.minecraftforge.net/topic/74979-1144-rotate-voxel-shapes/
    private static VoxelShape horizontalRotateShape90(VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{ shape, VoxelShapes.empty() };
    
        buffer[0].forEachBox((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = VoxelShapes.or(buffer[1], VoxelShapes.create(1-maxZ, minY, minX, 1-minZ, maxY, maxX)));
    
        return buffer[1];
    }
}
