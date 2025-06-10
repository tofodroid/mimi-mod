package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.block.AConfigurableTileBlock;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.tile.AConfigurableTile;
import io.github.tofodroid.mods.mimi.common.tile.TileRelay;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

public class ItemSourceLinker extends Item {
    public static final String REGISTRY_NAME = "sourcelinker";

    public ItemSourceLinker(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, context, tooltip, flagIn);

        if(MidiNbtDataUtils.getMidiSource(stack) == null) {
            tooltip.add(Component.literal("Crouch + Right-Click to Link to a Transmitter").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.literal("Right-Click Block to Link it to Saved Transmitter").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        }

        tooltip.add(Component.literal(""));
        MidiNbtDataUtils.appendMidiSourceTooltip(stack, tooltip);
    }
    
    @Override
    public InteractionResult interactLivingEntity(ItemStack stack, Player user, LivingEntity target, InteractionHand handIn) {
        if(target instanceof Player && user.isCrouching()) {
            if(!user.level().isClientSide) {
                MidiNbtDataUtils.setMidiSource(stack, target.getUUID(), target.getName().getString());
                user.setItemInHand(handIn, stack);
                Component message = Component.literal("Linked ").append(stack.getHoverName()).append(Component.literal(" to ")).append(target.getName());
                user.displayClientMessage(message, true);
            }
            return InteractionResult.SUCCESS;
        }
        return InteractionResult.PASS;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        final ItemStack heldItem = playerIn.getItemInHand(handIn);

        if(playerIn.isCrouching()) {
            if(!worldIn.isClientSide) {
                MidiNbtDataUtils.setMidiSource(heldItem, playerIn.getUUID(), playerIn.getName().getString());
                playerIn.setItemInHand(handIn, heldItem);
                Component message = Component.literal("Linked ").append(heldItem.getHoverName()).append(Component.literal(" to ")).append(playerIn.getName());
                playerIn.displayClientMessage(message, true);
            }
            return InteractionResultHolder.success(heldItem);
        }
        return InteractionResultHolder.pass(heldItem);
    }

    @Override
    public boolean doesSneakBypassUse(ItemStack stack, LevelReader level, BlockPos pos, Player player) {
        Block block = level.getBlockState(pos).getBlock();
        return block.equals(ModBlocks.TRANSMITTERBLOCK) || block.equals(ModBlocks.RELAY);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final ItemStack heldItem = context.getItemInHand();
        UUID savedSource = MidiNbtDataUtils.getMidiSource(heldItem);

        if(!context.isSecondaryUseActive() && savedSource != null) {
            String savedSourceName = MidiNbtDataUtils.getMidiSourceName(heldItem, false);
            BlockState state = context.getLevel().getBlockState(context.getClickedPos());

            if(state.getBlock() instanceof BlockInstrument || state.getBlock().equals(ModBlocks.RECEIVER) || state.getBlock().equals(ModBlocks.RELAY)) {
                if(!context.getLevel().isClientSide) {
                    AConfigurableTile tile = ((AConfigurableTileBlock<?>)state.getBlock()).getTileForBlock(context.getLevel(), context.getClickedPos());

                    if(tile != null && (tile instanceof TileRelay ? !((TileRelay)tile).getUUID().equals(savedSource) : true)) {
                        ItemStack sourceStack = tile.getSourceStack();
                        MidiNbtDataUtils.setMidiSource(sourceStack, savedSource, savedSourceName);
                        tile.setSourceStack(sourceStack);
                        Component message = Component.literal("Linked ").append(state.getBlock().getName()).append(Component.literal(" to configured ")).append(MidiNbtDataUtils.getMidiSourceType(heldItem));
                        context.getPlayer().displayClientMessage(message, true);
                    }
                }
                return InteractionResult.SUCCESS;
            }
        }        
        return InteractionResult.PASS;
    }
}
