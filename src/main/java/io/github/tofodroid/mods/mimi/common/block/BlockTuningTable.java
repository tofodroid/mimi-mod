package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.container.ContainerTuningTable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.phys.BlockHitResult;

public class BlockTuningTable extends Block {
   protected BlockTuningTable() {
      super(Properties.of(Material.METAL).explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
      this.setRegistryName("tuningtable");
   }

   public InteractionResult onBlockActivated(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand handIn, BlockHitResult hit) {
      if (worldIn.isClientSide) {
         return InteractionResult.SUCCESS;
      } else {
         player.openMenu(state.getMenuProvider(worldIn, pos));
         return InteractionResult.CONSUME;
      }
   }

   /*
   public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
      return new SimpleNamedContainerProvider((id, inventory, player) -> {
         return new ContainerTuningTable(id, inventory, null);
      },  new TranslationTextComponent("container.tuningtable"));
   }
   */
}