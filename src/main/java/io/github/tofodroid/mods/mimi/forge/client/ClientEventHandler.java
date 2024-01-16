package io.github.tofodroid.mods.mimi.forge.client;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.client.renderer.EntitySeatRenderer;
import io.github.tofodroid.mods.mimi.client.renderer.EntityNoteResponseTileRenderer;
import io.github.tofodroid.mods.mimi.client.ClientProxy;
import io.github.tofodroid.mods.mimi.client.gui.*;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.AColoredBlock;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.container.ModContainers;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.item.IColorableItem;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.keybind.ModBindings;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.client.gui.screens.MenuScreens;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.sound.SoundEngineLoadEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@SuppressWarnings({"deprecation"})
@OnlyIn(Dist.CLIENT)
@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
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
        registerIColorableItemColors(event, ModItems.INSTRUMENT_ITEMS.stream().filter(i -> i.isColorable()).collect(Collectors.toList()));
        registerIColorableItemColors(event, ModItems.BLOCK_INSTRUMENT_ITEMS.stream().filter(i -> i.isColorable()).collect(Collectors.toList()));
        registerIDyeableItemColors(event, Arrays.asList(ModItems.LEDCUBE_A, ModItems.LEDCUBE_B, ModItems.LEDCUBE_C, ModItems.LEDCUBE_D, ModItems.LEDCUBE_E, ModItems.LEDCUBE_F, ModItems.LEDCUBE_G, ModItems.LEDCUBE_H));
    }

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.Block event) {
        registerInstrumentBlockColors(event);
        registerAColoredBlockColors(event, Arrays.asList(ModBlocks.LEDCUBE_A.get(), ModBlocks.LEDCUBE_B.get(), ModBlocks.LEDCUBE_C.get(), ModBlocks.LEDCUBE_D.get(), ModBlocks.LEDCUBE_E.get(), ModBlocks.LEDCUBE_F.get(), ModBlocks.LEDCUBE_G.get(), ModBlocks.LEDCUBE_H.get()));
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SEAT.get(), EntitySeatRenderer::new);
        event.registerEntityRenderer(ModEntities.NOTERESPONSIVETILE.get(), EntityNoteResponseTileRenderer::new);
    }
    
    @SubscribeEvent
    public static void handleSoundReload(SoundEngineLoadEvent event) {
        if(MIMIMod.proxy.isClient() && ((ClientProxy)MIMIMod.proxy).getMidiSynth() != null) ((ClientProxy)MIMIMod.proxy).getMidiSynth().reloadSynths();
    }

    protected static void registerIColorableItemColors(RegisterColorHandlersEvent.Item event, List<? extends IColorableItem> items) {
        event.getItemColors().register((stack, color) ->
                    color > 0 ? -1 : ((IColorableItem) stack.getItem()).getColor(stack), items.toArray(new Item[items.size()]));
    }

    protected static void registerInstrumentBlockColors(RegisterColorHandlersEvent.Block event) {
        List<? extends Block> blocks = ModBlocks.getBlockInstruments().stream().filter(i -> i.isColorable()).collect(Collectors.toList());
        event.getBlockColors().register((state, reader, pos, color) -> {
            return reader != null && pos != null && reader.getBlockEntity(pos) != null && reader.getBlockEntity(pos) instanceof TileInstrument ? 
                ((TileInstrument)reader.getBlockEntity(pos)).getColor() : -1;
        }, blocks.toArray(new Block[blocks.size()]));
    }
    
    protected static void registerIDyeableItemColors(RegisterColorHandlersEvent.Item event, List<? extends BlockItem> items) {
        event.getItemColors().register((stack, color) ->
                    color > 0 ? -1 : DyeColor.byId(TagUtils.getIntOrDefault(stack, AColoredBlock.DYE_ID.getName(), 0)).getFireworkColor(), items.toArray(new Item[items.size()]));
    }

    protected static void registerAColoredBlockColors(RegisterColorHandlersEvent.Block event, List<? extends AColoredBlock> blocks) {
        event.getBlockColors().register((state, reader, pos, color) -> {
            return reader != null && pos != null && state.getBlock() instanceof AColoredBlock ?
                AColoredBlock.getDecimalColorFromState(state) : -1;
        }, blocks.toArray(new Block[blocks.size()]));
    }
}