package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;

public class MIMIModItemGroup extends CreativeModeTab {
    public MIMIModItemGroup() {
        super(MIMIMod.MODID + ".group");
    }

    @Override
    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public ItemStack makeIcon() {
        return new ItemStack(ModBlocks.INSTRUMENTS.get(0));
    }
}
