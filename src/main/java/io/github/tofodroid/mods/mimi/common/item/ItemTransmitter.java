package io.github.tofodroid.mods.mimi.common.item;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.ServerMusicPlayerStatusPacket;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class ItemTransmitter extends Item {
    public static final String REGISTRY_NAME = "transmitter";

    public ItemTransmitter(Properties props) {
        super(props.stacksTo(1));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        if(MIMIMod.getProxy().isClient()) {
            ServerMusicPlayerStatusPacket status = ((ClientProxy)MIMIMod.getProxy()).getMidiData().getPlayerStatusPacket();
            
            if(status != null) {
                return status.isPlaying;
            }
        }
        return false;
    }

    @Override
    @Nonnull
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn) {
        final ItemStack heldItem = playerIn.getItemInHand(handIn);

        if(worldIn.isClientSide && !playerIn.isCrouching()) {
            ClientGuiWrapper.openTransmitterGui(worldIn, playerIn);
            return new InteractionResultHolder<>(InteractionResult.SUCCESS, heldItem);
        }

        return new InteractionResultHolder<>(InteractionResult.PASS, heldItem);
    }
}
