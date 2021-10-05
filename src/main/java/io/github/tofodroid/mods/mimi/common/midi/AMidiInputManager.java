package io.github.tofodroid.mods.mimi.common.midi;

import java.util.List;

import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggedOutEvent;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public abstract class AMidiInputManager {
    public abstract Boolean hasTransmitter();
    public abstract TransmitMode getTransmitMode();
    public abstract List<ItemStack> getLocalInstrumentsForMidiDevice(PlayerEntity player, Byte channel);
    public abstract void handleTick(PlayerTickEvent event);
    public abstract void handleSelfLogOut(LoggedOutEvent event);
    public abstract void onDeathDevent(LivingDeathEvent event);
}
