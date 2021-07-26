package io.github.tofodroid.mods.mimi.common.block;

import java.util.Random;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.data.CommonDataUtil;
import io.github.tofodroid.mods.mimi.common.data.ItemInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import io.github.tofodroid.mods.mimi.util.PlayerNameUtils;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.block.SoundType;
import net.minecraft.state.IntegerProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.network.NetworkHooks;

public class BlockReceiver extends ContainerBlock   {
    private static final IntegerProperty POWER = BlockStateProperties.POWER_0_15;

    public BlockReceiver() {
        super(Properties.create(Material.WOOD).hardnessAndResistance(2.f, 6.f).sound(SoundType.WOOD));
        this.setDefaultState(this.stateContainer.getBaseState().with(POWER, Integer.valueOf(0)));
        this.setRegistryName("receiver");
    }

    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {   
        TileReceiver tileReceiver = getTileReceiverForBlock(worldIn, pos);
        if(tileReceiver != null) {
            /* CONTAINER
            INamedContainerProvider namedContainerProvider = this.getContainer(state, worldIn, pos);
            if (namedContainerProvider != null) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
                NetworkHooks.openGui(serverPlayerEntity, namedContainerProvider, (packetBuffer -> {
                    //write the chest pos here !
                    packetBuffer.writeBlockPos(pos);
                }));
            }
            */
            UUID instrumentSource = ItemInstrumentDataUtil.INSTANCE.getMidiSource(ItemInstrument.getEntityHeldInstrumentStack(player, hand));

            // Server-Side: If right clicked with instrument and not currently being used then set maestro, otherwise sit
            if(instrumentSource != null && !CommonDataUtil.SYS_SOURCE_ID.equals(instrumentSource) && !CommonDataUtil.PUBLIC_SOURCE_ID.equals(instrumentSource)) {
                if(!worldIn.isRemote) {
                    tileReceiver.setMidiSource(instrumentSource);
                    tileReceiver.markDirty();
                    ((ServerWorld)worldIn).getChunkProvider().markBlockChanged(pos);
                } else {
                    String instrumentMaestroName = PlayerNameUtils.getPlayerNameFromUUID(instrumentSource, worldIn);
                    player.sendStatusMessage(new StringTextComponent("Linked " + this.getTranslatedName().getString() + " MIDI Source: " +  instrumentMaestroName), true);
                }
            } else if(worldIn.isRemote) {
                MIMIMod.guiWrapper.openReceiverBlockGui(worldIn, player, tileReceiver);
            }
        }

        return ActionResultType.SUCCESS;
    }
    
    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        return this.getDefaultState().with(POWER, Integer.valueOf(0));
    }
    
    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        builder.add(POWER);
    }
    
    public int getWeakPower(BlockState blockState, IBlockReader blockAccess, BlockPos pos, Direction side) {
        return blockState.get(POWER);
    }
    
    public boolean canProvidePower(BlockState state) {
        return true;
    }

    @Override
    public void tick(BlockState state, ServerWorld worldIn, BlockPos pos, Random rand) {
        if (state.get(POWER) != 0) {
           worldIn.setBlockState(pos, state.with(POWER, Integer.valueOf(0)), 3);
        }
    }

    public void powerTarget(IWorld world, BlockState state, int power, BlockPos pos, int waitTime) {
        if (!world.getPendingBlockTicks().isTickScheduled(pos, state.getBlock())) {
            world.setBlockState(pos, state.with(POWER, Integer.valueOf(power)), 3);
            world.getPendingBlockTicks().scheduleTick(pos, state.getBlock(), waitTime);
        }
    }

    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }
    
    @Override
	public TileEntity createTileEntity(final BlockState state, final IBlockReader reader) {
        return createNewTileEntity(reader);
	}

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return ModTiles.RECEIVER.create();
    }

    public static TileReceiver getTileReceiverForBlock(World worldIn, BlockPos pos) {
        TileEntity entity = worldIn.getTileEntity(pos);
        return entity != null && entity instanceof TileReceiver ? (TileReceiver)entity : null;
    }
}
