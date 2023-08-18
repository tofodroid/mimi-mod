package io.github.tofodroid.mods.mimi.common.mob.villager;

import java.util.Arrays;

import com.google.common.collect.ImmutableSet;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import io.github.tofodroid.mods.mimi.common.block.ModBlocks;
import io.github.tofodroid.mods.mimi.common.item.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.ai.behavior.GiveGiftToHero;
import net.minecraft.world.entity.ai.village.poi.PoiType;

import net.minecraft.world.entity.npc.VillagerProfession;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.village.VillagerTradesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModVillagers {
    // POIs (Workstations)
    public static final DeferredRegister<PoiType> POI_TYPES = DeferredRegister.create(ForgeRegistries.POI_TYPES, MIMIMod.MODID);
    public static final RegistryObject<PoiType> TUNINGTABLE_POI = POI_TYPES.register("tuningtable", () -> new PoiType(ImmutableSet.of(ModBlocks.TUNINGTABLE.get().defaultBlockState()), 1, 1));
    
    // Needed for backwards compat
    public static final RegistryObject<PoiType> INSTRUMENTALIST_POI = POI_TYPES.register("instrumentalist", () -> new PoiType(ImmutableSet.of(), 0, 0));

    // Professions
    public static final DeferredRegister<VillagerProfession> PROFESSIONS = DeferredRegister.create(ForgeRegistries.VILLAGER_PROFESSIONS, MIMIMod.MODID);
    public static final RegistryObject<VillagerProfession> INSTRUMENTALIST = PROFESSIONS.register(
        "instrumentalist", 
        () -> {
            return new VillagerProfession(
                "instrumentalist", 
                holder -> holder.value().equals(TUNINGTABLE_POI.get()), 
                holder -> holder.value().equals(TUNINGTABLE_POI.get()), 
                ImmutableSet.of(Items.NOTE_BLOCK), 
                ImmutableSet.of(), 
                SoundEvents.VILLAGER_WORK_TOOLSMITH
            );
        }
    );

    // Villager Trades

	@SubscribeEvent
    public static void registerTrades(VillagerTradesEvent event) {
        // Instrumentalist
        if(event.getType() == INSTRUMENTALIST.get()) {
            GiveGiftToHero.GIFTS.put(INSTRUMENTALIST.get(), new ResourceLocation(MIMIMod.MODID, "gameplay/hero_of_the_village/instrumentalist_gift"));
            event.getTrades().get(1).addAll(Arrays.asList(
                new ItemsForItemsTrade(Arrays.asList(Items.MUSIC_DISC_CAT,Items.MUSIC_DISC_13), 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(Items.NOTE_BLOCK), 1, Arrays.asList(Items.PAPER, Items.REDSTONE), 8, 16, 20)
            ));

            event.getTrades().get(2).addAll(Arrays.asList(
                new ItemsForItemsTrade(ModItems.INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25)
            ));
            
            event.getTrades().get(3).addAll(Arrays.asList(
                new ItemsForItemsTrade(ModItems.BLOCK_INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(ModItems.FILECASTER), 4, Arrays.asList(Items.EMERALD), 1, 16, 30)
            ));
            
            event.getTrades().get(4).addAll(Arrays.asList(
                new ItemsForItemsTrade(ModItems.INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25),
                new ItemsForItemsTrade(Arrays.asList(ModItems.LISTENER,ModItems.RECEIVER,ModItems.CONDUCTOR,ModItems.MECHANICALMAESTRO), 1, Arrays.asList(Items.EMERALD), 4, 16, 25)
            ));
            
            event.getTrades().get(5).addAll(Arrays.asList(
                new ItemsForItemsTrade(Arrays.asList(Items.DISC_FRAGMENT_5,Items.MUSIC_DISC_PIGSTEP,Items.MUSIC_DISC_STAL,Items.MUSIC_DISC_WAIT,Items.MUSIC_DISC_STRAD,Items.MUSIC_DISC_11,Items.MUSIC_DISC_WARD,Items.MUSIC_DISC_BLOCKS,Items.MUSIC_DISC_CHIRP,Items.MUSIC_DISC_FAR,Items.MUSIC_DISC_MALL,Items.MUSIC_DISC_MELLOHI), 1, Arrays.asList(Items.EMERALD), 16, 1, 50),
                new ItemsForItemsTrade(ModItems.BLOCK_INSTRUMENT_ITEMS, 1, Arrays.asList(Items.EMERALD), 4, 16, 25)
            ));
        }        
    }
}