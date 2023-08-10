package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileListener;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockListener extends APoweredConfigurableMidiBlock<TileListener> {
    public static final String REGISTRY_NAME = "listener";

    public BlockListener() {
        super(Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
    }

    @Override
    protected void openGui(Level worldIn, Player player, TileListener tile) {
        ClientGuiWrapper.openListenerGui(worldIn, player, tile.getSourceStack());
    }

    @Override
    public BlockEntityType<TileListener> getTileType() {
        return ModTiles.LISTENER;
    }
}
