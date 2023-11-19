package io.github.tofodroid.mods.mimi.common.item;

import io.github.tofodroid.mods.mimi.common.block.BlockTransmitter;
import io.github.tofodroid.mods.mimi.common.tile.TileTransmitter;
import io.github.tofodroid.mods.mimi.util.InstrumentDataUtils;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.entity.BlockEntity;

public interface IInstrumentItem extends IDyeableItem {
    public Byte getInstrumentId();
    public String getDefaultChannels();

    @SuppressWarnings("null")
    default public Boolean linkToTransmitter(UseOnContext context) {
        if(context.getPlayer() instanceof ServerPlayer && context.getPlayer().isCrouching() && context.getLevel().getBlockState(context.getClickedPos()).getBlock() instanceof BlockTransmitter) {
            BlockEntity entity = context.getLevel().getBlockEntity(context.getClickedPos());

            if(entity != null && entity instanceof TileTransmitter) {
                InstrumentDataUtils.setMidiSource(context.getItemInHand(), ((TileTransmitter)entity).getUUID(), "Trns. (d,x,y,z)");
                context.getPlayer().displayClientMessage(Component.literal("Linked to Transmitter"), true);
            
                return true;
            }
        }
        return false;
    }
}
