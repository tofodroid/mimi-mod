package io.github.tofodroid.mods.mimi.common.keybind;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.instruments.ItemInstrumentDataUtil;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import net.minecraft.util.Hand;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

@SuppressWarnings("resource")
public class ModBindings {
    public static KeyBinding MIDIPLAYLIST;
    public static KeyBinding MIDISETTINGS;
    public static KeyBinding MIDIALLOFF;
    public static KeyBinding MIDIGUIMAIN;
    public static KeyBinding MIDIGUIOFF;
    public static KeyBinding MIDIGUISEAT;

    public static void register() {
        MIDIPLAYLIST = new KeyBinding("key." + MIMIMod.MODID + ".midi.playlist", GLFW.GLFW_KEY_PERIOD, "key.categories." + MIMIMod.MODID);
        MIDISETTINGS = new KeyBinding("key." + MIMIMod.MODID + ".midi.settings", GLFW.GLFW_KEY_COMMA, "key.categories." + MIMIMod.MODID);
        MIDIALLOFF = new KeyBinding("key." + MIMIMod.MODID + ".midi.alloff", GLFW.GLFW_KEY_END, "key.categories." + MIMIMod.MODID);
        MIDIGUIMAIN = new KeyBinding("key." + MIMIMod.MODID + ".midi.gui.main", GLFW.GLFW_KEY_J, "key.categories." + MIMIMod.MODID);
        MIDIGUIOFF = new KeyBinding("key." + MIMIMod.MODID + ".midi.gui.off", GLFW.GLFW_KEY_K, "key.categories." + MIMIMod.MODID);
        MIDIGUISEAT = new KeyBinding("key." + MIMIMod.MODID + ".midi.gui.seat", GLFW.GLFW_KEY_L, "key.categories." + MIMIMod.MODID);

        ClientRegistry.registerKeyBinding(MIDIPLAYLIST);
        ClientRegistry.registerKeyBinding(MIDISETTINGS);
        ClientRegistry.registerKeyBinding(MIDIALLOFF);
        ClientRegistry.registerKeyBinding(MIDIGUIMAIN);
        ClientRegistry.registerKeyBinding(MIDIGUIOFF);
        ClientRegistry.registerKeyBinding(MIDIGUISEAT);
    }
    
    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        World worldIn = Minecraft.getInstance().world;
        PlayerEntity playerIn = Minecraft.getInstance().player;

        // MIDI All Off
        if(MIDIALLOFF.isPressed()) {
            MIMIMod.proxy.getMidiSynth().allNotesOff();
        }
                
        // GUIs
        if(worldIn != null && playerIn != null) {
            if(MIDIPLAYLIST.isPressed()) {
                if(MIMIMod.proxy.getMidiInput().hasTransmitter()) {
                    MIMIMod.guiWrapper.openPlaylistGui(worldIn, playerIn);
                }
            } else if(MIDIGUIMAIN.isPressed()) {
                ItemStack stack = ItemInstrument.getEntityHeldInstrumentStack(playerIn, Hand.MAIN_HAND);
                if(stack != null) {
                    MIMIMod.guiWrapper.openInstrumentGui(worldIn, playerIn, ItemInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(stack), stack, Hand.MAIN_HAND);
                }
            } else if(MIDIGUIOFF.isPressed()) {
                ItemStack stack = ItemInstrument.getEntityHeldInstrumentStack(playerIn, Hand.OFF_HAND);
                if(stack != null) {
                    MIMIMod.guiWrapper.openInstrumentGui(worldIn, playerIn, ItemInstrumentDataUtil.INSTANCE.getInstrumentIdFromData(stack), stack, Hand.OFF_HAND);
                }
            } else if(MIDIGUISEAT.isPressed()) {
                TileInstrument tile = BlockInstrument.getTileInstrumentForEntity(playerIn);
                if(tile != null) {
                    MIMIMod.guiWrapper.openInstrumentGui(worldIn, playerIn, tile.getInstrumentId(), tile);
                }
            } else if(MIDISETTINGS.isPressed()) {
                MIMIMod.guiWrapper.openConfigGui(worldIn, playerIn);
            }
        }
    }
}
