package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import net.minecraft.block.material.Material;
import net.minecraft.block.SoundType;
import net.minecraft.tileentity.TileEntityType;

public class BlockMechanicalMaestro extends AContainerBlock<TileMechanicalMaestro> {
    public BlockMechanicalMaestro() {
        super(Properties.create(Material.IRON).hardnessAndResistance(2.f, 6.f).sound(SoundType.WOOD));
        this.setDefaultState(this.stateContainer.getBaseState());
        this.setRegistryName("mechanicalmaestro");
    }
    
    @Override
    public TileEntityType<TileMechanicalMaestro> getTileType() {
        return ModTiles.MECHANICALMAESTRO;
    }
}