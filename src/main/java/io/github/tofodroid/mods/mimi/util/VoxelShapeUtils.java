package io.github.tofodroid.mods.mimi.util;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;

public abstract class VoxelShapeUtils {

    public static Map<Direction, VoxelShape> generateFacingShape(VoxelShape source) {
        Map<Direction, VoxelShape> result = new HashMap<>();

        result.put(Direction.SOUTH, source);
        result.put(Direction.WEST, horizontalRotateShape90(source));
        result.put(Direction.NORTH, horizontalRotateShape90(result.get(Direction.WEST)));
        result.put(Direction.EAST, horizontalRotateShape90(result.get(Direction.NORTH)));
        
        return result;
    }

    public static VoxelShape loadFromStrings(List<String> input) {
        try {
            VoxelShape resultShape;
            List<VoxelShape> inputShapes = new ArrayList<>();
            for(String shapeString : input) {
                List<Double> coords = Arrays.asList(shapeString.split(",")).stream().map(s -> Double.parseDouble(s)).collect(Collectors.toList());
                inputShapes.add(Block.box(coords.get(0), coords.get(1), coords.get(2), coords.get(3), coords.get(4), coords.get(5)));
            }
            
            resultShape = inputShapes.get(0);
            if(inputShapes.size() > 1) {
                for(int i = 1; i < inputShapes.size(); i++) {
                    resultShape = Shapes.or(resultShape, inputShapes.get(i));
                }
            }
            return resultShape;
        } catch(Exception e) {
            MIMIMod.LOGGER.error("Failed to load collision shape for instrument. Falling back to full cube. Error: ", e);
            return Block.box(0, 0, 0, 16, 16, 16);
        }
    }

    // Credit to forum user wyn_price - https://forums.minecraftforge.net/topic/74979-1144-rotate-voxel-shapes/
    private static VoxelShape horizontalRotateShape90(VoxelShape shape) {
        VoxelShape[] buffer = new VoxelShape[]{ shape, Shapes.empty() };
    
        buffer[0].forAllBoxes((minX, minY, minZ, maxX, maxY, maxZ) -> buffer[1] = Shapes.or(buffer[1], Shapes.create(1-maxZ, minY, minX, 1-minZ, maxY, maxX)));
    
        return buffer[1];
    }
}
