package io.github.tofodroid.mods.mimi.common.mob.villager;

import java.util.Arrays;
import java.util.List;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.lang3.tuple.Triple;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.village.poi.PoiType;
import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModVillagers {
    // Registration Holders
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, MIMIMod.MODID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS,  MIMIMod.MODID);

    // Points of Interest (Workstations)
    public static final RegistryObject<PoiType> TUNINGTABLE = POI_TYPES.register("tuningtable", () -> new PoiType("instrumentalist", PoiType.getBlockStates(ModBlocks.TUNINGTABLE), 1, 1));

    // Professions
    public static final RegistryObject<VillagerProfession> INSTRUMENTALIST = PROFESSIONS.register("instrumentalist", () -> new VillagerProfession("instrumentalist", TUNINGTABLE.get(), ImmutableSet.of(Items.NOTE_BLOCK), ImmutableSet.of(Blocks.NOTE_BLOCK), SoundEvents.VILLAGER_WORK_TOOLSMITH));

    // Structures
    public static final List<Triple<ResourceLocation,ResourceLocation,Integer>> JIGSAW_PIECES = Arrays.asList(
        createJigsawPiece("village/plains/houses", "instrumentalist_house", 2),
        createJigsawPiece("village/snowy/houses", "instrumentalist_house", 2),
        createJigsawPiece("village/savanna/houses", "instrumentalist_house", 2),
        createJigsawPiece("village/desert/houses", "instrumentalist_house", 2),
        createJigsawPiece("village/taiga/houses", "instrumentalist_house", 2)
    );

    public static void preInit(FMLJavaModLoadingContext fmlContext) {
        //POI_TYPES.register(fmlContext.getModEventBus());
        //PROFESSIONS.register(fmlContext.getModEventBus());
    }

    public static void init() {
        //injectPOITypes();
        //injectGiftTables();
        //injectTrades();
        //injectJigsawPatterns();
    }

    public static void injectPOITypes() {
        POI_TYPES.getEntries().forEach(entry -> {
            try {
                ObfuscationReflectionHelper.findMethod(PoiType.class, "register", PoiType.class).invoke(null, entry.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void injectGiftTables() {
        PROFESSIONS.getEntries().forEach(entry -> {
            GiveGiftToHero.GIFTS.put(entry.get(), new ResourceLocation(entry.get().getRegistryName().getNamespace(), "gameplay/hero_of_the_village/" + entry.get().getRegistryName().getPath() + "_gift"));
        });
    }

    public static void injectTrades() {
        // Instrumentalist
        VillagerTrades.TRADES.put(INSTRUMENTALIST.get(), new Int2ObjectOpenHashMap<VillagerTrades.ItemListing[]>(ImmutableMap.of(
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

    /*
    public static void injectJigsawPatterns() {
        VillagePools.bootstrap();

        JIGSAW_PIECES.forEach(entry -> {
            JigsawPattern old = WorldGenRegistries.JIGSAW_POOL.getOrDefault(entry.getLeft());
            List<PoolElementStructurePiece> shuffled = old != null ? old.getShuffledPieces(new Random()) : ImmutableList.of();
            List<Pair<PoolElementStructurePiece, Integer>> newPieces = shuffled.stream().map(p -> new Pair<>(p, 1)).collect(Collectors.toList());
            newPieces.add(Pair.of(PoolElementStructurePiece.func_242849_a(entry.getMiddle().toString()).apply(PlacementBehaviour.RIGID), entry.getRight()));
            ResourceLocation name = old.getName();
            Registry.register(Registry.STRUCTURE_PIECE, entry.getLeft(), new PoolElementStructurePiece(entry.getLeft(), name, newPieces));
        });
    }
    */

    public static Triple<ResourceLocation,ResourceLocation,Integer> createJigsawPiece(String poolName, String entryName, Integer weight) {
        return Triple.of(new ResourceLocation(poolName), new ResourceLocation(MIMIMod.MODID, poolName + "/" + entryName), weight);
    }
}
