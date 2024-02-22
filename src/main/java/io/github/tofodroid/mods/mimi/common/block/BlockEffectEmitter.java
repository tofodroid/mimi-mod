package io.github.tofodroid.mods.mimi.common.block;

import java.util.List;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileEffectEmitter;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

public class BlockEffectEmitter extends AConfigurableTileBlock<TileEffectEmitter> {
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty INVERTED = BlockStateProperties.INVERTED;
    public static final String REGISTRY_NAME = "effectemitter";

    public BlockEffectEmitter(Properties props) {
        super(props.explosionResistance(6.f).strength(2.f).sound(SoundType.COPPER));
        this.registerDefaultState(this.defaultBlockState().setValue(POWERED,false).setValue(INVERTED,false));
    }
    
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
            .setValue(INVERTED, TagUtils.getBooleanOrDefault(context.getItemInHand(), TileEffectEmitter.INVERTED_TAG, false))
            .setValue(POWERED, Boolean.valueOf(context.getLevel().hasNeighborSignal(context.getClickedPos())));
    }
        
    @Override
    public BlockEntityType<TileEffectEmitter> getTileType() {
        return ModTiles.EFFECTEMITTER;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> state) {
        state.add(POWERED).add(INVERTED);
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, getTileType(), TileEffectEmitter::doTick);
    }

    @Override
    public void neighborChanged(BlockState state, Level worldIn, BlockPos pos, Block blockIn, BlockPos fromPos, boolean isMoving) {
        if(!worldIn.isClientSide) {
            Boolean wasPowered = state.getValue(POWERED);
            
            if(wasPowered != worldIn.hasNeighborSignal(pos)) {
                worldIn.setBlock(pos, state.cycle(POWERED), 2);
            }
        }
    }

    @Override
    protected void openGui(Level worldIn, Player player, TileEffectEmitter tile) {
        ClientGuiWrapper.openEffectEmitterGui(worldIn, tile.getBlockPos(), tile.getSourceStack());
    }

    @Override
    protected void appendSettingsTooltip(ItemStack blockItemStack, List<Component> tooltip) {
        tooltip.add(Component.literal("Inverted:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        tooltip.add(Component.literal("  " + (TagUtils.getBooleanOrDefault(blockItemStack, INVERTED.getName(), false) ? "Yes" : "No")).withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));

        // Sound
        tooltip.add(Component.literal("Sound:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        String soundResource = TagUtils.getStringOrDefault(blockItemStack, TileEffectEmitter.SOUND_ID_TAG, "");
        if(soundResource.isBlank()) {
            tooltip.add(Component.literal("  None").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.literal("  ID: " + soundResource).withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal("  Volume: " + TagUtils.getByteOrDefault(blockItemStack, TileEffectEmitter.VOLUME_TAG, 5)).withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal("  Pitch: " + TagUtils.getByteOrDefault(blockItemStack, TileEffectEmitter.PITCH_TAG, 0)).withStyle(ChatFormatting.GREEN));

            Integer loop = TagUtils.getIntOrDefault(blockItemStack, TileEffectEmitter.SOUND_LOOP_TAG, 0);
            tooltip.add(Component.literal(loop == 0 ? "  Loop: None" : "  Loop: Every " + loop + " ticks").withStyle(ChatFormatting.GREEN));
        }
        
        // Particle
        tooltip.add(Component.literal("Particle:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));
        String particleResource = TagUtils.getStringOrDefault(blockItemStack, TileEffectEmitter.PARTICLE_ID_TAG, "");
        if(particleResource.isBlank()) {
            tooltip.add(Component.literal("  None").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.literal("  ID: " + particleResource).withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal("  Side: " + BlockEffectEmitter.getSideFromByte(TagUtils.getByteOrDefault(blockItemStack, TileEffectEmitter.SIDE_TAG, 0))).withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal("  Speed: ").withStyle(ChatFormatting.GREEN));
            tooltip.add(
                Component.literal("    X: " + TagUtils.getByteOrDefault(blockItemStack, TileEffectEmitter.SPEED_X_TAG, 0) + 
                                  ", Y: " + TagUtils.getByteOrDefault(blockItemStack, TileEffectEmitter.SPEED_Y_TAG, 0) + 
                                  ", Z: " + TagUtils.getByteOrDefault(blockItemStack, TileEffectEmitter.SPEED_Z_TAG, 0)
                ).withStyle(ChatFormatting.GREEN)
            );
            tooltip.add(Component.literal("  Area: " + TagUtils.getByteOrDefault(blockItemStack, TileEffectEmitter.SPREAD_TAG, 0)).withStyle(ChatFormatting.GREEN));
            tooltip.add(Component.literal("  Num: " + TagUtils.getByteOrDefault(blockItemStack, TileEffectEmitter.COUNT_TAG, 0)).withStyle(ChatFormatting.GREEN));
            
            Integer loop = TagUtils.getIntOrDefault(blockItemStack, TileEffectEmitter.PARTICLE_LOOP_TAG, 0);
            tooltip.add(Component.literal(loop == 0 ? "  Loop: None" : "  Loop: Every " + loop + " ticks").withStyle(ChatFormatting.GREEN));
        }
    }
    
    public static String getSideFromByte(Byte side) {
        switch(side) {
            case 0:
            default:
                return "Top";
            case 1:
                return "Bottom";
            case 2:
                return "North";
            case 3:
                return "East";
            case 4:
                return "South";
            case 5:
                return "West";
        }
    }
}