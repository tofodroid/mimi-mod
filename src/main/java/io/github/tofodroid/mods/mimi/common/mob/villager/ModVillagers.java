package io.github.tofodroid.mods.mimi.common.mob.villager;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableSet;

import io.github.tofodroid.mods.mimi.util.ResourceUtils;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.entity.npc.VillagerTrades.ItemListing;
import net.minecraft.world.item.Items;

public class ModVillagers {
    public static final Map<ResourceLocation, PoiType> POI_TYPES = new HashMap<>();
    public static final Map<ResourceLocation, VillagerProfession> PROFESSIONS = new HashMap<>();

    // POIs (Workstations)
    //public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, MIMIMod.MODID);
    public static final PoiType TUNINGTABLE_POI = create("tuningtable", new PoiType(ImmutableSet.of(ModBlocks.TUNINGTABLE.defaultBlockState()), 1, 1));
    
    // Needed for backwards compat
    public static final PoiType INSTRUMENTALIST_POI = create("instrumentalist", new PoiType(ImmutableSet.of(), 0, 0));

    // Professions
    //public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, MIMIMod.MODID);
    public static final VillagerProfession INSTRUMENTALIST = create(
        "instrumentalist", 
        new VillagerProfession(
            "instrumentalist", 
            holder -> holder.value().equals(TUNINGTABLE_POI), 
            holder -> holder.value().equals(TUNINGTABLE_POI), 
            ImmutableSet.of(Items.NOTE_BLOCK), 
            ImmutableSet.of(), 
            SoundEvents.VILLAGER_WORK_TOOLSMITH
        )
    );

    public static PoiType create(String id, PoiType type) {
        POI_TYPES.put(ResourceUtils.newModLocation(id), type);
        return type;
    }

    public static VillagerProfession create(String id, VillagerProfession profession) {
        PROFESSIONS.put(ResourceUtils.newModLocation(id), profession);
        return profession;
    }

    public static void registerTrades(VillagerProfession profession, Int2ObjectMap<List<ItemListing>> trades) {
        // Instrumentalist
        if(profession == INSTRUMENTALIST) {
            GiveGiftToHero.GIFTS.put(INSTRUMENTALIST, ResourceKey.create(Registries.LOOT_TABLE,  ResourceUtils.newModLocation("gameplay/hero_of_the_village/instrumentalist_gift")));
            trades.get(1).addAll(Arrays.asList(
                new ItemsForItemsTrade(Arrays.asList(Items.MUSIC_DISC_CAT,Items.MUSIC_DISC_13), 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(Items.NOTE_BLOCK), 1, Arrays.asList(Items.PAPER, Items.REDSTONE), 8, 16, 20)
            ));

            trades.get(2).addAll(Arrays.asList(
                new ItemsForItemsTrade(ModItems.INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25)
            ));
            
            trades.get(3).addAll(Arrays.asList(
                new ItemsForItemsTrade(ModItems.BLOCK_INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(ModItems.TRANSMITTER), 4, Arrays.asList(Items.EMERALD), 1, 16, 30)
            ));
            
            trades.get(4).addAll(Arrays.asList(
                new ItemsForItemsTrade(ModItems.INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(ModItems.LISTENER,ModItems.RECEIVER,ModItems.MECHANICALMAESTRO), 1, Arrays.asList(Items.EMERALD), 4, 16, 25)
            ));
            
            trades.get(5).addAll(Arrays.asList(
                new ItemsForItemsTrade(Arrays.asList(Items.DISC_FRAGMENT_5,Items.MUSIC_DISC_PIGSTEP,Items.MUSIC_DISC_STAL,Items.MUSIC_DISC_WAIT,Items.MUSIC_DISC_STRAD,Items.MUSIC_DISC_11,Items.MUSIC_DISC_WARD,Items.MUSIC_DISC_BLOCKS,Items.MUSIC_DISC_CHIRP,Items.MUSIC_DISC_FAR,Items.MUSIC_DISC_MALL,Items.MUSIC_DISC_MELLOHI), 1, Arrays.asList(Items.EMERALD), 16, 1, 50),
                new ItemsForItemsTrade(ModItems.BLOCK_INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25)
            ));
        }        
    }
}