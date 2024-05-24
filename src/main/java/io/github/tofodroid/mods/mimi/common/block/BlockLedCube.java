package io.github.tofodroid.mods.mimi.common.block;

import java.util.List;

import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

public class BlockLedCube extends AColoredBlock {
   public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
   public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
   
   public static final String REGISTRY_NAME_A = "ledcube_a";
   public static final String REGISTRY_NAME_B = "ledcube_b";
   public static final String REGISTRY_NAME_C = "ledcube_c";
   public static final String REGISTRY_NAME_D = "ledcube_d";
   public static final String REGISTRY_NAME_E = "ledcube_e";
   public static final String REGISTRY_NAME_F = "ledcube_f";
   public static final String REGISTRY_NAME_G = "ledcube_g";
   public static final String REGISTRY_NAME_H = "ledcube_h";
   
   public BlockLedCube() {
      super(
         Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.LANTERN).hasPostProcess((a,b,c) -> true).emissiveRendering((a,b,c) -> {
            return BlockLedCube.isLit(a);
         })
      );
      this.registerDefaultState(this.defaultBlockState().setValue(POWERED, false).setValue(INVERTED, false).setValue(DYE_ID, 0));
   }

   public static Boolean isLit(BlockState state) {
      return state.getValue(INVERTED) != state.getValue(POWERED);
   }

   @Override
   public float getShadeBrightness(BlockState state, BlockGetter p_153690_, BlockPos p_153691_) {
      return state.getValue(BlockLedCube.POWERED) ? 1.0f : super.getShadeBrightness(state, p_153690_, p_153691_);
   }

   @Override
   public ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      if(Items.REDSTONE_TORCH.equals(player.getItemInHand(handIn).getItem()) || Items.REDSTONE_BLOCK.equals(player.getItemInHand(handIn).getItem())) {
         if(!worldIn.isClientSide) {
            worldIn.setBlock(pos, state.cycle(INVERTED), 2);
            worldIn.playSound(null, pos, SoundEvents.LANTERN_HIT, SoundSource.BLOCKS);
         }
         return ItemInteractionResult.CONSUME;
      } else if(player.getItemInHand(handIn).isEmpty()) {
         if(!worldIn.isClientSide) {
            Integer dyeId = state.getValue(DYE_ID);
            worldIn.setBlock(pos, state.setValue(DYE_ID, dyeId < 15 ? (dyeId + 1) : 0), 2);
            worldIn.playSound(null, pos, SoundEvents.NOTE_BLOCK_BELL.get(), SoundSource.BLOCKS);
         }
         return ItemInteractionResult.CONSUME;
      }
   
      return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
   }

   @Override
   public int getLightBlock(BlockState state, BlockGetter p_60586_, BlockPos p_60587_) {
      return BlockLedCube.isLit(state) ? BlockStateProperties.MAX_LEVEL_15 : super.getLightBlock(state, p_60586_, p_60587_);
   }

   @Override
   public BlockState getStateForPlacement(BlockPlaceContext context) {
      return super.getStateForPlacement(context)
         .setValue(POWERED, Boolean.valueOf(context.getLevel().hasNeighborSignal(context.getClickedPos())))
         .setValue(INVERTED, TagUtils.getBooleanOrDefault(context.getItemInHand(), INVERTED.getName(), false))
      ;
   }

   @Override
   protected void appendSettingsTooltip(ItemStack blockItemStack, List<Component> tooltip) {
      super.appendSettingsTooltip(blockItemStack, tooltip);
      tooltip.add(Component.literal("Inverted:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
      tooltip.add(Component.literal("  " + (TagUtils.getBooleanOrDefault(blockItemStack, INVERTED.getName(), false) ? "Yes" : "No")).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
   }
   
   @Override
   public ItemStack getCloneItemStack(LevelReader reader, BlockPos pos, BlockState state) {
      ItemStack itemstack = super.getCloneItemStack(reader, pos, state);
      itemstack.set(TagUtils.getBoolComponent(INVERTED.getName()), state.getOptionalValue(INVERTED).orElse(false));
      return itemstack;
   }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
      state.add(POWERED);
      state.add(INVERTED);
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