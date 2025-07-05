package io.github.tofodroid.mods.mimi.common.block;

import java.util.List;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileListener;
import io.github.tofodroid.mods.mimi.server.events.note.consumer.ServerNoteConsumerManager;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class BlockListener extends AConfigurableMidiPowerSourceBlock<TileListener> {
    public static final String REGISTRY_NAME = "listener";

    public BlockListener(Properties props) {
        super(props.explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD).isRedstoneConductor((a,b,c) -> false));
    }

    @Override
    public OpenGuiWrapper openGuiWrapper() {
        return ClientGuiWrapper::openListenerGui;
    }

    @Override
    public BlockEntityType<TileListener> getTileType() {
        return ModTiles.LISTENER;
    }

    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if(!worldIn.isClientSide) {
            if (!state.hasBlockEntity() || state.getBlock() == newState.getBlock())
                return;
            BlockEntity blockEntity = worldIn.getBlockEntity(pos);
            
            if (blockEntity instanceof TileListener) {
                ServerNoteConsumerManager.removeConsumers(((TileListener)blockEntity).getUUID());
            }
        }

        super.onRemove(state, worldIn, pos, newState, isMoving);
    }

    @Override
    protected void appendSettingsTooltip(ItemStack blockItemStack, List<Component> tooltip) {
        tooltip.add(Component.literal(""));
        tooltip.add(Component.literal("MIDI Settings:").withStyle(ChatFormatting.AQUA, ChatFormatting.BOLD));

        MidiNbtDataUtils.appendPowerModeTooltip(blockItemStack, tooltip);
        MidiNbtDataUtils.appendFilterInstrumentTooltip(blockItemStack, tooltip);
        MidiNbtDataUtils.appendFilterNoteTooltip(blockItemStack, tooltip);
    }

    @Override
    protected Boolean shouldSkipUse(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand) {
        Item useItem = player.getItemInHand(hand).getItem();
        return useItem.equals(ModItems.SETTINGSSYNC);
    }
}
