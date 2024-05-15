package io.github.tofodroid.mods.mimi.neoforge.client;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.client.renderer.EntitySeatRenderer;
import io.github.tofodroid.mods.mimi.client.renderer.EntityNoteResponseTileRenderer;
import io.github.tofodroid.mods.mimi.client.gui.*;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.AColoredBlock;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.container.ModContainers;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.item.IInstrumentItem;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.keybind.ModBindings;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.client.KeyMapping;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.block.Block;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@SuppressWarnings({"deprecation"})
@OnlyIn(Dist.CLIENT)
@EventBusSubscriber(modid = MIMIMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientRegistrationHandler {
    @SubscribeEvent
    public static void register(RegisterKeyMappingsEvent event) {
        for(KeyMapping bind : ModBindings.REGISTRANTS) {
            event.register(bind);
        }
    }
    
    @SubscribeEvent
    public static void register(RegisterMenuScreensEvent event) {
        event.register(ModContainers.TUNINGTABLE, GuiTuningTableContainerScreen::new);
        event.register(ModContainers.MECHANICALMAESTRO, GuiMechanicalMaestroContainerScreen::new);
    }

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.Item event) {
        registerDyeableItemColors(event, ModItems.INSTRUMENT_ITEMS.stream().filter(i -> i.getDefaultColor() != null).collect(Collectors.toList()));
        registerDyeableItemColors(event, ModItems.BLOCK_INSTRUMENT_ITEMS.stream().filter(i -> i.getDefaultColor() != null).collect(Collectors.toList()));
        registerIDyeableItemColors(event, Arrays.asList(ModItems.LEDCUBE_A, ModItems.LEDCUBE_B, ModItems.LEDCUBE_C, ModItems.LEDCUBE_D, ModItems.LEDCUBE_E, ModItems.LEDCUBE_F, ModItems.LEDCUBE_G, ModItems.LEDCUBE_H));
    }

    @SubscribeEvent
    public static void register(RegisterColorHandlersEvent.Block event) {
        registerInstrumentBlockColors(event);
        registerAColoredBlockColors(event, Arrays.asList(ModBlocks.LEDCUBE_A, ModBlocks.LEDCUBE_B, ModBlocks.LEDCUBE_C, ModBlocks.LEDCUBE_D, ModBlocks.LEDCUBE_E, ModBlocks.LEDCUBE_F, ModBlocks.LEDCUBE_G, ModBlocks.LEDCUBE_H));
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SEAT, EntitySeatRenderer::new);
        event.registerEntityRenderer(ModEntities.NOTERESPONSIVETILE, EntityNoteResponseTileRenderer::new);
    }

    protected static void registerDyeableItemColors(RegisterColorHandlersEvent.Item event, List<? extends IInstrumentItem> items) {
        event.getItemColors().register((stack, color) -> {
            //stack.has(DataComponents.DYED_COLOR) ? stack.get(DataComponents.DYED_COLOR).rgb() : ((IInstrumentItem)stack.getItem()).getDefaultColor(), items.toArray(new Item[items.size()]));
                return color > 0 ? -1 : DyedItemColor.getOrDefault(stack, FastColor.ARGB32.opaque(((IInstrumentItem)stack.getItem()).getDefaultColor()));
            }, items.toArray(new Item[items.size()]) 
        );
    }

    protected static void registerInstrumentBlockColors(RegisterColorHandlersEvent.Block event) {
        List<? extends Block> blocks = ModBlocks.INSTRUMENTS.stream().filter(i -> i.getDefaultColor() != null).collect(Collectors.toList());
        event.getBlockColors().register((state, reader, pos, color) -> {
            return reader != null && pos != null && reader.getBlockEntity(pos) != null && reader.getBlockEntity(pos) instanceof TileInstrument ? 
                ((TileInstrument)reader.getBlockEntity(pos)).getColor() : -1;
        }, blocks.toArray(new Block[blocks.size()]));
    }
    
    protected static void registerIDyeableItemColors(RegisterColorHandlersEvent.Item event, List<? extends BlockItem> items) {
        event.getItemColors().register((stack, color) -> { 
            return color > 0 ? -1 : DyeColor.byId(TagUtils.getIntOrDefault(stack, AColoredBlock.DYE_ID.getName(), 0)).getFireworkColor();
        }, items.toArray(new Item[items.size()])
        );
    }

    protected static void registerAColoredBlockColors(RegisterColorHandlersEvent.Block event, List<? extends AColoredBlock> blocks) {
        event.getBlockColors().register((state, reader, pos, color) -> {
            return reader != null && pos != null && state.getBlock() instanceof AColoredBlock ?
                AColoredBlock.getDecimalColorFromState(state) : -1;
        }, blocks.toArray(new Block[blocks.size()]));
    }
}