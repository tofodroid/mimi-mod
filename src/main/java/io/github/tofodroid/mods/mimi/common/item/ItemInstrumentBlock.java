package io.github.tofodroid.mods.mimi.common.item;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import net.minecraft.item.BlockItem;

public class ItemInstrumentBlock extends BlockItem {
    public ItemInstrumentBlock(BlockInstrument blockIn, Properties builder) {
        super(blockIn, builder);
    }

    public Byte getInstrumentId() {
        return ((BlockInstrument)getBlock()).getInstrumentId();
    }
}
