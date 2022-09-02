package io.github.tofodroid.mods.mimi.common.mob.villager;

import java.util.Arrays;
import java.util.List;
import java.util.OptionalInt;
import java.util.Random;
import java.util.function.Function;

import com.google.common.collect.ImmutableSet;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Lifecycle;

import org.apache.commons.lang3.tuple.Triple;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.core.Holder;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.ProcessorLists;
import net.minecraft.data.worldgen.VillagePools;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModVillagers {
    // POIs (Workstations)
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, MIMIMod.MODID);
    public static final RegistryObject<PoiType> TUNINGTABLE_POI = POI_TYPES.register("tuningtable", () -> new PoiType("tuningtable", ImmutableSet.of(ModBlocks.TUNINGTABLE.get().defaultBlockState()), 1, 1));
    
    // Needed for backwards compat
    public static final RegistryObject<PoiType> INSTRUMENTALIST_POI = POI_TYPES.register("instrumentalist", () -> new PoiType("instrumentalist", ImmutableSet.of(), 0, 0));

    // Professions
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.PROFESSIONS, MIMIMod.MODID);
    public static final RegistryObject<VillagerProfession> INSTRUMENTALIST = PROFESSIONS.register(
        "instrumentalist", 
        () -> {
            return new VillagerProfession(
                "instrumentalist", 
                TUNINGTABLE_POI.get(),
                ImmutableSet.of(Items.NOTE_BLOCK), 
                ImmutableSet.of(), 
                SoundEvents.VILLAGER_WORK_TOOLSMITH
            );
        }
    );

    // Villager Trades
    public static void registerTrades(VillagerTradesEvent event) {
        // Instrumentalist
        if(event.getType() == INSTRUMENTALIST.get()) {
            GiveGiftToHero.GIFTS.put(INSTRUMENTALIST.get(), new ResourceLocation(MIMIMod.MODID, "gameplay/hero_of_the_village/instrumentalist_gift"));
            event.getTrades().get(1).addAll(Arrays.asList(
                new ItemsForItemsTrade(Arrays.asList(Items.MUSIC_DISC_CAT,Items.MUSIC_DISC_13), 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(Items.NOTE_BLOCK), 1, Arrays.asList(Items.PAPER, Items.REDSTONE), 8, 16, 20)
            ));

            event.getTrades().get(2).addAll(Arrays.asList(
                new ItemsForItemsTrade(ModItems.INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(ModItems.SWITCHBOARD, ModItems.FLOPPYDISK), 1, Arrays.asList(Items.EMERALD), 1, 32, 10)
            ));
            
            event.getTrades().get(3).addAll(Arrays.asList(
                new ItemsForItemsTrade(ModItems.BLOCK_INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(ModItems.TRANSMITTER,ModItems.FILECASTER,ModItems.BROADCASTER), 4, Arrays.asList(Items.EMERALD), 1, 16, 30)
            ));
            
            event.getTrades().get(4).addAll(Arrays.asList(
                new ItemsForItemsTrade(ModItems.INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(ModItems.LISTENER,ModItems.RECEIVER,ModItems.CONDUCTOR,ModItems.MECHANICALMAESTRO), 1, Arrays.asList(Items.EMERALD), 4, 16, 25)
            ));
            
            event.getTrades().get(5).addAll(Arrays.asList(
                new ItemsForItemsTrade(Arrays.asList(Items.MUSIC_DISC_PIGSTEP,Items.MUSIC_DISC_STAL,Items.MUSIC_DISC_WAIT,Items.MUSIC_DISC_STRAD,Items.MUSIC_DISC_11,Items.MUSIC_DISC_WARD,Items.MUSIC_DISC_BLOCKS,Items.MUSIC_DISC_CHIRP,Items.MUSIC_DISC_FAR,Items.MUSIC_DISC_MALL,Items.MUSIC_DISC_MELLOHI), 1, Arrays.asList(Items.EMERALD), 16, 1, 50),
                new ItemsForItemsTrade(ModItems.BLOCK_INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25)
            ));
        }        
    }

    // Structures
    public static final List<Triple<ResourceLocation,ResourceLocation,Holder<StructureProcessorList>>> JIGSAW_PIECES = Arrays.asList(
        createJigsawPiece("village/plains/houses", "instrumentalist_house", ProcessorLists.STREET_PLAINS),
        createJigsawPiece("village/snowy/houses", "instrumentalist_house", ProcessorLists.STREET_SNOWY_OR_TAIGA),
        createJigsawPiece("village/savanna/houses", "instrumentalist_house", ProcessorLists.STREET_SAVANNA),
        createJigsawPiece("village/desert/houses", "instrumentalist_house", ProcessorLists.EMPTY),
        createJigsawPiece("village/taiga/houses", "instrumentalist_house",  ProcessorLists.STREET_SNOWY_OR_TAIGA)
    );

    public static void injectStructures() {
		VillagePools.bootstrap();

        JIGSAW_PIECES.forEach(pieceTriple -> {
            StructureTemplatePool patternPool = BuiltinRegistries.TEMPLATE_POOL.get(pieceTriple.getLeft());
            int id = BuiltinRegistries.TEMPLATE_POOL.getId(patternPool);
            if (patternPool == null)
        	    return;
            List<StructurePoolElement> poolElements = patternPool.getShuffledTemplates(new Random());
            Object2IntMap<StructurePoolElement> structurePoolElementMap = new Object2IntLinkedOpenHashMap<>();
            for(StructurePoolElement structurePoolElement : poolElements) {
                structurePoolElementMap.computeInt(structurePoolElement, (StructurePoolElement p, Integer i) -> (i == null ? 0 : i) + 1);
            }
            
            Function<Projection, SinglePoolElement> projectableTemplate = StructurePoolElement.single(pieceTriple.getMiddle().toString(), pieceTriple.getRight());
            StructurePoolElement piece = projectableTemplate.apply(Projection.RIGID);
            structurePoolElementMap.put(piece, 4);
            patternPool.rawTemplates.add(Pair.of(piece, 4));
            ((WritableRegistry<StructureTemplatePool>)BuiltinRegistries.TEMPLATE_POOL).registerOrOverride(OptionalInt.of(id), ResourceKey.create(BuiltinRegistries.TEMPLATE_POOL.key(), patternPool.getName()), patternPool, Lifecycle.stable());
        });
	}



    public static Triple<ResourceLocation,ResourceLocation,Holder<StructureProcessorList>> createJigsawPiece(String poolName, String entryName, Holder<StructureProcessorList> processorList) {
        return Triple.of(new ResourceLocation(poolName), new ResourceLocation(MIMIMod.MODID, poolName + "/" + entryName), processorList);
    }
}