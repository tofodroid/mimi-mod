package io.github.tofodroid.mods.mimi.common.block;

import java.util.Arrays;
import java.util.List;

import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.common.tile.ModTiles;
import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;
import io.github.tofodroid.mods.mimi.server.midi.transmitter.ServerMusicTransmitterManager;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.phys.BlockHitResult;

public class BlockTransmitter extends AContainerBlock<TileTransmitter> {
    public static final String REGISTRY_NAME = "transmitterblock";

    public BlockTransmitter() {
        super(Properties.of().explosionResistance(6.f).strength(2.f).sound(SoundType.WOOD));
    }

    @Override
    public InteractionResult use(BlockState state, Level worldIn, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hit) {
        TileTransmitter tile = getTileForBlock(worldIn, pos);
        
        if(tile != null) {
           if(worldIn.isClientSide) {
                ClientGuiWrapper.openTransmitterGui(worldIn, tile.getUUID());
                return InteractionResult.CONSUME;
            }
        }

        return InteractionResult.SUCCESS;
    }
    
    @Override
    public void onRemove(BlockState state, Level worldIn, BlockPos pos, BlockState newState, boolean isMoving) {
        TileTransmitter tileEntity = getTileForBlock(worldIn, pos);

        if(!worldIn.isClientSide && tileEntity != null) {
            ServerMusicTransmitterManager.removeTransmitter(tileEntity.getUUID());
        }
        
        super.onRemove(state, worldIn, pos, newState, isMoving);
    }
    
    @Override
    public void setPlacedBy(Level worldIn, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        TileTransmitter tileEntity = getTileForBlock(worldIn, pos);
        if (tileEntity != null) {
            ItemStack newStack = new ItemStack(stack.getItem(), 1);
            newStack.setTag(stack.getOrCreateTag().copy());
            tileEntity.setSourceStack(newStack);
            
            if(!worldIn.isClientSide) {
                ServerMusicTransmitterManager.createTransmitter(tileEntity);
            }
        }
    }

    @Override
    public List<ItemStack> getDrops(BlockState state, LootParams.Builder builder) {
        return Arrays.asList();
    }

    @Override
    public BlockEntityType<TileTransmitter> getTileType() {
        return ModTiles.TRANSMITTER;
    }
}
