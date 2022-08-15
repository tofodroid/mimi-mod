package io.github.tofodroid.mods.mimi.client.midi;

import io.github.tofodroid.mods.mimi.common.item.ItemFileCaster;
import io.github.tofodroid.mods.mimi.common.item.ItemFloppyDisk;
import io.github.tofodroid.mods.mimi.common.item.ItemTransmitter;
import io.github.tofodroid.mods.mimi.common.network.TransmitterNotePacket.TransmitMode;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut;
import net.minecraftforge.event.TickEvent.Phase;
import net.minecraftforge.event.TickEvent.PlayerTickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.LogicalSide;

@OnlyIn(Dist.CLIENT)
public class MidiInputManager {
    public final MidiInputDeviceManager inputDeviceManager;
    public final MidiFileCasterManager fileCasterManager;
    public final MidiTransmitterManager transmitterManager;

    private Integer activeSlot = null;
    private ItemStack referenceStack = null;
    private Boolean activeIsTransmitter = null;

    public MidiInputManager() {
        this.inputDeviceManager = new MidiInputDeviceManager();
        this.fileCasterManager = new MidiFileCasterManager();
        this.transmitterManager = new MidiTransmitterManager();
        this.fileCasterManager.open();
        this.inputDeviceManager.open();
        this.transmitterManager.open();
    }

    public Boolean fileCasterIsActive() {
        return this.activeIsTransmitter != null && !this.activeIsTransmitter;
    }
    
    public Boolean transmitterIsActive() {
        return this.activeIsTransmitter != null && this.activeIsTransmitter;
    }
    
    public Integer getActiveSlot() {
        return activeSlot;
    }

    public TransmitMode getTransmitMode() {
        if(fileCasterIsActive()) {
            return fileCasterManager.getTransmitMode();
        } else if(transmitterIsActive()) {
            return ItemTransmitter.getTransmitMode(this.referenceStack);
        }
        return TransmitMode.SELF;
    }

    @SubscribeEvent
    public void handleTick(PlayerTickEvent event) {
        if(event.phase != Phase.END || event.side != LogicalSide.CLIENT || !event.player.isLocalPlayer()) {
            return;
        }

        if(this.activeSlot != null && !ItemStack.matches(event.player.getInventory().getItem(this.activeSlot), this.referenceStack)) {
            // For transmitters, compare again ignoring transmit mode
            if(this.transmitterIsActive()) {
                ItemStack compareStack = this.referenceStack.copy();
                ItemTransmitter.setTransmitMode(compareStack, ItemTransmitter.getTransmitMode(event.player.getInventory().getItem(this.activeSlot)));

                if(ItemStack.matches(event.player.getInventory().getItem(this.activeSlot), compareStack)) {
                    this.referenceStack = compareStack;
                    return;
                }
            }

            this.fileCasterManager.stop();
            this.transmitterManager.stop();
            this.activeIsTransmitter = null;
            this.referenceStack = null;
            this.activeSlot = null;
        }
    }
    
    @SubscribeEvent
    public void handleSelfLogOut(LoggingOut event) {
        if(event.getPlayer() != null && event.getPlayer().isLocalPlayer()) {
            this.fileCasterManager.stop();
            this.transmitterManager.stop();
        }
    }

    @SubscribeEvent
    public void onDeathEvent(LivingDeathEvent event) {
        if(EntityType.PLAYER.equals(event.getEntity().getType()) && ((Player)event.getEntity()).isLocalPlayer()) {
            this.fileCasterManager.stop();
            this.transmitterManager.stop();
        }
    }

    public void setActiveSlot(Integer newSlot, ItemStack newStack) {
        Boolean flagTransmitter = newStack.getItem() instanceof ItemTransmitter;
        Boolean flagFileCaster = newStack.getItem() instanceof ItemFileCaster;

        if(flagTransmitter || flagFileCaster) {
            // Stop any active transmissions
            if(this.activeSlot != null) {
                this.fileCasterManager.stop();
                this.transmitterManager.stop();
            }

            this.activeSlot = newSlot;
            this.referenceStack = newStack.copy();

            if(flagTransmitter && ItemTransmitter.hasActiveFloppyDisk(newStack)) {
                this.transmitterManager.loadURL(ItemFloppyDisk.getMidiUrl(ItemTransmitter.getActiveFloppyDiskStack(newStack)));
                this.activeIsTransmitter = true;
            } else {
                this.fileCasterManager.playFromBeginning();
                this.activeIsTransmitter = false;
            }
        }
        
    }
}
