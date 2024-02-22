package io.github.tofodroid.mods.mimi.common.block;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.common.entity.EntitySeat;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import io.github.tofodroid.mods.mimi.util.VoxelShapeUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class BlockInstrument extends AContainerBlock<TileInstrument> implements SimpleWaterloggedBlock {
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final DirectionProperty DIRECTION = BlockStateProperties.HORIZONTAL_FACING;

    protected final Map<Direction, VoxelShape> SHAPES;
    protected final Byte instrumentId;
    protected final Integer defaultChannels;
    protected final Integer defaultColor;
    protected final Boolean colorable;
    protected final String REGISTRY_NAME;
 
    public BlockInstrument(Properties props, Byte instrumentId) {
        super(props.explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD).dynamicShape().noOcclusion());
        InstrumentSpec spec = InstrumentConfig.getBydId(instrumentId);
        this.SHAPES = this.generateShapes(VoxelShapeUtils.loadFromStrings(spec.collisionShapes));
        this.REGISTRY_NAME = spec.registryName;
        this.defaultChannels = MidiNbtDataUtils.getDefaultChannelsForBank(spec.midiBankNumber);
        this.instrumentId = instrumentId;
        this.colorable = spec.isColorable();
        this.defaultColor = this.colorable ? spec.defaultColor() : null;
        this.registerDefaultState(this.stateDefinition.any().setValue(DIRECTION, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    public BlockInstrument(Properties props, InstrumentSpec spec) {
        super(props.explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD).dynamicShape().noOcclusion());
        this.SHAPES = this.generateShapes(VoxelShapeUtils.loadFromStrings(spec.collisionShapes));
        this.REGISTRY_NAME = spec.registryName;
        this.defaultChannels = MidiNbtDataUtils.getDefaultChannelsForBank(spec.midiBankNumber);
        this.instrumentId = spec.instrumentId;
        this.colorable = spec.isColorable();
        this.defaultColor = this.colorable ? spec.defaultColor() : null;
        this.registerDefaultState(this.stateDefinition.any().setValue(DIRECTION, Direction.NORTH).setValue(WATERLOGGED, Boolean.valueOf(false)));
    }

    protected Map<Direction, VoxelShape> generateShapes(VoxelShape shape) {
        return VoxelShapeUtils.generateFacingShape(shape);
    }
    
    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        TileInstrument tileInstrument = getTileForBlock(worldIn, pos);
        
        if(tileInstrument != null) {
           if(!worldIn.isClientSide) {
                if(player.getVehicle() == null) {
                    tileInstrument.attemptSit(player);
                }
            } else if(tileInstrument.equals(getTileInstrumentForEntity(player))) {
                ClientGuiWrapper.openInstrumentGui(worldIn, player, null, tileInstrument.getInstrumentStack());
            }
        }

        return InteractionResult.SUCCESS;
    }
    
    @Override
    public boolean propagatesSkylightDown(BlockState state, BlockGetter getter, BlockPos pos) {
        return true;
    }

    @Override
    public BlockEntityType<TileInstrument> getTileType() {
        return ModTiles.INSTRUMENT;
    }
    
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(DIRECTION, WATERLOGGED);
    }
    
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    @SuppressWarnings("deprecation")
    public ItemStack getCloneItemStack(BlockGetter reader, BlockPos pos, BlockState state) {
        TileInstrument tileInstrument = reader.getBlockEntity(pos, ModTiles.INSTRUMENT).orElse(null);
        
        if(tileInstrument != null) {
            return tileInstrument.getInstrumentStack();
        }

        return super.getCloneItemStack(reader, pos, state);
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getHorizontalDirection().getOpposite();
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());

        return this.defaultBlockState().setValue(DIRECTION, direction).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter getter, BlockPos pos, CollisionContext context) {
       return SHAPES.get(state.getValue(DIRECTION));
    }

    @SuppressWarnings("deprecation")
    public FluidState getFluidState(BlockState p_51581_) {
        return p_51581_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_51581_);
    }

    @Override
    public BlockState rotate(BlockState state, LevelAccessor world, BlockPos pos, Rotation rotation)
    {
        return state.setValue(DIRECTION, rotation.rotate(state.getValue(DIRECTION)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public BlockState mirror(BlockState state, Mirror mirror)
    {
        return state.rotate(mirror.getRotation(state.getValue(DIRECTION)));
    }

    @Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		TileInstrument tile = (TileInstrument)super.newBlockEntity(pos, state);
        return tile;
	}

    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        BlockEntity tileEntity = worldIn.getBlockEntity(pos);
        if (tileEntity instanceof TileInstrument) {
            ItemStack newStack = new ItemStack(stack.getItem(), stack.getCount());
            newStack.setTag(stack.getOrCreateTag().copy());
            ((TileInstrument)tileEntity).setInstrumentStack(newStack);
        }
    }
    
    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Arrays.asList();
    }

    public String getRegistryName() {
        return this.REGISTRY_NAME;
    }

    public Boolean isColorable() {
        return this.colorable;
    }

    public Integer getDefaultColor() {
        return this.defaultColor;
    }

    public Byte getInstrumentId() {
        return this.instrumentId;
    }

    public Integer getDefaultChannels() {
        return this.defaultChannels;
    }

    @SuppressWarnings("null")
    public static Boolean isEntitySittingAtInstrument(LivingEntity entity) {
        return entity.isPassenger() && entity.getVehicle() != null && ModEntities.SEAT.equals(entity.getVehicle().getType());
    }
    
    public static EntitySeat getSeatForEntity(LivingEntity entity) {
        if(isEntitySittingAtInstrument(entity)) {
            return (EntitySeat) entity.getVehicle();
        }

        return null;
    }

    public static ItemStack getTileInstrumentStackForEntity(LivingEntity entity) {
        TileInstrument tile = getTileInstrumentForEntity(entity);

        if(tile != null) {
            return tile.getInstrumentStack();
        }
        return null;
    }
    
    public static TileInstrument getTileInstrumentForEntity(LivingEntity entity) {
        if(entity.isAlive() && isEntitySittingAtInstrument(entity)) {
            BlockPos pos = getSeatForEntity(entity).getSource();

            if(pos != null && Level.isInSpawnableBounds(pos)) {
                BlockEntity sourceEntity = entity.level().getBlockEntity(getSeatForEntity(entity).getSource());
                return sourceEntity != null && sourceEntity instanceof TileInstrument ? (TileInstrument) sourceEntity : null;
            }
        }

        return null;
    }
}
