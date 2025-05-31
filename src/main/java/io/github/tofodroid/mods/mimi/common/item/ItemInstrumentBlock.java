package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

public class ItemInstrumentBlock extends ItemMidiSourceLinkableBlock implements IInstrumentItem {
    protected final String REGISTRY_NAME;
    protected final BlockInstrument instrumentBlock;

    public ItemInstrumentBlock(BlockInstrument blockIn, Properties props, String name) {
        super(blockIn, props);
        this.REGISTRY_NAME = name;
        this.instrumentBlock = blockIn;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);
        this.appendSettingsTooltip(stack, tooltip);
    }

    @Override
    public Byte getInstrumentId() {
        return this.instrumentBlock.getInstrumentId();
    }

    @Override
    public Integer getDefaultColor() {
        return this.instrumentBlock.getDefaultColor();
    }

    @Override
    public Integer getDefaultChannels() {
        return this.instrumentBlock.getDefaultChannels();
    }

    @Override
    public String getRegistryName() {
        return this.REGISTRY_NAME;
    }
}
