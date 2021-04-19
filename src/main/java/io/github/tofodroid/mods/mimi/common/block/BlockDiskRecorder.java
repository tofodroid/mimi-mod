package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.container.ContainerDiskRecorder;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.container.INamedContainerProvider;
import net.minecraft.inventory.container.SimpleNamedContainerProvider;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;

public class BlockDiskRecorder extends Block {
    private static final ITextComponent CONTAINER_NAME = new TranslationTextComponent("container.crafting");
    
    public BlockDiskRecorder() {
        super(Block.Properties.create(Material.IRON));
        this.setRegistryName("diskrecorder");
    }

    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand handIn, BlockRayTraceResult hit) {
        if (worldIn.isRemote) {
            return ActionResultType.SUCCESS;
        } else {
            player.openContainer(state.getContainer(worldIn, pos));
            return ActionResultType.CONSUME;
        }
    }

    @Override
    public INamedContainerProvider getContainer(BlockState state, World worldIn, BlockPos pos) {
        return new SimpleNamedContainerProvider((id, inventory, player) -> {
            return new ContainerDiskRecorder(id, inventory);
        }, CONTAINER_NAME);
    }
}
