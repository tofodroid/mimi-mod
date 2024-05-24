package io.github.tofodroid.mods.mimi.common.block;

import java.util.List;

import com.mojang.serialization.MapCodec;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockReceiver extends AConfigurableMidiPowerSourceBlock<TileReceiver> {
    public static final String REGISTRY_NAME = "receiver";
    public static final MapCodec<BlockReceiver> CODEC = simpleCodec(BlockReceiver::new);
 
    @Override
    public MapCodec<BlockReceiver> codec() {
       return CODEC;
    }

    public BlockReceiver(Properties props) {
        super(props.explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD).isRedstoneConductor((a,b,c) -> false));
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
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!worldIn.isClientSide) {
            if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
                return;
            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            
            if (blockEntity instanceof TileReceiver) {
                BroadcastManager.removeOwnedBroadcastConsumers(((TileReceiver)blockEntity).getUUID());
            }
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    protected void appendSettingsTooltip(ItemStack blockItemStack, List<Component> tooltip) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("MIDI Settings:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        Integer enabledChannels = MidiNbtDataUtils.getEnabledChannelsInt(blockItemStack);
        if(enabledChannels != null) {
            if(enabledChannels.equals(MidiNbtDataUtils.ALL_CHANNELS_INT)) {
                tooltip.add(Component.literal("  Channels: All").withStyle(ChatFormatting.GREEN));
            } else if(enabledChannels.equals(MidiNbtDataUtils.NONE_CHANNELS_INT)) {
                tooltip.add(Component.literal("  Channels: None").withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.literal("  Channels: " + MidiNbtDataUtils.getEnabledChannelsAsString(enabledChannels)).withStyle(ChatFormatting.GREEN));
            }
        }

        // Invert Signal
        tooltip.add(Component.literal("  Invert Power: " 
            + (MidiNbtDataUtils.getInvertSignal(blockItemStack) ? "Yes " : "No")).withStyle(ChatFormatting.GREEN)
        );

        // Note Source
        if(MidiNbtDataUtils.getMidiSource(blockItemStack) != null) {
            Boolean isTransmitter = MidiNbtDataUtils.getMidiSourceIsTransmitter(blockItemStack);
            Boolean isRelay = MidiNbtDataUtils.getMidiSourceIsRelay(blockItemStack);
            tooltip.add(Component.literal("  Recieve Notes From: " + (isTransmitter ? "Transmitter:" : ( isRelay ? "Relay:" : "Player:"))).withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal("  " + MidiNbtDataUtils.getMidiSourceName(blockItemStack, true)).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.literal("  Recieve Notes From: None").withStyle(ChatFormatting.GREEN));
        }

        // Filter Note
        tooltip.add(Component.literal("  Note(s): " 
            + (MidiNbtDataUtils.getInvertNoteOct(blockItemStack) ? "Not " : "")
            + MidiNbtDataUtils.getFilteredNotesAsString(blockItemStack)).withStyle(ChatFormatting.GREEN)
        );
    }
}