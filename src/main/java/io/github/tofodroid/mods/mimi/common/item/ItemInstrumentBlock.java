package io.github.tofodroid.mods.mimi.common.item;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import net.minecraft.world.item.BlockItem;

public class ItemInstrumentBlock extends BlockItem implements IDyeableInstrumentItem {    
    public ItemInstrumentBlock(BlockInstrument blockIn, Properties builder) {
        super(blockIn, builder);
    }

    public Byte getInstrumentId() {
        return ((BlockInstrument)getBlock()).getInstrumentId();
    }

    @Override
    public Boolean isDyeable() {
        return ((BlockInstrument)getBlock()).isDyeable();
    }

    @Override
    public Integer getDefaultColor() {
        return ((BlockInstrument)getBlock()).getDefaultColor();
    }
}
