package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileBroadcaster;
import io.github.tofodroid.mods.mimi.server.midi.ServerMusicPlayerMidiManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class BlockBroadcaster extends AContainerBlock<TileBroadcaster> {
    public static final IntegerProperty POWER = BlockStateProperties.POWER;
    public static final String REGISTRY_NAME = "broadcaster";

    public BlockBroadcaster() {
        super(Properties.of(Material.METAL).explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
        this.registerDefaultState(this.stateDefinition.any().setValue(POWER, Integer.valueOf(0)));
    }
    
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!worldIn.isClientSide && worldIn instanceof ServerLevel) {
            if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
                return;
            TileBroadcaster tile = getTileForBlock(worldIn, pos);
            ServerMusicPlayerMidiManager.removeMusicPlayer(tile);
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
    
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!worldIn.isClientSide) {
            TileBroadcaster tile = getTileForBlock(worldIn, pos);

            if(tile != null) {
               ItemStack stack = player.getItemInHand(hand);

                if(ModItems.SWITCHBOARD.equals(stack.getItem())) {
                    ItemMidiSwitchboard.setMidiSource(stack, tile.getMusicPlayerId(), "Broad. " + pos.toShortString());
                    player.setItemInHand(hand, stack);
                    player.displayClientMessage(Component.literal("Set MIDI Source to Broadcaster at: " + pos.toShortString()), true);
                    return InteractionResult.CONSUME;
                }
            }
        }

        return super.use(state, worldIn, pos, player, hand, hit);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModTiles.BROADCASTER, TileBroadcaster::doTick);
    }
    
    @Override
    public BlockEntityType<TileBroadcaster> getTileType() {
        return ModTiles.BROADCASTER;
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWER, Integer.valueOf(0));
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }
    
    @Override
    public int getSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction direction) {
        return state.getValue(POWER);
    }
    
    @Override
    public boolean isSignalSource(BlockState p_55730_) {
        return true;
    }
}