package io.github.tofodroid.mods.mimi.common.block;

import java.util.Random;

import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileConductor;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.ticks.ScheduledTick;

public class BlockConductor extends AContainerBlock<TileConductor> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public BlockConductor() {
        super(Properties.of(Material.METAL).explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
        this.registerDefaultState(this.stateDefinition.any().setValue(POWERED, Boolean.valueOf(false)));
        this.setRegistryName("conductor");
    }
    
    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if(!worldIn.isClientSide) {
            boolean flag = worldIn.hasNeighborSignal(pos);
            if (flag != state.getValue(POWERED)) {
                if (flag) {
                    TileConductor tile = getTileForBlock(worldIn, pos);
                    
                    if(tile != null) {
                        if (!worldIn.getBlockTicks().hasScheduledTick(pos, state.getBlock())) {
                            tile.transmitNoteOn(worldIn);
                            worldIn.getBlockTicks().schedule(new ScheduledTick<Block>(this, pos, 8, 0));
                        }
                    }
                }
                worldIn.setBlock(pos, state.setValue(POWERED, Boolean.valueOf(flag)), 3);
            }
        }
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(POWERED);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(POWERED, false);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        TileConductor tile = getTileForBlock(worldIn, pos);
        
        if(tile != null) {
           if(!worldIn.isClientSide) {
               ItemStack stack = player.getItemInHand(hand);

                if(ModItems.SWITCHBOARD.equals(stack.getItem())) {
                    ItemMidiSwitchboard.setMidiSource(stack, tile.getUniqueId(), "Cond. " + pos.toShortString());
                    player.setItemInHand(hand, stack);
                    player.displayClientMessage(new TextComponent("Set MIDI Source to Conductor at: " + pos.toShortString()), true);
                    return InteractionResult.CONSUME;
                }
            }
        }

        return super.use(state, worldIn, pos, player, hand, hit);
    }
    
    @Override
    public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, Random rand) {
        TileConductor tile = getTileForBlock(worldIn, pos);
                    
        if(tile != null) {
            tile.transmitNoteOff(worldIn);
        }
    }
    
    @Override
    public BlockEntityType<TileConductor> getTileType() {
        return ModTiles.CONDUCTOR;
    }
}