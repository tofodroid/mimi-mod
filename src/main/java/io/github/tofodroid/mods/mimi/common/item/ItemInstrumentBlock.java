package io.github.tofodroid.mods.mimi.common.item;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.UseOnContext;

public class ItemInstrumentBlock extends BlockItem implements IDyeableItem {
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
    
    @Override
    @Nonnull
    public InteractionResult useOn(UseOnContext context) {
        if(washItem(context)) {
            return InteractionResult.SUCCESS;
        }

        return super.useOn(context);
    }
}
