package io.github.tofodroid.mods.mimi.common.block;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import io.github.tofodroid.mods.mimi.server.events.broadcast.producer.transmitter.ServerTransmitterManager;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;

public class BlockTransmitter extends AContainerBlock<TileTransmitter> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final String REGISTRY_NAME = "transmitterblock";

    public BlockTransmitter(Properties props) {
        super(props.explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        TileTransmitter tile = getTileForBlock(worldIn, pos);
        
        if(tile != null) {
            ItemStack handStack = player.getItemInHand(hand);

            if(!player.isCrouching() && (handStack.getItem() instanceof IInstrumentItem || handStack.getItem().equals(ModItems.RECEIVER) || handStack.getItem().equals(ModItems.RELAY))) {
                if(!worldIn.isClientSide) {
                    String transmitterName = worldIn.dimension().location().getPath() + "@(" + pos.toShortString() + ")";
                    MidiNbtDataUtils.setMidiSourceFromTransmitter(handStack, tile.getUUID(), transmitterName);
                    player.setItemInHand(hand, handStack);
                    player.displayClientMessage(Component.literal("Linked to Transmitter"), true);
                }
            } else if(worldIn.isClientSide) {
                ClientGuiWrapper.openTransmitterBlockGui(worldIn, tile.getUUID());
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(POWERED);
    }
    
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
            return; 

        TileTransmitter tileEntity = getTileForBlock(worldIn, pos);

        if(!worldIn.isClientSide && tileEntity != null) {
            BroadcastManager.removeBroadcastProducer(tileEntity.getUUID());
        }
        
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
    
    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        TileTransmitter tileEntity = getTileForBlock(worldIn, pos);
        if (tileEntity != null) {
            ItemStack newStack = new ItemStack(stack.getItem(), 1);
            newStack.setTag(stack.getOrCreateTag().copy());
            tileEntity.setSourceStack(newStack);
            
            if(!worldIn.isClientSide) {
                ServerTransmitterManager.createTransmitter(tileEntity);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_60537_, LootContext.Builder p_60538_) {
        return Arrays.asList();
    }

    @Override
    public BlockEntityType<TileTransmitter> getTileType() {
        return ModTiles.TRANSMITTER;
    }
    
    @Override
    public int getSignal(BlockState state, BlockGetter getter, BlockPos pos, Direction direction) {
        return state.getValue(POWERED) ? 15 : 0;
    }

    @Override
    public boolean isSignalSource(BlockState p_55730_) {
        return true;
    }
}
