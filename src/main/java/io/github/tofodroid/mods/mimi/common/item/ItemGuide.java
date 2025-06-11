package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.util.ResourceUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import vazkii.patchouli.api.PatchouliAPI;
import net.minecraftforge.fml.ModList;

public class ItemGuide extends Item {
    public static final String REGISTRY_NAME = "guide";
    public static final ClickEvent onClickP = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.curseforge.com/minecraft/mc-mods/patchouli");
    public static final ClickEvent onClickM = new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/tofodroid/mimi-mod/wiki");

    public ItemGuide(Properties props) {
        super(props.stacksTo(1));
    }
    
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, level, tooltip, flagIn);
        tooltip.add(Component.literal("3rd Edition").withStyle(ChatFormatting.GRAY));
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        if (playerIn instanceof ServerPlayer serverPlayer) {
            if (ModList.get().isLoaded("patchouli")) {
                PatchouliAPI.get().openBookGUI(serverPlayer, ResourceUtils.newModLocation(REGISTRY_NAME));
                return new InteractionResultHolder<>(InteractionResult.SUCCESS, playerIn.getItemInHand(handIn));
            }
            
            serverPlayer.sendSystemMessage(
                Component.literal("The ").withStyle(ChatFormatting.DARK_RED)
                .append(Component.literal("Patchouli Mod").withStyle(style -> style.withColor(ChatFormatting.BLUE).withUnderlined(true).withClickEvent(onClickP)))
                .append(Component.literal(" must be installed to read this book.").withStyle(ChatFormatting.DARK_RED))
            );
            serverPlayer.sendSystemMessage(Component.literal(""));
            serverPlayer.sendSystemMessage(
                Component.literal("Visit the ")
                .append(Component.literal("MIMI Wiki").withStyle(style -> style.withColor(ChatFormatting.BLUE).withUnderlined(true).withClickEvent(onClickM)))
                .append(Component.literal(" for online documentation!"))
            );
            serverPlayer.sendSystemMessage(Component.literal(""));
        }
        return new InteractionResultHolder<>(InteractionResult.CONSUME, playerIn.getItemInHand(handIn));
    }
}
