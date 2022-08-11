package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileConductor;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;

public class BlockConductor extends AContainerBlock<TileConductor> {
    public static final String REGISTRY_NAME = "conductor";

    public BlockConductor() {
        super(Properties.of(Material.METAL).explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
    }
    
    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if(!worldIn.isClientSide) {
            TileConductor tile = getTileForBlock(worldIn, pos);
            
            if(tile != null) {
                if(worldIn.hasNeighborSignal(pos)) {
                    tile.transmitNoteOn(worldIn);
                } else {
                    tile.transmitNoteOff(worldIn);
                }
            }
        }
    }
    
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!worldIn.isClientSide) {
            if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
                return;
            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            
            if (blockEntity instanceof TileConductor) {
                ((TileConductor)blockEntity).transmitNoteOff(worldIn);
            }
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        if(!worldIn.isClientSide) {
            TileConductor tile = getTileForBlock(worldIn, pos);

            if(tile != null) {
               ItemStack stack = player.getItemInHand(hand);

                if(ModItems.SWITCHBOARD.equals(stack.getItem())) {
                    ItemMidiSwitchboard.setMidiSource(stack, tile.getUniqueId(), "Cond. " + pos.toShortString());
                    player.setItemInHand(hand, stack);
                    player.displayClientMessage(Component.literal("Set MIDI Source to Conductor at: " + pos.toShortString()), true);
                    return InteractionResult.CONSUME;
                }
            }
        }

        return super.use(state, worldIn, pos, player, hand, hit);
    }

    @Override
    public BlockEntityType<TileConductor> getTileType() {
        return ModTiles.CONDUCTOR;
    }
}