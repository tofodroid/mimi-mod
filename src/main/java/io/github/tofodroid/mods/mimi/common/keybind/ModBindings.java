package io.github.tofodroid.mods.mimi.common.keybind;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;

@SuppressWarnings("resource")
public class ModBindings {
    public static KeyBinding MIDIPLAYLIST;
    public static KeyBinding MIDISETTINGS;
    public static KeyBinding MIDIALLOFF;

    public static void register() {
        MIDIPLAYLIST = new KeyBinding("key." + MIMIMod.MODID + ".midi.playlist", GLFW.GLFW_KEY_PERIOD, "key.categories." + MIMIMod.MODID);
        MIDISETTINGS = new KeyBinding("key." + MIMIMod.MODID + ".midi.settings", GLFW.GLFW_KEY_COMMA, "key.categories." + MIMIMod.MODID);
        MIDIALLOFF = new KeyBinding("key." + MIMIMod.MODID + ".midi.alloff", GLFW.GLFW_KEY_END, "key.categories." + MIMIMod.MODID);

        ClientRegistry.registerKeyBinding(MIDIPLAYLIST);
        ClientRegistry.registerKeyBinding(MIDISETTINGS);
        ClientRegistry.registerKeyBinding(MIDIALLOFF);
    }
    
    @SubscribeEvent
    public void onKeyInput(KeyInputEvent event) {
        World worldIn = Minecraft.getInstance().world;
        PlayerEntity playerIn = Minecraft.getInstance().player;

        if(MIDIPLAYLIST.isPressed()) {
            if(worldIn != null && playerIn != null && MIMIMod.proxy.getMidiInput().hasTransmitter()) {
                MIMIMod.guiWrapper.openPlaylistGui(worldIn, playerIn);
            }
        }

        if(MIDISETTINGS.isPressed()) {
            if(worldIn != null && playerIn != null) {
                MIMIMod.guiWrapper.openConfigGui(worldIn, playerIn);
            }
        }

        if(MIDIALLOFF.isPressed()) {
            MIMIMod.proxy.getMidiSynth().allNotesOff();
        }
    }
}
