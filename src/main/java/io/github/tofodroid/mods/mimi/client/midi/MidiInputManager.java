package io.github.tofodroid.mods.mimi.client.midi;

import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class MidiInputManager {
    public final MidiInputDeviceManager inputDeviceManager;
    public final MidiFileCasterManager fileCasterManager;
    private Boolean hasActiveFileCaster = false;
    private UUID activeTransmitterIdCache = null;

    public MidiInputManager() {
        this.inputDeviceManager = new MidiInputDeviceManager();
        MinecraftForge.EVENT_BUS.register(this.inputDeviceManager);
        this.fileCasterManager = new MidiFileCasterManager();
        MinecraftForge.EVENT_BUS.register(this.fileCasterManager);
        this.fileCasterManager.open();
        this.inputDeviceManager.open();
    }

    @SubscribeEvent
    public void handleTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT || !event.player.isLocalPlayer()) {
            return;
        }

        Boolean newHasFileCaster = hasFileCaster(event.player);

        if(!newHasFileCaster && this.hasActiveFileCaster) {
            this.fileCasterManager.stop();
        }

        this.hasActiveFileCaster = newHasFileCaster;
    }
    
    @SubscribeEvent
    public void handleSelfLogOut(LoggingOut event) {
        if(event.getPlayer() != null && event.getPlayer().isLocalPlayer()) {
            this.fileCasterManager.stop();
        }
    }

    @SubscribeEvent
    public void onDeathEvent(LivingDeathEvent event) {
        if(EntityType.PLAYER.equals(event.getEntity().getType()) && ((Player)event.getEntity()).isLocalPlayer()) {
            this.fileCasterManager.stop();
        }
    }

    public UUID getActiveTransmitterIdCache() {
        return activeTransmitterIdCache;
    }

    public void setActiveTransmitterIdCache(UUID activeTransmitterIdCache) {
        this.activeTransmitterIdCache = activeTransmitterIdCache;
    }

    public Boolean fileCasterIsActive() {
        return this.hasActiveFileCaster;
    }

    public TransmitMode getTransmitMode() {
        return fileCasterManager.getTransmitMode();
    }

    protected Boolean hasFileCaster(Player player) {
        if(player.getInventory() != null) {

            // Off-hand isn't part of hotbar, so check it explicitly
            if(ModItems.FILECASTER.equals(player.getItemInHand(InteractionHand.OFF_HAND).getItem())) {
                return true;
            }

            // check hotbar
            for(int i = 0; i < 9; i++) {
                ItemStack invStack = player.getInventory().getItem(i);
                if(invStack != null && ModItems.FILECASTER.equals(invStack.getItem())) {
                    return true;
                }
            }
        }

        return false;
    }
}
