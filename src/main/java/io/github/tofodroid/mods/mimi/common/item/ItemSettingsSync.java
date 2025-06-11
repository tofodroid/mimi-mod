package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;
import java.util.function.BiFunction;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.block.AConfigurableTileBlock;
import io.github.tofodroid.mods.mimi.common.block.BlockEffectEmitter;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class ItemSettingsSync extends Item {
    public static final String REGISTRY_NAME = "settingssync";
    public static final Integer INSTRUMENT_SETTING_TYPE = -1;
    public static final Integer NONE_SETTING_TYPE = -2;

    public ItemSettingsSync(Properties props) {
        super(props.stacksTo(64));
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, level, tooltip, flagIn);
        Integer settingType = getSettingType(stack);

        if(settingType < 0) {
            tooltip.add(Component.literal("Crouch + Right-Click Block to Copy Settings").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
        } else {
            tooltip.add(Component.literal("Right-Click Block of Same Type to Paste").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC));
            tooltip.add(Component.literal(""));
        }

        if(settingType == INSTRUMENT_SETTING_TYPE) {
            tooltip.add(Component.literal("Instrument").withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD, ChatFormatting.ITALIC));
            IInstrumentItem.appendInstrumentTooltip(stack, tooltip);
        } else if(settingType >= 0) {
            AConfigurableTileBlock<?> block = ModBlocks.SETTINGS_SYNC_BLOCKS.get(settingType);
            tooltip.add(block.getName().withStyle(ChatFormatting.GREEN, ChatFormatting.BOLD, ChatFormatting.ITALIC));
            block.appendHoverText(stack, level, tooltip, flagIn);
        }
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        final ItemStack heldItem = playerIn.getItemInHand(handIn);

        if(worldIn.isClientSide && !playerIn.isCrouching()) {
            Integer settingType = getSettingType(heldItem);

            if(settingType == INSTRUMENT_SETTING_TYPE) {
                ClientGuiWrapper.openInstrumentGui(worldIn, playerIn, null, handIn, heldItem);
                return InteractionResultHolder.success(heldItem);
            } else if(settingType >= 0) {
                ModBlocks.SETTINGS_SYNC_BLOCKS.get(settingType).openGuiWrapper().accept(worldIn, playerIn, null, handIn, heldItem);
                return InteractionResultHolder.success(heldItem);
            }
        }

        return InteractionResultHolder.pass(heldItem);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        final ItemStack heldItem = context.getItemInHand();
        BlockState state = context.getLevel().getBlockState(context.getClickedPos());

        if(state.getBlock() instanceof AConfigurableTileBlock) {
            AConfigurableTileBlock<?> block = (AConfigurableTileBlock<?>)state.getBlock();
            BiFunction<ItemStack, ItemStack, ItemStack> copyFunction = block.equals(ModBlocks.EFFECTEMITTER) ? BlockEffectEmitter::copyEffectEmitterSettings : MidiNbtDataUtils::copyMidiSettings;
            Integer blockSettingType = toSettingType(block);
            Integer stackSettingType = getSettingType(heldItem);

            if(blockSettingType >= -1) {
                if(context.isSecondaryUseActive()) {
                    if(!context.getLevel().isClientSide) {
                        // Copy Block --> Item
                        ItemStack newStack = copyFunction.apply(block.getSourceStack(context.getLevel(), context.getClickedPos()), heldItem);
                        TagUtils.setOrRemoveInt(newStack, "setting_type", blockSettingType);
                        context.getPlayer().setItemInHand(context.getHand(), newStack);
                        Component message = Component.literal("Copied ").append(state.getBlock().getName()).append(Component.literal(" Settings to Synchronizer"));
                        context.getPlayer().displayClientMessage(message, true);
                        return InteractionResult.PASS;
                    } else {
                        return InteractionResult.SUCCESS;
                    }
                } else if(stackSettingType == blockSettingType) {
                    // Paste Item --> Block
                    block.setSourceStack(context.getLevel(), context.getClickedPos(), copyFunction.apply(heldItem, block.getSourceStack(context.getLevel(), context.getClickedPos())));
                    Component message = Component.literal("Applied Synchronizer Settings to ").append(state.getBlock().getName());
                    context.getPlayer().displayClientMessage(message, true);
                    return InteractionResult.SUCCESS;
                }
            }
        }
        return InteractionResult.PASS;
    }


    public Integer toSettingType(AConfigurableTileBlock<?> block) {
        if(block instanceof BlockInstrument) {
            return INSTRUMENT_SETTING_TYPE;
        }
        return ModBlocks.SETTINGS_SYNC_BLOCKS.indexOf(block);
    }

    public Integer getSettingType(ItemStack stack) {
        Integer raw = TagUtils.getIntOrDefault(stack, "setting_type", NONE_SETTING_TYPE);
        return raw >= NONE_SETTING_TYPE && raw < ModBlocks.SETTINGS_SYNC_BLOCKS.size() ? raw : NONE_SETTING_TYPE;
    }
}
