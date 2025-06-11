package io.github.tofodroid.mods.mimi.server.events;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.ConfigProxy;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public abstract class ServerEventHandler {
    public static final String FIRST_JOIN_MOD_TAG = MIMIMod.MODID + "_joined";
    public static final ClickEvent onClick = new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/mimi config");
    
    public static void onPlayerLoggedIn(ServerPlayer player) {
        if(ConfigProxy.getDoGiveBookOnFirstJoin() && isFirstJoin(player)) {
            player.sendSystemMessage(Component.literal("")
                .append(Component.literal("Click Here").withStyle(style -> style.withColor(ChatFormatting.BLUE).withUnderlined(true).withClickEvent(onClick)))
                .append(Component.literal(" or run the "))
                .append(Component.literal("/mimi config ").withStyle(style -> style.withColor(ChatFormatting.GREEN).withClickEvent(onClick)))
                .append(Component.literal("command at any time to configure MIMI settings!"))
            );
            player.sendSystemMessage(Component.literal(""));
            player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.GUIDE, 1));
        }
    }

    public static boolean isFirstJoin(Player player){
        if(player.getTags().contains(FIRST_JOIN_MOD_TAG)) {
            return false;
        }
        return player.addTag(FIRST_JOIN_MOD_TAG);
    }
}
