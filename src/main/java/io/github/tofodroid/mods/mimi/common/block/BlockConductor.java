package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;

public class BlockConductor extends Block {
    public static final String REGISTRY_NAME = "conductor";

    public BlockConductor() {
        super(Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
    }
}