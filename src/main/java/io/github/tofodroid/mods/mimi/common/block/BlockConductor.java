package io.github.tofodroid.mods.mimi.common.block;

import java.util.Random;

import io.github.tofodroid.mods.mimi.common.item.ItemMidiSwitchboard;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileConductor;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.SoundType;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;

public class BlockConductor extends AContainerBlock<TileConductor> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;

    public BlockConductor() {
        super(Properties.create(Material.IRON).hardnessAndResistance(2.f, 6.f).sound(SoundType.WOOD));
        this.setDefaultState(this.stateContainer.getBaseState().with(POWERED, false));
        this.setRegistryName("conductor");
    }
    
    @Override
    public void neighborChanged(BlockState state, World worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if(!worldIn.isRemote) {
            boolean flag = worldIn.isBlockPowered(pos);
            if (flag != state.get(POWERED)) {
                if (flag) {
                    TileConductor tile = getTileForBlock(worldIn, pos);
                    
                    if(tile != null) {
                        if (!worldIn.getPendingBlockTicks().isTickScheduled(pos, state.getBlock())) {
                            tile.transmitNoteOn(worldIn);
                            worldIn.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), 8);
                        }
                    }
                }
                worldIn.setBlockState(pos, state.with(POWERED, Boolean.valueOf(flag)), 3);
            }
        }
    }
    
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileConductor tile = getTileForBlock(worldIn, pos);
        
        if(tile != null) {
           if(!worldIn.isRemote) {
               ItemStack stack = player.getHeldItem(hand);

                if(ModItems.SWITCHBOARD.equals(stack.getItem())) {
                    ItemMidiSwitchboard.setMidiSource(stack, tile.getUniqueId(), "Cond. " + pos.getCoordinatesAsString());
                    player.setHeldItem(hand, stack);
                    player.sendStatusMessage(new StringTextComponent("Set MIDI Source to Conductor at: " + pos.getCoordinatesAsString()), true);
                    return ActionResultType.CONSUME;
                }
            }
        }

        return super.onBlockActivated(state, worldIn, pos, player, hand, hit);
    }
    
    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        TileConductor tile = getTileForBlock(worldIn, pos);
                    
        if(tile != null) {
            tile.transmitNoteOff(worldIn);
        }
    }
    
    @Override
    public TileEntityType<TileConductor> getTileType() {
        return ModTiles.CONDUCTOR;
    }
    
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWERED);
    }
}