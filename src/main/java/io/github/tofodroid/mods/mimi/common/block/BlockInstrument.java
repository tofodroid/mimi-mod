package io.github.tofodroid.mods.mimi.common.block;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.IWaterLoggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.BlockItemUseContext;
import net.minecraft.state.BooleanProperty;
import net.minecraft.state.DirectionProperty;
import net.minecraft.state.StateContainer;
import net.minecraft.state.properties.BlockStateProperties;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.Mirror;
import net.minecraft.util.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.shapes.ISelectionContext;
import net.minecraft.util.math.shapes.VoxelShape;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

import java.util.Map;

import io.github.tofodroid.mods.mimi.common.entity.EntitySeat;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

public abstract class BlockInstrument extends AContainerBlock<TileInstrument> implements IWaterLoggable {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty DIRECTION = BlockStateProperties.HORIZONTAL_FACING;

    public final Map<Direction, VoxelShape> SHAPES;
    private final Byte instrumentId;

    public BlockInstrument(Properties properties, Byte instrumentId) {
        super(properties);
        this.instrumentId = instrumentId;
        this.setDefaultState(this.getStateContainer().getBaseState()
            .with(WATERLOGGED, false)
            .with(DIRECTION, Direction.NORTH)
        );
        this.SHAPES = this.generateShapes();
    }

    protected abstract Map<Direction, VoxelShape> generateShapes();
    
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        TileInstrument tileInstrument = getTileForBlock(worldIn, pos);
        
        if(tileInstrument != null) {
           if(!worldIn.isRemote) {
                if(tileInstrument.equals(getTileInstrumentForEntity(player))) {
                    NetworkHooks.openGui((ServerPlayerEntity) player, this.getContainer(state, worldIn, pos), buffer -> {
                        buffer.writeByte(this.instrumentId);
                        buffer.writeBoolean(false);
                        buffer.writeBlockPos(pos);
                    });
                } else {
                    return EntitySeat.create(worldIn, pos, this.getSeatOffset(state), player);
                }
            }
        }

        return ActionResultType.SUCCESS;
    }

    @Override
    public TileEntityType<TileInstrument> getTileType() {
        return ModTiles.INSTRUMENT;
    }

    @Override
    public BlockState getStateForPlacement(BlockItemUseContext context) {
        FluidState fluidState = context.getWorld().getFluidState(context.getPos());
        return this.getDefaultState()
            .with(WATERLOGGED, fluidState.getFluid() == Fluids.WATER)
            .with(DIRECTION, context.getPlacementHorizontalFacing().getOpposite());
    }

    @Override
    protected void fillStateContainer(StateContainer.Builder<Block, BlockState> builder) {
        super.fillStateContainer(builder);
        builder.add(DIRECTION);
        builder.add(WATERLOGGED);
    }

    @Override
    public VoxelShape getShape(BlockState state, IBlockReader reader, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.get(DIRECTION));
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return SHAPES.get(state.get(DIRECTION));
    }
    
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.with(DIRECTION, rotation.rotate(state.get(DIRECTION)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.toRotation(state.get(DIRECTION)));
    }

    @Override
	public TileEntity createTileEntity(final BlockState state, final IBlockReader reader) {
		TileInstrument tile = (TileInstrument)super.createNewTileEntity(reader);
        tile.setInstrumentId(instrumentId);
        return tile;
	}

    public Byte getInstrumentId() {
        return instrumentId;
    }

    protected Vector3d getSeatOffset(BlockState state) {
        switch(state.get(DIRECTION)) {
            case NORTH:
                return new Vector3d(0.5, 0, 0.05);
            case SOUTH:
                return new Vector3d(0.5, 0, 0.95);
            case EAST:
                return new Vector3d(0.95, 0, 0.5);
            case WEST:
                return new Vector3d(0.05, 0, 0.5);
            default:
                return new Vector3d(0.5, 0, 0.05);
        }
    }

    public static Boolean isEntitySittingAtInstrument(LivingEntity entity) {
        return entity.isPassenger() && ModEntities.SEAT.equals(entity.getRidingEntity().getType());
    }
    
    public static EntitySeat getSeatForEntity(LivingEntity entity) {
        if(isEntitySittingAtInstrument(entity)) {
            return (EntitySeat) entity.getRidingEntity();
        }

        return null;
    }
    
    public static TileInstrument getTileInstrumentForEntity(LivingEntity entity) {
        if(entity.isAlive() && isEntitySittingAtInstrument(entity)) {
            BlockPos pos = getSeatForEntity(entity).getSource();

            if(pos != null && !World.isOutsideBuildHeight(pos)) {
                TileEntity sourceEntity = entity.getEntityWorld().getTileEntity(getSeatForEntity(entity).getSource());
                return sourceEntity != null && sourceEntity instanceof TileInstrument ? (TileInstrument) sourceEntity : null;
            }
        }

        return null;
    }
}
