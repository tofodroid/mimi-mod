package io.github.tofodroid.mods.mimi.common.block;

import java.util.List;
import java.util.Map;

import com.mojang.math.Vector3d;

import io.github.tofodroid.mods.mimi.common.entity.EntitySeat;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.item.IDyeableInstrumentItem;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.network.NetworkHooks;

public class BlockInstrument extends AContainerBlock<TileInstrument> {
    public static final ResourceLocation CONTENTS = new ResourceLocation("contents");

    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty DIRECTION = BlockStateProperties.HORIZONTAL_FACING;

    protected final Map<Direction, VoxelShape> SHAPES;
    protected final Byte instrumentId;
    protected final Boolean dyeable;
    protected final Integer defaultColor;

    public BlockInstrument(Byte instrumentId, String registryName, Boolean dyeable, Integer defaultColor, VoxelShape collisionShape) {
        super(Properties.of(Material.WOOD).explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
        this.instrumentId = instrumentId;
        this.dyeable = dyeable;
        this.defaultColor = defaultColor;
        this.registerDefaultState(this.stateDefinition.any().setValue(DIRECTION, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
        this.SHAPES = this.generateShapes(collisionShape);
        this.setRegistryName(registryName);
    }

    protected Map<Direction, VoxelShape> generateShapes(VoxelShape shape) {
        return VoxelShapeUtils.generateFacingShape(shape);
    }
    
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        TileInstrument tileInstrument = getTileForBlock(worldIn, pos);
        
        if(tileInstrument != null) {
           if(!worldIn.isClientSide) {
                if(tileInstrument.equals(getTileInstrumentForEntity(player))) {
                    NetworkHooks.openGui((ServerPlayer) player, this.getMenuProvider(state, worldIn, pos), buffer -> {
                        buffer.writeByte(this.instrumentId);
                        buffer.writeBoolean(false);
                        buffer.writeBlockPos(pos);
                    });
                } else {
                    return EntitySeat.create(worldIn, pos, this.getSeatOffset(state), player);
                }
            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public BlockEntityType<TileInstrument> getTileType() {
        return ModTiles.INSTRUMENT;
    }

    /*    
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter reader, BlockPos pos, ISelectionContext context) {
        return SHAPES.get(state.get(DIRECTION));
    }

    @Override
    public VoxelShape getRenderShape(BlockState state, IBlockReader reader, BlockPos pos) {
        return SHAPES.get(state.get(DIRECTION));
    }
    */
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(DIRECTION, WATERLOGGED);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(DIRECTION, Direction.NORTH).setValue(WATERLOGGED, false);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
       return SHAPES.get(state.getValue(DIRECTION));
    }
     
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(DIRECTION, rotation.rotate(state.getValue(DIRECTION)));
    }

    /*
    @Override
    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getStillFluidState(false) : super.getFluidState(state);
    }
    */

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(DIRECTION)));
    }

    @Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		TileInstrument tile = (TileInstrument)super.newBlockEntity(pos, state);
        return tile;
	}

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        if(IDyeableInstrumentItem.isDyeableInstrument(stack) && ((IDyeableInstrumentItem)stack.getItem()).hasColor(stack)) {
            BlockEntity tileentity = worldIn.getBlockEntity(pos);
            if (tileentity instanceof TileInstrument) {
                ((TileInstrument)tileentity).setColor(((IDyeableInstrumentItem)stack.getItem()).getColor(stack));
            }
        }
    }
    
    @Override
    @SuppressWarnings("deprecation")
    public List<ItemStack> getDrops(BlockState state, LootContext.Builder builder) {
        List<ItemStack> drops = super.getDrops(state, builder);
        BlockEntity tileentity = builder.getParameter(LootContextParams.BLOCK_ENTITY);

        if(tileentity != null && tileentity instanceof TileInstrument && ((TileInstrument)tileentity).hasColor()) {
            for(ItemStack stack : drops) {
                if(this.isDyeable() && stack.getItem() instanceof IDyeableInstrumentItem) {
                    ((IDyeableInstrumentItem)stack.getItem()).setColor(stack, ((TileInstrument)tileentity).getColor());
                }
            }
        }

        return drops;
    }

    public Boolean isDyeable() {
        return this.dyeable;
    }

    public Integer getDefaultColor() {
        return this.defaultColor;
    }

    public Byte getInstrumentId() {
        return instrumentId;
    }

    protected Vector3d getSeatOffset(BlockState state) {
        switch(state.getValue(DIRECTION)) {
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
        return entity.isPassenger() && ModEntities.SEAT.equals(entity.getVehicle().getType());
    }
    
    public static EntitySeat getSeatForEntity(LivingEntity entity) {
        if(isEntitySittingAtInstrument(entity)) {
            return (EntitySeat) entity.getVehicle();
        }

        return null;
    }
    
    public static TileInstrument getTileInstrumentForEntity(LivingEntity entity) {
        if(entity.isAlive() && isEntitySittingAtInstrument(entity)) {
            BlockPos pos = getSeatForEntity(entity).getSource();

            if(pos != null && Level.isInSpawnableBounds(pos)) {
                BlockEntity sourceEntity = entity.getLevel().getBlockEntity(getSeatForEntity(entity).getSource());
                return sourceEntity != null && sourceEntity instanceof TileInstrument ? (TileInstrument) sourceEntity : null;
            }
        }

        return null;
    }
}
