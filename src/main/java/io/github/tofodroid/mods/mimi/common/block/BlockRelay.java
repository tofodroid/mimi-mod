package io.github.tofodroid.mods.mimi.common.block;

import java.util.List;

import com.mojang.serialization.MapCodec;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import io.github.tofodroid.mods.mimi.common.tile.TileRelay;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BlockRelay extends AConfigurableNoteResponsiveTileBlock<TileRelay> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final String REGISTRY_NAME = "relay";
    public static final MapCodec<BlockRelay> CODEC = simpleCodec(BlockRelay::new);
 
    @Override
    public MapCodec<BlockRelay> codec() {
       return CODEC;
    }

    public BlockRelay(Properties props) {
        super(props.explosionResistance(6.f).strength(2.f).sound(SoundType.METAL).isRedstoneConductor((a,b,c) -> false));
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(POWERED, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(POWERED);
    }

    @Override
    protected void openGui(Level worldIn, Player player, TileRelay tile) {
        ClientGuiWrapper.openRelayGui(worldIn, player, tile.getBlockPos(), tile.getSourceStack());
    }

    @Override
    public BlockEntityType<TileRelay> getTileType() {
        return ModTiles.RELAY;
    }

    @Override
    public InteractionResult useWithoutItem(BlockState state, Level worldIn, BlockPos pos, Player player, BlockHitResult hit) {
        TileRelay tile = getTileForBlock(worldIn, pos);
        
        if(tile != null) {
            ItemStack handStack = player.getItemInHand(player.getUsedItemHand());

            if(!player.isCrouching() && (handStack.getItem() instanceof IInstrumentItem || handStack.getItem().equals(ModItems.RECEIVER) || handStack.getItem().equals(ModItems.RELAY))) {
                if(!worldIn.isClientSide) {
                    String transmitterName = worldIn.dimension().location().getPath() + "@(" + pos.toShortString() + ")";
                    MidiNbtDataUtils.setMidiSourceFromRelay(handStack, tile.getUUID(), transmitterName);
                    player.setItemInHand(player.getUsedItemHand(), handStack);
                    player.displayClientMessage(Component.literal("Linked to Relay"), true);
                    return InteractionResult.CONSUME;
                }
            } else if(worldIn.isClientSide) {
                ClientGuiWrapper.openRelayGui(worldIn, player, tile.getBlockPos(), tile.getSourceStack());
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.SUCCESS;
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

        // Channel Mappings
        Integer enabledChannels = MidiNbtDataUtils.getEnabledChannelsInt(blockItemStack);
        tooltip.add(Component.literal("  Channels:").withStyle(ChatFormatting.GREEN));

        if(enabledChannels.equals(MidiNbtDataUtils.NONE_CHANNELS_INT)) {
            tooltip.add(Component.literal("    None").withStyle(ChatFormatting.GREEN));
        } else {
            Byte[] channelMap = MidiNbtDataUtils.getChannelMap(blockItemStack);
            Boolean channelRendered = false;
            for(byte i = 0; i < 16; i++) {
                Boolean enabled = MidiNbtDataUtils.isChannelEnabled(enabledChannels, i);
                
                if(!enabled || channelMap[i] != i) {
                    tooltip.add(Component.literal("    " + (i+1) + " (" + (enabled ? "On" : "Off") + ") --> " + (channelMap[i]+1)).withStyle(ChatFormatting.GREEN));
                    channelRendered = true;
                }
            }

            if(!channelRendered) {
                tooltip.add(Component.literal("    Default").withStyle(ChatFormatting.GREEN));
            }
        }

        // Note Source
        if(MidiNbtDataUtils.getMidiSource(blockItemStack) != null) {
            Boolean isTransmitter = MidiNbtDataUtils.getMidiSourceIsTransmitter(blockItemStack);
            Boolean isRelay = MidiNbtDataUtils.getMidiSourceIsRelay(blockItemStack);
            tooltip.add(Component.literal("  Recieve Notes From: " + (isTransmitter ? "Transmitter:" : ( isRelay ? "Relay:" : "Player:"))).withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal("  " + MidiNbtDataUtils.getMidiSourceName(blockItemStack, true)).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.literal("  Recieve Notes From: None").withStyle(ChatFormatting.GREEN));
        }
    }
}