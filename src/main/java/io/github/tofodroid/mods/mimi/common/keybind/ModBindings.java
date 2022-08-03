package io.github.tofodroid.mods.mimi.common.keybind;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.KeybindOpenInstrumentPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.InputEvent.Key;

@SuppressWarnings("resource")
public class ModBindings {
    public static KeyMapping MIDIPLAYLIST;
    public static KeyMapping MIDISETTINGS;
    public static KeyMapping MIDIALLOFF;
    public static KeyMapping MIDIGUIMAIN;
    public static KeyMapping MIDIGUIOFF;
    public static KeyMapping MIDIGUISEAT;

    public static void register(RegisterKeyMappingsEvent event) {
        MIDIPLAYLIST = new KeyMapping("key." + MIMIMod.MODID + ".midi.playlist", GLFW.GLFW_KEY_PERIOD, "key.categories." + MIMIMod.MODID);
        MIDISETTINGS = new KeyMapping("key." + MIMIMod.MODID + ".midi.settings", GLFW.GLFW_KEY_COMMA, "key.categories." + MIMIMod.MODID);
        MIDIALLOFF = new KeyMapping("key." + MIMIMod.MODID + ".midi.alloff", GLFW.GLFW_KEY_END, "key.categories." + MIMIMod.MODID);
        MIDIGUIMAIN = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.main", GLFW.GLFW_KEY_J, "key.categories." + MIMIMod.MODID);
        MIDIGUIOFF = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.off", GLFW.GLFW_KEY_K, "key.categories." + MIMIMod.MODID);
        MIDIGUISEAT = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.seat", GLFW.GLFW_KEY_L, "key.categories." + MIMIMod.MODID);

        event.register(MIDIPLAYLIST);
        event.register(MIDISETTINGS);
        event.register(MIDIALLOFF);
        event.register(MIDIGUIMAIN);
        event.register(MIDIGUIOFF);
        event.register(MIDIGUISEAT);
    }
    
    public static void onKeyInput(Key event) {
        Level worldIn = Minecraft.getInstance().level;
        LocalPlayer playerIn = Minecraft.getInstance().player;

        // MIDI All Off
        if(MIDIALLOFF.isDown()) {
            MIMIMod.proxy.getMidiSynth().allNotesOff();
        }
                
        // GUIs
        if(worldIn != null && playerIn != null) {
            if(MIDIPLAYLIST.isDown()) {
                if(MIMIMod.proxy.getMidiInput().hasTransmitter()) {
                    MIMIMod.guiWrapper.openPlaylistGui(worldIn, playerIn);
                }
            } else if(MIDIGUIMAIN.isDown()) {
                NetworkManager.NET_CHANNEL.sendToServer(new KeybindOpenInstrumentPacket(true, InteractionHand.MAIN_HAND));
            } else if(MIDIGUIOFF.isDown()) {
                NetworkManager.NET_CHANNEL.sendToServer(new KeybindOpenInstrumentPacket(true, InteractionHand.OFF_HAND));
            } else if(MIDIGUISEAT.isDown()) {
                NetworkManager.NET_CHANNEL.sendToServer(new KeybindOpenInstrumentPacket(false, null));
            } else if(MIDISETTINGS.isDown()) {
                MIMIMod.guiWrapper.openConfigGui(worldIn, playerIn);
            }
        }
    }
}
