package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.Material;

public class BlockMechanicalMaestro extends AContainerBlock<TileMechanicalMaestro> {
    public BlockMechanicalMaestro() {
        super(Properties.of(Material.METAL).explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
        this.setRegistryName("mechanicalmaestro");
    }
    
    @Override
    public BlockEntityType<TileMechanicalMaestro> getTileType() {
        return ModTiles.MECHANICALMAESTRO;
    }
}