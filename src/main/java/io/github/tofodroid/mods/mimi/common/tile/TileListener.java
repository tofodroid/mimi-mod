package io.github.tofodroid.mods.mimi.common.tile;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.container.Container;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;

public class TileListener extends ANoteResponsiveTile {
    public TileListener() {
        super(ModTiles.LISTENER, 0);
    }

    @Override
    public Container createMenu(int id, PlayerInventory playerInventory, PlayerEntity playerEntity) {
        return null;
    }

    @Override
    public ITextComponent getDisplayName() {
		return new TranslationTextComponent(this.getBlockState().getBlock().asItem().getTranslationKey());
    }
    
    @Override
    protected Boolean shouldHaveEntity() {
        return true;
    }
}
