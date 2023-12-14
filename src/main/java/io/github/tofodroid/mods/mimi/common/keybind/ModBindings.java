package io.github.tofodroid.mods.mimi.common.keybind;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.util.EntityUtils;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.InputEvent.Key;

@SuppressWarnings("resource")
public class ModBindings {
    public static KeyMapping GUIFILECASTER;
    public static KeyMapping GUITRANSMITTER;
    public static KeyMapping MIDISETTINGS;
    public static KeyMapping MIDIALLOFF;
    public static KeyMapping MIDIGUIMAIN;
    public static KeyMapping MIDIGUIOFF;
    public static KeyMapping MIDIGUISEAT;

    public static void register(RegisterKeyMappingsEvent event) {
        GUIFILECASTER = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.filecaster", GLFW.GLFW_KEY_PERIOD, "key.categories." + MIMIMod.MODID);
        GUITRANSMITTER = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.transmitter", GLFW.GLFW_KEY_COMMA, "key.categories." + MIMIMod.MODID);
        MIDISETTINGS = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.settings", GLFW.GLFW_KEY_HOME, "key.categories." + MIMIMod.MODID);
        MIDIALLOFF = new KeyMapping("key." + MIMIMod.MODID + ".midi.alloff", GLFW.GLFW_KEY_END, "key.categories." + MIMIMod.MODID);
        MIDIGUIMAIN = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.main", GLFW.GLFW_KEY_J, "key.categories." + MIMIMod.MODID);
        MIDIGUIOFF = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.off", GLFW.GLFW_KEY_K, "key.categories." + MIMIMod.MODID);
        MIDIGUISEAT = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.seat", GLFW.GLFW_KEY_L, "key.categories." + MIMIMod.MODID);

        event.register(GUIFILECASTER);
        event.register(GUITRANSMITTER);
        event.register(MIDISETTINGS);
        event.register(MIDIALLOFF);
        event.register(MIDIGUIMAIN);
        event.register(MIDIGUIOFF);
        event.register(MIDIGUISEAT);
    }
    
    public static void onKeyInput(Key event) {
        Level worldIn = Minecraft.getInstance().level;
        Player playerIn = Minecraft.getInstance().player;

        // MIDI All Off
        if(MIDIALLOFF.isDown() && MIMIMod.proxy.isClient()) {
            ((ClientProxy)MIMIMod.proxy).getMidiSynth().allNotesOff();
        }

        // GUIs
        if(worldIn != null && playerIn != null && MIMIMod.proxy.isClient() && Minecraft.getInstance().screen == null) {
            if(GUIFILECASTER.isDown() && EntityUtils.playerHasActiveTransmitter(playerIn)) {
                ClientGuiWrapper.openEnderTransmitterGui(worldIn, playerIn);
            } else if(MIDIGUIMAIN.isDown()) {
                ClientGuiWrapper.openInstrumentGui(worldIn, playerIn, InteractionHand.MAIN_HAND, playerIn.getItemInHand(InteractionHand.MAIN_HAND));
            } else if(MIDIGUIOFF.isDown()) {
                ClientGuiWrapper.openInstrumentGui(worldIn, playerIn, InteractionHand.OFF_HAND, playerIn.getItemInHand(InteractionHand.OFF_HAND));
            } else if(MIDIGUISEAT.isDown()) {
                TileInstrument tile = BlockInstrument.getTileInstrumentForEntity(playerIn);

                if(tile != null) {
                    ClientGuiWrapper.openInstrumentGui(worldIn, playerIn, null, tile.getInstrumentStack());
                }
            } else if(MIDISETTINGS.isDown()) {
                ClientGuiWrapper.openConfigGui(worldIn, playerIn);
            }
        }
    }
}
