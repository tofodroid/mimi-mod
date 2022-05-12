package io.github.tofodroid.mods.mimi.client;

import java.util.List;
import java.util.stream.Collectors;

import io.github.tofodroid.mods.mimi.client.renderer.EntitySeatRenderer;
import io.github.tofodroid.mods.mimi.client.renderer.EntityNoteResponseTileRenderer;
import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.entity.ModEntities;
import io.github.tofodroid.mods.mimi.common.item.IDyeableInstrumentItem;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientEventHandler {
    @SubscribeEvent
    public static void register(ColorHandlerEvent.Item event) {
        registerItemColors(event, ModItems.INSTRUMENT_ITEMS.stream().filter(i -> i.isDyeable()).collect(Collectors.toList()));
        registerItemColors(event, ModItems.BLOCK_INSTRUMENT_ITEMS.stream().filter(i -> i.isDyeable()).collect(Collectors.toList()));
    }

    @SubscribeEvent
    public static void register(ColorHandlerEvent.Block event) {
        registerBlockColors(event, ModBlocks.INSTRUMENTS.stream().filter(i -> i.isDyeable()).collect(Collectors.toList()));
    }

    @SubscribeEvent
    public static void register(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.SEAT.get(), EntitySeatRenderer::new);
        event.registerEntityRenderer(ModEntities.NOTERESPONSIVETILE.get(), EntityNoteResponseTileRenderer::new);
    }

    protected static void registerItemColors(ColorHandlerEvent.Item event, List<? extends Item> items) {
        event.getItemColors().register((stack, color) ->
                    color > 0 ? -1 : ((IDyeableInstrumentItem) stack.getItem()).getColor(stack), items.toArray(new Item[items.size()]));
    }

    protected static void registerBlockColors(ColorHandlerEvent.Block event, List<? extends Block> blocks) {
        event.getBlockColors().register((state, reader, pos, color) -> {
            return reader != null && pos != null && reader.getBlockEntity(pos) != null && reader.getBlockEntity(pos) instanceof TileInstrument ? 
                ((TileInstrument)reader.getBlockEntity(pos)).getColor() : -1;
        }, blocks.toArray(new Block[blocks.size()]));
    }
}