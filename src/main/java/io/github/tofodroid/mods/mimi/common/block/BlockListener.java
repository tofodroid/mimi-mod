package io.github.tofodroid.mods.mimi.common.block;

import java.util.List;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileListener;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class BlockListener extends AConfigurableMidiPowerSourceBlock<TileListener> {
    public static final String REGISTRY_NAME = "listener";

    public BlockListener(Properties props) {
        super(props.explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD).isRedstoneConductor((a,b,c) -> false));
    }

    @Override
    protected void openGui(Level worldIn, Player player, TileListener tile) {
        ClientGuiWrapper.openListenerGui(worldIn, tile.getBlockPos(), tile.getSourceStack());
    }

    @Override
    public BlockEntityType<TileListener> getTileType() {
        return ModTiles.LISTENER;
    }

    @Override
    protected void appendSettingsTooltip(ItemStack blockItemStack, List<Component> tooltip) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("MIDI Settings:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        // Invert Signal
        tooltip.add(Component.literal("  Invert Power: " 
            + (MidiNbtDataUtils.getInvertSignal(blockItemStack) ? "Yes " : "No")).withStyle(ChatFormatting.GREEN)
        );

        // Filter Instrument
        tooltip.add(Component.literal("  Instrument: " 
            + (MidiNbtDataUtils.getInvertInstrument(blockItemStack) ? "Not " : "")
            + MidiNbtDataUtils.getInstrumentName(MidiNbtDataUtils.getFilterInstrument(blockItemStack))).withStyle(ChatFormatting.GREEN)
        );

        // Filter Note
        tooltip.add(Component.literal("  Note(s): " 
            + (MidiNbtDataUtils.getInvertNoteOct(blockItemStack) ? "Not " : "")
            + MidiNbtDataUtils.getFilteredNotesAsString(blockItemStack)).withStyle(ChatFormatting.GREEN)
        );
    }
}
