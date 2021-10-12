package io.github.tofodroid.mods.mimi.common.mob.villager;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;

import org.apache.commons.lang3.tuple.Triple;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.block.Blocks;
import net.minecraft.entity.ai.brain.task.GiveHeroGiftsTask;
import net.minecraft.entity.merchant.villager.VillagerProfession;
import net.minecraft.entity.merchant.villager.VillagerTrades;
import net.minecraft.item.Items;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.registry.WorldGenRegistries;
import net.minecraft.village.PointOfInterestType;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern;
import net.minecraft.world.gen.feature.jigsaw.JigsawPiece;
import net.minecraft.world.gen.feature.jigsaw.JigsawPattern.PlacementBehaviour;
import net.minecraft.world.gen.feature.structure.VillagesPools;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraft.entity.merchant.villager.VillagerTrades.ITrade;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraft.util.registry.Registry;

public class ModVillagers {
    // Registration Holders
    public static final DeferredRegister<PointOfInterestType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, MIMIMod.MODID);
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS,  MIMIMod.MODID);

    // Points of Interest (Workstations)
    public static final RegistryObject<PointOfInterestType> TUNINGTABLE = POI_TYPES.register("tuningtable", () -> new PointOfInterestType("instrumentalist", PointOfInterestType.getAllStates(ModBlocks.TUNINGTABLE), 1, 1));

    // Professions
    public static final RegistryObject<VillagerProfession> INSTRUMENTALIST = PROFESSIONS.register("instrumentalist", () -> new VillagerProfession("instrumentalist", TUNINGTABLE.get(), ImmutableSet.of(Items.NOTE_BLOCK), ImmutableSet.of(Blocks.NOTE_BLOCK), SoundEvents.ENTITY_VILLAGER_WORK_TOOLSMITH));

    // Structures
    public static final List<Triple<ResourceLocation,ResourceLocation,Integer>> JIGSAW_PIECES = Arrays.asList(
        createJigsawPiece("village/plains/houses", "instrumentalist_house", 2),
        createJigsawPiece("village/snowy/houses", "instrumentalist_house", 2),
        createJigsawPiece("village/savanna/houses", "instrumentalist_house", 2),
        createJigsawPiece("village/desert/houses", "instrumentalist_house", 2),
        createJigsawPiece("village/taiga/houses", "instrumentalist_house", 2)
    );

    public static void preInit(FMLJavaModLoadingContext fmlContext) {
        POI_TYPES.register(fmlContext.getModEventBus());
        PROFESSIONS.register(fmlContext.getModEventBus());
    }

    public static void init() {
        injectPOITypes();
        injectGiftTables();
        injectTrades();
        injectJigsawPatterns();
    }

    public static void injectPOITypes() {
        POI_TYPES.getEntries().forEach(entry -> {
            try {
                ObfuscationReflectionHelper.findMethod(PointOfInterestType.class, "func_221052_a", PointOfInterestType.class).invoke(null, entry.get());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        });
    }

    public static void injectGiftTables() {
        PROFESSIONS.getEntries().forEach(entry -> {
            GiveHeroGiftsTask.GIFTS.put(entry.get(), new ResourceLocation(entry.get().getRegistryName().getNamespace(), "gameplay/hero_of_the_village/" + entry.get().getRegistryName().getPath() + "_gift"));
        });
    }

    public static void injectTrades() {
        // Instrumentalist
        VillagerTrades.VILLAGER_DEFAULT_TRADES.put(INSTRUMENTALIST.get(), new Int2ObjectOpenHashMap<VillagerTrades.ITrade[]>(ImmutableMap.of(
            1, new ITrade[]{
                new ItemsForItemsTrade(Arrays.asList(Items.MUSIC_DISC_CAT,Items.MUSIC_DISC_13), 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(Items.NOTE_BLOCK), 1, Arrays.asList(Items.PAPER, Items.REDSTONE), 8, 16, 20),
            },
            2, new ITrade[]{
                new ItemsForItemsTrade(ModItems.INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(ModItems.SWITCHBOARD), 1, Arrays.asList(Items.EMERALD), 1, 32, 10),
            }, 
            3, new ITrade[]{
                new ItemsForItemsTrade(ModItems.BLOCK_INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(ModItems.MECHANICALMAESTRO,ModItems.CONDUCTOR,ModItems.RECEIVER), 1, Arrays.asList(Items.EMERALD), 8, 16, 30)
            },
            4, new ITrade[]{
                new ItemsForItemsTrade(Arrays.asList(ModItems.TRANSMITTER), 1, Arrays.asList(Items.EMERALD), 6, 16, 30),
                new ItemsForItemsTrade(Arrays.asList(ModItems.LISTENER), 1, Arrays.asList(Items.EMERALD), 4, 16, 25)
            },
            5, new ITrade[]{
                new ItemsForItemsTrade(Arrays.asList(Items.MUSIC_DISC_PIGSTEP,Items.MUSIC_DISC_STAL,Items.MUSIC_DISC_WAIT,Items.MUSIC_DISC_STRAD,Items.MUSIC_DISC_11,Items.MUSIC_DISC_WARD,Items.MUSIC_DISC_BLOCKS,Items.MUSIC_DISC_CHIRP,Items.MUSIC_DISC_FAR,Items.MUSIC_DISC_MALL,Items.MUSIC_DISC_MELLOHI), 1, Arrays.asList(Items.EMERALD), 16, 1, 50),
                new ItemsForItemsTrade(ModItems.INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25)
            }
        )));
    }

    public static void injectJigsawPatterns() {
        VillagesPools.func_244194_a();

        JIGSAW_PIECES.forEach(entry -> {
            JigsawPattern old = WorldGenRegistries.JIGSAW_POOL.getOrDefault(entry.getLeft());
            List<JigsawPiece> shuffled = old != null ? old.getShuffledPieces(new Random()) : ImmutableList.of();
            List<Pair<JigsawPiece, Integer>> newPieces = shuffled.stream().map(p -> new Pair<>(p, 1)).collect(Collectors.toList());
            newPieces.add(Pair.of(JigsawPiece.func_242849_a(entry.getMiddle().toString()).apply(PlacementBehaviour.RIGID), entry.getRight()));
            ResourceLocation name = old.getName();
            Registry.register(WorldGenRegistries.JIGSAW_POOL, entry.getLeft(), new JigsawPattern(entry.getLeft(), name, newPieces));
        });
    }

    public static Triple<ResourceLocation,ResourceLocation,Integer> createJigsawPiece(String poolName, String entryName, Integer weight) {
        return Triple.of(new ResourceLocation(poolName), new ResourceLocation(MIMIMod.MODID, poolName + "/" + entryName), weight);
    }
}
