package io.github.tofodroid.mods.mimi.common.block;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentConfig;
import io.github.tofodroid.mods.mimi.common.config.instrument.InstrumentSpec;
import io.github.tofodroid.mods.mimi.util.VoxelShapeUtils;
import net.minecraft.client.renderer.ItemBlockRenderTypes;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.registries.RegisterEvent;

public class ModBlocks {
    // Redstone Blocks
    public static BlockListener LISTENER;
    public static BlockReceiver RECEIVER;
    public static BlockMechanicalMaestro MECHANICALMAESTRO;
    public static BlockConductor CONDUCTOR;

    // Village Blocks
    public static BlockTuningTable TUNINGTABLE;

    // Instrument Blocks
    public static List<BlockInstrument> INSTRUMENTS = buildInstruments();

    public static void submitRegistrations(final RegisterEvent.RegisterHelper<Block> event) {
        LISTENER = new BlockListener();
        event.register(BlockListener.REGISTRY_NAME, LISTENER);

        RECEIVER = new BlockReceiver();
        event.register(BlockReceiver.REGISTRY_NAME, RECEIVER);

        MECHANICALMAESTRO = new BlockMechanicalMaestro();
        event.register(BlockMechanicalMaestro.REGISTRY_NAME, MECHANICALMAESTRO);

        CONDUCTOR = new BlockConductor();
        event.register(BlockConductor.REGISTRY_NAME, CONDUCTOR);

        TUNINGTABLE = new BlockTuningTable();
        event.register(BlockTuningTable.REGISTRY_NAME, TUNINGTABLE);

        INSTRUMENTS.forEach((BlockInstrument instrument) -> {
            event.register(instrument.REGISTRY_NAME, instrument);
        });
    }


    @Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler {
        @SubscribeEvent
        @OnlyIn(Dist.CLIENT)
        public static void clientRegistration(final FMLClientSetupEvent event) {
            // Renderers
            /*
            ModBlocks.INSTRUMENTS.forEach(block -> {
                ItemBlockRenderTypes.setRenderLayer(block, RenderType.cutout());
            });
            */
        }
    }

    public static List<BlockInstrument> buildInstruments()  {
        List<BlockInstrument> result = new ArrayList<>();
        for(InstrumentSpec instrument : InstrumentConfig.getBlockInstruments()) {
            result.add(new BlockInstrument(instrument.instrumentId, instrument.registryName, instrument.isDyeable(), instrument.defaultColor(), VoxelShapeUtils.loadFromStrings(instrument.collisionShapes)));
        }
        return result;
    }
}
