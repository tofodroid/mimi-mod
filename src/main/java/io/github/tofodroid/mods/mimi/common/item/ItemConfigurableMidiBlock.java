package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;

import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.block.AConfigurableMidiBlock;
import io.github.tofodroid.mods.mimi.common.tile.AConfigurableMidiTile;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemConfigurableMidiBlock<T extends AConfigurableMidiBlock<? extends AConfigurableMidiTile>> extends BlockItem {
    public final String REGISTRY_NAME;

    public ItemConfigurableMidiBlock(T blockIn, Properties builder, String name) {
        super(blockIn, builder);
        this.REGISTRY_NAME = name;
    }
}
