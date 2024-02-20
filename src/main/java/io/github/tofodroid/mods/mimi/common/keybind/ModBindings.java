package io.github.tofodroid.mods.mimi.common.keybind;

import io.github.tofodroid.mods.mimi.client.gui.ClientGuiWrapper;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.util.EntityUtils;

import java.util.Arrays;
import java.util.List;

import org.lwjgl.glfw.GLFW;

import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@SuppressWarnings("resource")
public class ModBindings {
    public static KeyMapping GUIFILECASTER = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.filecaster", GLFW.GLFW_KEY_PERIOD, "key.categories." + MIMIMod.MODID);
    public static KeyMapping GUITRANSMITTER = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.transmitter", GLFW.GLFW_KEY_COMMA, "key.categories." + MIMIMod.MODID);
    public static KeyMapping MIDISETTINGS = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.settings", GLFW.GLFW_KEY_HOME, "key.categories." + MIMIMod.MODID);
    public static KeyMapping MIDIALLOFF = new KeyMapping("key." + MIMIMod.MODID + ".midi.alloff", GLFW.GLFW_KEY_END, "key.categories." + MIMIMod.MODID);
    public static KeyMapping MIDIGUIMAIN = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.main", GLFW.GLFW_KEY_J, "key.categories." + MIMIMod.MODID);
    public static KeyMapping MIDIGUIOFF = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.off", GLFW.GLFW_KEY_K, "key.categories." + MIMIMod.MODID);
    public static KeyMapping MIDIGUISEAT = new KeyMapping("key." + MIMIMod.MODID + ".midi.gui.seat", GLFW.GLFW_KEY_L, "key.categories." + MIMIMod.MODID);
    
    public static List<KeyMapping> REGISTRANTS = Arrays.asList(GUIFILECASTER, GUITRANSMITTER, MIDISETTINGS, MIDIALLOFF, MIDIGUIMAIN, MIDIGUIOFF, MIDIGUISEAT);
    
    public static void onKeyInput() {
        Level worldIn = Minecraft.getInstance().level;
        Player playerIn = Minecraft.getInstance().player;

        // MIDI All Off
        if(MIDIALLOFF.isDown() && MIMIMod.getProxy().isClient()) {
            ((ClientProxy)MIMIMod.getProxy()).getMidiSynth().allNotesOff();
        }

        // GUIs
        if(worldIn != null && playerIn != null && MIMIMod.getProxy().isClient() && Minecraft.getInstance().screen == null) {
            if(GUIFILECASTER.isDown() && EntityUtils.playerHasActiveTransmitter(playerIn)) {
                ClientGuiWrapper.openTransmitterGui(worldIn, playerIn);
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
