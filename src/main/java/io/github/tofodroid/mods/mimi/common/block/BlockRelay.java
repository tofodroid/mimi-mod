package io.github.tofodroid.mods.mimi.common.block;

import java.util.List;

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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
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
    public OpenGuiWrapper openGuiWrapper() {
        return ClientGuiWrapper::openRelayGui;
    }

    @Override
    public BlockEntityType<TileRelay> getTileType() {
        return ModTiles.RELAY;
    }
    
    @Override
    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        ItemStack stack = player.getItemInHand(hand);
        TileRelay tile = getTileForBlock(level, pos);
        
        if(stack.getItem() instanceof IInstrumentItem || stack.getItem().equals(ModItems.RECEIVER) || stack.getItem().equals(ModItems.RELAY) || stack.getItem().equals(ModItems.SOURCELINKER)) {
            if(tile != null && player.isCrouching()) {
                // Server: Link | Client: Don't open GUI
                if(!level.isClientSide) {
                    String transmitterName = level.dimension().location().getPath() + "@(" + pos.toShortString() + ")";
                    MidiNbtDataUtils.setMidiSourceFromRelay(stack, tile.getUUID(), transmitterName);
                    player.setItemInHand(player.getUsedItemHand(), stack);
                    Component message = Component.literal("Linked ").append(stack.getHoverName()).append(Component.literal(" to ")).append(this.getName());
                    player.displayClientMessage(message, true);
                }
                return InteractionResult.SUCCESS;
            }
        }

        if(tile != null && !shouldSkipUse(state, level, pos, player, hand) && level.isClientSide) {
            ClientGuiWrapper.openRelayGui(level, player, pos, null, tile.getSourceStack());
            return InteractionResult.SUCCESS;
        }

        return super.use(state, level, pos, player, hand, hit);
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
    protected Boolean shouldSkipUse(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand) {
        Item useItem = player.getItemInHand(hand).getItem();
        return useItem.equals(ModItems.SETTINGSSYNC) || useItem.equals(ModItems.SOURCELINKER);
    }

    @Override
    protected void appendSettingsTooltip(ItemStack blockItemStack, List<Component> tooltip) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("MIDI Settings:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        // Channel Mappings
        MidiNbtDataUtils.appendMidiChannelMappingsTooltip(blockItemStack, tooltip);
        MidiNbtDataUtils.appendMidiSourceTooltip(blockItemStack, tooltip);
    }
}