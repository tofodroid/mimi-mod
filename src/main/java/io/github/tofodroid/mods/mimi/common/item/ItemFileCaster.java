package io.github.tofodroid.mods.mimi.common.item;

import java.util.List;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

public class ItemFileCaster extends Item {
    public static final String REGISTRY_NAME = "filecaster";

    public ItemFileCaster() {
        super(new Properties().stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if(MIMIMod.proxy.isClient()) {
            return ((ClientProxy)MIMIMod.proxy).getMidiInput().enderTransmitterManager.isPlaying();
        }
        return false;
    }
    
    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        super.appendHoverText(stack, worldIn, tooltip, flagIn);

        // Client-side only
        if(worldIn != null && worldIn.isClientSide) {
            
            if(((ClientProxy)MIMIMod.proxy).getMidiInput().enderTransmitterManager.isPlaying()) {

                tooltip.add(Component.literal("§2§lCurrently Playing§r"));
                tooltip.add(Component.literal("§oMust keep one Ender Transmitter in Hands§r"));
                tooltip.add(Component.literal("§oor HotBar to keep playing§r"));
            }
        }
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        final ItemStack heldItem = playerIn.getItemInHand(handIn);

        if(worldIn.isClientSide && !playerIn.isCrouching()) {
            ClientGuiWrapper.openEnderTransmitterGui(worldIn);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, heldItem);
        }

        return new InteractionResultHolder<>(InteractionResult.PASS, heldItem);
    }
}
