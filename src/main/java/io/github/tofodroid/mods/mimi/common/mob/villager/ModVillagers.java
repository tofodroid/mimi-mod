package io.github.tofodroid.mods.mimi.common.mob.villager;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import org.apache.commons.lang3.tuple.Triple;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.VillagePools;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.structures.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructurePoolElement;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool;
import net.minecraft.world.level.levelgen.feature.structures.StructureTemplatePool.Projection;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

public class ModVillagers {
    // Points of Interest (Workstations)
    public static final PoiType TUNINGTABLE = new PoiType("instrumentalist", PoiType.getBlockStates(ModBlocks.TUNINGTABLE), 1, 1).setRegistryName(MIMIMod.MODID, "tuningtable");

    // Professions
    public static final VillagerProfession INSTRUMENTALIST = new VillagerProfession("instrumentalist", TUNINGTABLE, ImmutableSet.of(Items.NOTE_BLOCK), ImmutableSet.of(Blocks.NOTE_BLOCK), SoundEvents.VILLAGER_WORK_TOOLSMITH).setRegistryName(MIMIMod.MODID, "instrumentalist");

    // Structures
    public static final List<Triple<ResourceLocation,ResourceLocation,Integer>> JIGSAW_PIECES = Arrays.asList(
        createJigsawPiece("village/plains/houses", "instrumentalist_house", 4),
        createJigsawPiece("village/snowy/houses", "instrumentalist_house", 4),
        createJigsawPiece("village/savanna/houses", "instrumentalist_house", 4),
        createJigsawPiece("village/desert/houses", "instrumentalist_house", 4),
        createJigsawPiece("village/taiga/houses", "instrumentalist_house", 4)
    );

    @Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistrationHandler {
        @SubscribeEvent
        public static void registerPoiTypes(final RegistryEvent.Register<PoiType> event) {
            event.getRegistry().register(TUNINGTABLE);
        }

        @SubscribeEvent
        public static void registerProfessions(final RegistryEvent.Register<VillagerProfession> event) {
            event.getRegistry().register(INSTRUMENTALIST);
            injectGiftTables();
            injectTrades();
        }
    }

    protected static void injectGiftTables() {
        GiveGiftToHero.GIFTS.put(INSTRUMENTALIST, new ResourceLocation(INSTRUMENTALIST.getRegistryName().getNamespace(), "gameplay/hero_of_the_village/" + INSTRUMENTALIST.getRegistryName().getPath() + "_gift"));
    }

    protected static void injectTrades() {
        // Instrumentalist
        VillagerTrades.TRADES.put(INSTRUMENTALIST, new Int2ObjectOpenHashMap<VillagerTrades.ItemListing[]>(ImmutableMap.of(
            1, new ItemListing[]{
                new ItemsForItemsTrade(Arrays.asList(Items.MUSIC_DISC_CAT,Items.MUSIC_DISC_13), 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(Items.NOTE_BLOCK), 1, Arrays.asList(Items.PAPER, Items.REDSTONE), 8, 16, 20),
            },
            2, new ItemListing[]{
                new ItemsForItemsTrade(ModItems.INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(ModItems.SWITCHBOARD), 1, Arrays.asList(Items.EMERALD), 1, 32, 10),
            }, 
            3, new ItemListing[]{
                new ItemsForItemsTrade(ModItems.BLOCK_INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(ModItems.MECHANICALMAESTRO,ModItems.CONDUCTOR,ModItems.RECEIVER), 1, Arrays.asList(Items.EMERALD), 8, 16, 30)
            },
            4, new ItemListing[]{
                new ItemsForItemsTrade(Arrays.asList(ModItems.TRANSMITTER), 1, Arrays.asList(Items.EMERALD), 6, 16, 30),
                new ItemsForItemsTrade(Arrays.asList(ModItems.LISTENER), 1, Arrays.asList(Items.EMERALD), 4, 16, 25)
            },
            5, new ItemListing[]{
                new ItemsForItemsTrade(Arrays.asList(Items.MUSIC_DISC_PIGSTEP,Items.MUSIC_DISC_STAL,Items.MUSIC_DISC_WAIT,Items.MUSIC_DISC_STRAD,Items.MUSIC_DISC_11,Items.MUSIC_DISC_WARD,Items.MUSIC_DISC_BLOCKS,Items.MUSIC_DISC_CHIRP,Items.MUSIC_DISC_FAR,Items.MUSIC_DISC_MALL,Items.MUSIC_DISC_MELLOHI), 1, Arrays.asList(Items.EMERALD), 16, 1, 50),
                new ItemsForItemsTrade(ModItems.INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25)
            }
        )));
    }

    public static void injectStructures() {
		VillagePools.bootstrap();

        JIGSAW_PIECES.forEach(pieceTriple -> {
            StructureTemplatePool patternPool = BuiltinRegistries.TEMPLATE_POOL.get(pieceTriple.getLeft());
            if (patternPool == null)
        	    return;
            
            Function<Projection, LegacySinglePoolElement> projectableTemplate = StructurePoolElement.legacy(pieceTriple.getMiddle().toString());
            StructurePoolElement piece = projectableTemplate.apply(Projection.RIGID);

            patternPool.rawTemplates.add(Pair.of(piece, pieceTriple.getRight()));
			for (int i = 0; i < pieceTriple.getRight(); ++i) {
                patternPool.templates.add(piece);
            }
        });
	}

    public static Triple<ResourceLocation,ResourceLocation,Integer> createJigsawPiece(String poolName, String entryName, Integer weight) {
        return Triple.of(new ResourceLocation(poolName), new ResourceLocation(MIMIMod.MODID, poolName + "/" + entryName), weight);
    }
}
