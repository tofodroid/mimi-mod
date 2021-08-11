package io.github.tofodroid.mods.mimi.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.util.Direction;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.shapes.VoxelShapes;

import java.util.Map;

import io.github.tofodroid.mods.mimi.common.midi.MidiInstrument;
import io.github.tofodroid.mods.mimi.util.VoxelShapeUtils;

public class BlockDrums extends BlockInstrument {
    public BlockDrums() {
        super(
            Properties.create(Material.WOOD).hardnessAndResistance(2.f, 6.f).sound(SoundType.WOOD).notSolid(),
            MidiInstrument.DRUMS.getId()
        );
        this.setRegistryName("drums");
    }

    @Override
    protected Map<Direction, VoxelShape> generateShapes() {
        return VoxelShapeUtils.generateFacingShape(VoxelShapes.or(
            Block.makeCuboidShape(0, 0, 0, 16, 12, 8), 
            Block.makeCuboidShape(0, 0, 0, 16, 4, 12)
        ).simplify());
    }
}
