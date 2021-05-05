package io.github.tofodroid.mods.mimi.common.item;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.world.World;

public class ItemTransmitter extends Item {
    public ItemTransmitter() {
        super(new Properties().group(ModItems.ITEM_GROUP).maxStackSize(1));
        this.setRegistryName("transmitter");
    }
    
    @Override
    @Nonnull
    public ActionResult<ItemStack> onItemRightClick(World worldIn, PlayerEntity playerIn, Hand handIn) {
        final ItemStack heldItem = playerIn.getHeldItem(handIn);

        if(worldIn.isRemote && !playerIn.isSneaking()) {
            MIMIMod.guiWrapper.openPlaylistGui(worldIn, playerIn);
            return new ActionResult<>(ActionResultType.SUCCESS, heldItem);
        }

        return new ActionResult<>(ActionResultType.PASS, heldItem);
    }
}
