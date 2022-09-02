package io.github.tofodroid.mods.mimi.common.keybind;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.network.KeybindOpenInstrumentPacket;
import io.github.tofodroid.mods.mimi.common.network.KeybindOpenTransmitterPacket;
import io.github.tofodroid.mods.mimi.common.network.NetworkManager;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.ClientRegistry;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;

@SuppressWarnings("resource")
public class ModBindings {
    public static KeyMapping GUIFILECASTER;
    public static KeyMapping GUITRANSMITTER;
    public static KeyMapping MIDISETTINGS;
    public static KeyMapping MIDIALLOFF;
    public static KeyMapping MIDIGUIMAIN;
    public static KeyMapping MIDIGUIOFF;
    public static KeyMapping MIDIGUISEAT;

    public static void register() {
        GUIFILECASTER = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.filecaster", GLFW.GLFW_KEY_PERIOD, "key.categories." + MIMIMod.MODID);
        GUITRANSMITTER = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.transmitter", GLFW.GLFW_KEY_COMMA, "key.categories." + MIMIMod.MODID);
        MIDISETTINGS = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.settings", GLFW.GLFW_KEY_HOME, "key.categories." + MIMIMod.MODID);
        MIDIALLOFF = new KeyMapping("key." + MIMIMod.MODID + ".midi.alloff", GLFW.GLFW_KEY_END, "key.categories." + MIMIMod.MODID);
        MIDIGUIMAIN = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.main", GLFW.GLFW_KEY_J, "key.categories." + MIMIMod.MODID);
        MIDIGUIOFF = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.off", GLFW.GLFW_KEY_K, "key.categories." + MIMIMod.MODID);
        MIDIGUISEAT = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.seat", GLFW.GLFW_KEY_L, "key.categories." + MIMIMod.MODID);

        ClientRegistry.registerKeyBinding(GUIFILECASTER);
        ClientRegistry.registerKeyBinding(GUITRANSMITTER);
        ClientRegistry.registerKeyBinding(MIDISETTINGS);
        ClientRegistry.registerKeyBinding(MIDIALLOFF);
        ClientRegistry.registerKeyBinding(MIDIGUIMAIN);
        ClientRegistry.registerKeyBinding(MIDIGUIOFF);
        ClientRegistry.registerKeyBinding(MIDIGUISEAT);
    }
    
    public static void onKeyInput(KeyInputEvent event) {
        Level worldIn = Minecraft.getInstance().level;
        Player playerIn = Minecraft.getInstance().player;

        // MIDI All Off
        if(MIDIALLOFF.isDown() && MIMIMod.proxy.isClient()) {
            ((ClientProxy)MIMIMod.proxy).getMidiSynth().allNotesOff();
        }

        // GUIs
        if(worldIn != null && playerIn != null && MIMIMod.proxy.isClient() && Minecraft.getInstance().screen == null) {
            if(GUIFILECASTER.isDown()) {
                if(((ClientProxy)MIMIMod.proxy).getMidiInput().fileCasterIsActive()) {
                    ClientGuiWrapper.openPlaylistGui(worldIn, playerIn);
                }
            }else if(GUITRANSMITTER.isDown()) {
                NetworkManager.INFO_CHANNEL.sendToServer(new KeybindOpenTransmitterPacket());
            } else if(MIDIGUIMAIN.isDown()) {
                NetworkManager.INFO_CHANNEL.sendToServer(new KeybindOpenInstrumentPacket(true, InteractionHand.MAIN_HAND));
            } else if(MIDIGUIOFF.isDown()) {
                NetworkManager.INFO_CHANNEL.sendToServer(new KeybindOpenInstrumentPacket(true, InteractionHand.OFF_HAND));
            } else if(MIDIGUISEAT.isDown()) {
                NetworkManager.INFO_CHANNEL.sendToServer(new KeybindOpenInstrumentPacket(false, null));
            } else if(MIDISETTINGS.isDown()) {
                ClientGuiWrapper.openConfigGui(worldIn, playerIn);
            }
        }
    }
}
