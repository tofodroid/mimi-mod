package io.github.tofodroid.mods.mimi.common.block;

import java.util.List;

import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileConductor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;

public class BlockConductor extends AConfigurableTileBlock<TileConductor> {
   public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    public static final String REGISTRY_NAME = "conductor";

    public BlockConductor() {
        super(Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
        this.registerDefaultState(this.defaultBlockState().setValue(TRIGGERED, Boolean.valueOf(false)));
   }

   @Nullable
   public BlockState getStateForPlacement(BlockPlaceContext p_55659_) {
        return this.defaultBlockState().setValue(TRIGGERED, Boolean.valueOf(p_55659_.getLevel().hasNeighborSignal(p_55659_.getClickedPos())));
   }
   
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!worldIn.isClientSide) {
            if (!state.hasBlockEntity())
                return;

            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            
            if (blockEntity instanceof TileConductor) {
                ((TileConductor)blockEntity).stopNote();
            }
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    public void neighborChanged(BlockState blockState, Level level, BlockPos blockPos, Block neighborBlock, BlockPos neighborPos, boolean flag1) {
        if (!level.isClientSide) {
            boolean wasTriggered = blockState.getValue(TRIGGERED);

            if (wasTriggered != level.hasNeighborSignal(blockPos)) {
                if(wasTriggered) {
                    getTileForBlock(level, blockPos).stopNote();
                } else {
                    getTileForBlock(level, blockPos).startNote();
                    level.scheduleTick(blockPos, this, 4);
                }
                level.setBlock(blockPos, blockState.cycle(TRIGGERED), 2);
            }

        }
    }

    @Override
    public void tick(BlockState blockState, ServerLevel level, BlockPos blockPos, RandomSource random) {
        // if (blockState.getValue(TRIGGERED)) {
        //     if(level.hasNeighborSignal(blockPos)) {
        //         getTileForBlock(level, blockPos).startNote();
        //         level.scheduleTick(blockPos, this, 4);
        //     } else {
        //         getTileForBlock(level, blockPos).stopNote();
        //         level.setBlock(blockPos, blockState.cycle(TRIGGERED), 2);
        //     }
        // }
    }

   @Override
   protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
      builder.add(TRIGGERED);
   }

    @Override
    protected void openGui(Level worldIn, Player player, TileConductor tile) {
        //ClientGuiWrapper.openConductorGui(worldIn, tile.getBlockPos(), tile.getSourceStack());

        player.sendSystemMessage(Component.translatableWithFallback("block.mimi.conductor.wip", "Coming soon!"));
    }

    @Override
    public BlockEntityType<TileConductor> getTileType() {
        return ModTiles.CONDUCTOR;
    }

    @Override
    protected void appendSettingsTooltip(ItemStack blockItemStack, List<Component> tooltip) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.translatableWithFallback("block.mimi.conductor.wip", "Coming soon!"));

        // tooltip.add(Component.literal("MIDI Settings:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        // Integer enabledChannels = MidiNbtDataUtils.getEnabledChannelsInt(blockItemStack);
        // if(enabledChannels != null) {
        //     if(enabledChannels.equals(MidiNbtDataUtils.ALL_CHANNELS_INT)) {
        //         tooltip.add(Component.literal("  Channels: All").withStyle(ChatFormatting.GREEN));
        //     } else if(enabledChannels.equals(MidiNbtDataUtils.NONE_CHANNELS_INT)) {
        //         tooltip.add(Component.literal("  Channels: None").withStyle(ChatFormatting.GREEN));
        //     } else {
        //         tooltip.add(Component.literal("  Channels: " + MidiNbtDataUtils.getEnabledChannelsAsString(enabledChannels)).withStyle(ChatFormatting.GREEN));
        //     }
        // }

        // // Transmit Note
        // tooltip.add(Component.literal("  Transmit Note: " + MidiNbtDataUtils.getFilteredNotesAsString(blockItemStack)).withStyle(ChatFormatting.GREEN));
    }
}