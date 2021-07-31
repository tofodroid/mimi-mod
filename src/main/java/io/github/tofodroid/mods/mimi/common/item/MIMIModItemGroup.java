package io.github.tofodroid.mods.mimi.common.item;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;

public class MIMIModItemGroup extends ItemGroup {
    public MIMIModItemGroup() {
        super(MIMIMod.MODID + ".group");
    }

    @Override
    @Nonnull
    @OnlyIn(Dist.CLIENT)
    public ItemStack createIcon() {
        return new ItemStack(ModBlocks.INSTRUMENTS.get(0));
    }
}
