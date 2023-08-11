package io.github.tofodroid.mods.mimi.common.block;

import java.util.List;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockReceiver extends APoweredConfigurableMidiBlock<TileReceiver> {
    public static final String REGISTRY_NAME = "receiver";

    public BlockReceiver() {
        super(Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
    }

    @Override
    protected void openGui(Level worldIn, Player player, TileReceiver tile) {
        ClientGuiWrapper.openReceiverGui(worldIn, player, tile.getBlockPos(), tile.getSourceStack());
    }

    @Override
    public BlockEntityType<TileReceiver> getTileType() {
        return ModTiles.RECEIVER;
    }

    @Override
    protected void appendMidiSettingsTooltip(ItemStack blockItemStack, List<Component> tooltip) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("MIDI Settings:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        String enabledChannels = InstrumentDataUtils.getEnabledChannelsString(blockItemStack);
        if(enabledChannels != null && !enabledChannels.isEmpty()) {
            if(enabledChannels.equals(InstrumentDataUtils.ALL_CHANNELS_STRING)) {
                tooltip.add(Component.literal("  Channels: All").withStyle(ChatFormatting.GREEN));
            } else if(enabledChannels.equals(InstrumentDataUtils.NONE_CHANNELS_STRING)) {
                tooltip.add(Component.literal("  Channels: None").withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.literal("  Channels: " + enabledChannels).withStyle(ChatFormatting.GREEN));
            }
        }

        // Note Source
        tooltip.add(Component.literal("  Receive Notes From: " + InstrumentDataUtils.getMidiSourceName(blockItemStack)).withStyle(ChatFormatting.GREEN));

        // Filter Note
        tooltip.add(Component.literal("  Note(s): " 
            + (InstrumentDataUtils.getInvertNoteOct(blockItemStack) ? " Not " : "")
            + InstrumentDataUtils.getFilteredNotesAsString(blockItemStack)).withStyle(ChatFormatting.GREEN)
        );
    }
}