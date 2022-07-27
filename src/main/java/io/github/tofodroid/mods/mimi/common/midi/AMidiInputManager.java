package io.github.tofodroid.mods.mimi.common.midi;

import java.util.List;

import org.apache.commons.lang3.tuple.Pair;

import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

public abstract class AMidiInputManager {
    public abstract Boolean hasTransmitter();
    public abstract TransmitMode getTransmitMode();
    public abstract List<Pair<Byte,ItemStack>> getLocalInstrumentsForMidiDevice(Player player, Byte channel);
    public abstract void handleTick(PlayerTickEvent event);
    public abstract void handleSelfLogOut(LoggingOut event);
    public abstract void onDeathDevent(LivingDeathEvent event);
}
