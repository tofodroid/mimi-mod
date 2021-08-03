package io.github.tofodroid.mods.mimi.common.block;

import io.github.tofodroid.mods.mimi.common.tile.ATileInventory;
import net.minecraft.block.BlockRenderType;
import net.minecraft.block.BlockState;
import net.minecraft.block.ContainerBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.inventory.InventoryHelper;
import net.minecraft.item.ItemStack;
import net.minecraft.network.PacketBuffer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.world.IBlockReader;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;
import net.minecraftforge.items.IItemHandler;

public abstract class AContainerBlock<T extends ATileInventory> extends ContainerBlock {
    protected AContainerBlock(Properties builder) {
        super(builder);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void onReplaced(BlockState state, World worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!state.isIn(newState.getBlock())) {
            TileEntity tileentity = worldIn.getTileEntity(pos);
            if (tileentity instanceof ATileInventory) {
                dropInventoryItems(worldIn, pos, ((ATileInventory)tileentity).getInventory());
                worldIn.updateComparatorOutputLevel(pos, this);
            }

            super.onReplaced(state, worldIn, pos, newState, isMoving);
        }
    }
    
    @Override
    public ActionResultType onBlockActivated(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, BlockRayTraceResult hit) {
        if(!worldIn.isRemote) {
            T tile = getTileForBlock(worldIn, pos);
            if(tile != null) {
                ServerPlayerEntity serverPlayerEntity = (ServerPlayerEntity) player;
                NetworkHooks.openGui(serverPlayerEntity, this.getContainer(state, worldIn, pos), (packetBuffer -> {
                    writeGuiPacketBuffer(state, worldIn, pos, player, hand, packetBuffer);
                }));
                return ActionResultType.CONSUME;
            }
        }

        return ActionResultType.SUCCESS;
    }
    
    @Override
    public BlockRenderType getRenderType(BlockState state) {
        return BlockRenderType.MODEL;
    }
        
    @Override
    public boolean hasTileEntity(final BlockState state) {
        return true;
    }

    @Override
    public TileEntity createNewTileEntity(IBlockReader worldIn) {
        return getTileType().create();
    }

    public abstract TileEntityType<T> getTileType();
    
    @Override
	public TileEntity createTileEntity(final BlockState state, final IBlockReader reader) {
        return createNewTileEntity(reader);
	}

    public void writeGuiPacketBuffer(BlockState state, World worldIn, BlockPos pos, PlayerEntity player, Hand hand, PacketBuffer buffer) {
        buffer.writeBlockPos(pos);
    }

    @SuppressWarnings("unchecked")
    public T getTileForBlock(World worldIn, BlockPos pos) {
        TileEntity entity = worldIn.getTileEntity(pos);
        return entity != null && entity instanceof ATileInventory ? (T)entity : null;
    }
    
    public void dropInventoryItems(World worldIn, BlockPos pos, IItemHandler inventory) {
        for (int i = 0; i < inventory.getSlots(); i++) {
            ItemStack itemstack = inventory.getStackInSlot(i);
            if (!itemstack.isEmpty()) {
                InventoryHelper.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), itemstack);
            }
        }
    }
}
