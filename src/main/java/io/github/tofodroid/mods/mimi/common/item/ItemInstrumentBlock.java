package io.github.tofodroid.mods.mimi.common.item;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import net.minecraft.world.item.BlockItem;

public class ItemInstrumentBlock extends BlockItem implements IDyeableInstrumentItem {
    public final String REGISTRY_NAME;

    public ItemInstrumentBlock(BlockInstrument blockIn, Properties builder, String name) {
        super(blockIn, builder);
        this.REGISTRY_NAME = name;
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
