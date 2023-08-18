package io.github.tofodroid.mods.mimi.client;

import java.util.List;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.client.renderer.EntitySeatRenderer;
import io.github.tofodroid.mods.mimi.client.renderer.EntityNoteResponseTileRenderer;
import io.github.tofodroid.mods.mimi.client.gui.*;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.container.ModContainers;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.item.IDyeableItem;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.keybind.ModBindings;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
@SuppressWarnings("deprecation")
public class ClientEventHandler {
    
    @SubscribeEvent
    public static void registerScreens(FMLClientSetupEvent event) {
        MenuScreens.register(ModContainers.TUNINGTABLE, GuiTuningTableContainerScreen::new);
        MenuScreens.register(ModContainers.MECHANICALMAESTRO, GuiMechanicalMaestroContainerScreen::new);
    }

    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        ModBindings.register(event);
    }

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.Item event) {
        registerItemColors(event, ModItems.INSTRUMENT_ITEMS.stream().filter(i -> i.isDyeable()).collect(Collectors.toList()));
        registerItemColors(event, ModItems.BLOCK_INSTRUMENT_ITEMS.stream().filter(i -> i.isDyeable()).collect(Collectors.toList()));
    }

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.Block event) {
        registerBlockColors(event, ModBlocks.getBlockInstruments().stream().filter(i -> i.isDyeable()).collect(Collectors.toList()));
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SEAT.get(), EntitySeatRenderer::new);
        event.registerEntityRenderer(ModEntities.NOTERESPONSIVETILE.get(), EntityNoteResponseTileRenderer::new);
    }
    
    @SubscribeEvent
    public static void handleSoundReload(SoundEngineLoadEvent event) {
        MIMIMod.LOGGER.info("EVENT");
        if(MIMIMod.proxy.isClient() && ((ClientProxy)MIMIMod.proxy).getMidiSynth() != null) ((ClientProxy)MIMIMod.proxy).getMidiSynth().reloadSynths();
    }

    protected static void registerItemColors(RegisterColorHandlersEvent.Item event, List<? extends Item> items) {
        event.getItemColors().register((stack, color) ->
                    color > 0 ? -1 : ((IDyeableItem) stack.getItem()).getColor(stack), items.toArray(new Item[items.size()]));
    }

    @SuppressWarnings("null")
    protected static void registerBlockColors(RegisterColorHandlersEvent.Block event, List<? extends Block> blocks) {
        event.getBlockColors().register((state, reader, pos, color) -> {
            return reader != null && pos != null && reader.getBlockEntity(pos) != null && reader.getBlockEntity(pos) instanceof TileInstrument ? 
                ((TileInstrument)reader.getBlockEntity(pos)).getColor() : -1;
        }, blocks.toArray(new Block[blocks.size()]));
    }
}