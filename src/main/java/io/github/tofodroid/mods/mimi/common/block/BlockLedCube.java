package io.github.tofodroid.mods.mimi.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BlockLedCube extends AColoredBlock {
   public static final String REGISTRY_NAME_A = "ledcube_a";
   public static final String REGISTRY_NAME_B = "ledcube_b";
   public static final String REGISTRY_NAME_C = "ledcube_c";
   public static final String REGISTRY_NAME_D = "ledcube_d";
   public static final String REGISTRY_NAME_E = "ledcube_e";
   public static final String REGISTRY_NAME_F = "ledcube_f";
   public static final String REGISTRY_NAME_G = "ledcube_g";
   public static final String REGISTRY_NAME_H = "ledcube_h";
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   
   public BlockLedCube() {
      super(
         Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.LANTERN).hasPostProcess((a,b,c) -> true).emissiveRendering((a,b,c) -> {
            return a.getValue(BlockLedCube.POWERED);
         })
      );
      this.registerDefaultState(this.defaultBlockState().setValue(POWERED, Boolean.valueOf(false)).setValue(DYE_ID, 0));
   }

   @Override
   @SuppressWarnings("deprecation")
   public float getShadeBrightness(BlockState state, BlockGetter p_153690_, BlockPos p_153691_) {
      return state.getValue(BlockLedCube.POWERED) ? 1.0f : super.getShadeBrightness(state, p_153690_, p_153691_);
   }

   @Override
   public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      if(player.isCrouching()) {
         if (worldIn.isClientSide) {
            worldIn.playLocalSound(pos, SoundEvents.END_PORTAL_FRAME_FILL, SoundSource.BLOCKS, 1.0f, 1.0f, false);
            return InteractionResult.SUCCESS;
         } else {
            Integer dyeId = state.getValue(DYE_ID);
            worldIn.setBlock(pos, state.setValue(DYE_ID, dyeId >= 15 ? 0 : dyeId + 1), 2);
            return InteractionResult.CONSUME;
         }
      }
      return InteractionResult.PASS;
   }

   @Override
   @SuppressWarnings("deprecation")
   public int getLightBlock(BlockState state, BlockGetter p_60586_, BlockPos p_60587_) {
      return state.getValue(BlockLedCube.POWERED) ? BlockStateProperties.MAX_LEVEL_15 : super.getLightBlock(state, p_60586_, p_60587_);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return super.getStateForPlacement(context).setValue(POWERED, Boolean.valueOf(context.getLevel().hasNeighborSignal(context.getClickedPos())));
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
      state.add(POWERED);
      state.add(DYE_ID);
   }
   
   @Override
   public void tick(BlockState state, ServerLevel worldIn, BlockPos pos, RandomSource random) {
      if (state.getValue(POWERED) && !worldIn.hasNeighborSignal(pos)) {
         worldIn.setBlock(pos, state.cycle(POWERED), 2);
      }
   }

   @Override
   public boolean isSignalSource(BlockState p_60571_) {
      return false;
   }

   @Override
   public int getSignal(BlockState p_60483_, BlockGetter p_60484_, BlockPos p_60485_, Direction p_60486_) {
      return 0;
   }

   @Override
   public boolean hasAnalogOutputSignal(BlockState p_60457_) {
      return false;
   }

   @Override
   public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
      if(!worldIn.isClientSide) {
         Boolean wasPowered = state.getValue(POWERED);
         
         if(wasPowered != worldIn.hasNeighborSignal(pos)) {
            if(wasPowered) {
               worldIn.scheduleTick(pos, this, 1);
            } else {
               worldIn.setBlock(pos, state.cycle(POWERED), 2);
            }
         }
      }
   }
}