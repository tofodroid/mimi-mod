package io.github.tofodroid.mods.mimi.common.world;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.LegacySinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraftforge.event.server.ServerAboutToStartEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ModStructures {
    private static final ResourceLocation desertHouse = new ResourceLocation("mimi", "village/desert/houses/instrumentalist_house");
    private static final ResourceLocation plainsHouse = new ResourceLocation("mimi", "village/plains/houses/instrumentalist_house");
    private static final ResourceLocation savannaHouse = new ResourceLocation("mimi", "village/savanna/houses/instrumentalist_house");
    private static final ResourceLocation snowyHouse = new ResourceLocation("mimi", "village/snowy/houses/instrumentalist_house");
    private static final ResourceLocation taigaHouse = new ResourceLocation("mimi", "village/taiga/houses/instrumentalist_house");
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(Registries.PROCESSOR_LIST,
            new ResourceLocation("minecraft", "empty"));


	@SubscribeEvent
	public static void serverAboutToStartEvent(final ServerAboutToStartEvent event) {
		MIMIMod.LOGGER.error("EVENT!");
		ModStructures.registerVillageStructures(event);
	}
    
	public static void registerVillageStructures(ServerAboutToStartEvent event) {
		Registry<StructureProcessorList> structureProcessorList = event.getServer().registryAccess().registry(Registries.PROCESSOR_LIST).orElseThrow();
		Registry<StructureTemplatePool> structureTemplatePool = event.getServer().registryAccess().registry(Registries.TEMPLATE_POOL).orElseThrow();

		addPieceToPool(structureProcessorList, structureTemplatePool, desertHouse, new ResourceLocation("minecraft:village/desert/houses"), 1);
		addPieceToPool(structureProcessorList, structureTemplatePool, plainsHouse, new ResourceLocation("minecraft:village/plains/houses"), 1);
		addPieceToPool(structureProcessorList, structureTemplatePool, savannaHouse, new ResourceLocation("minecraft:village/savanna/houses"), 1);
		addPieceToPool(structureProcessorList, structureTemplatePool, snowyHouse, new ResourceLocation("minecraft:village/snowy/houses"), 1);
		addPieceToPool(structureProcessorList, structureTemplatePool, taigaHouse, new ResourceLocation("minecraft:village/taiga/houses"), 1);
	}
    
	public static void addPieceToPool(Registry<StructureProcessorList> structureProcessorList, Registry<StructureTemplatePool> structureTemplatePool, ResourceLocation sourcePiece, ResourceLocation targetPool, int weight) {
		LegacySinglePoolElement singlePoolElement = SinglePoolElement.legacy(sourcePiece.toString(), structureProcessorList.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY)).apply(StructureTemplatePool.Projection.RIGID);
		StructureTemplatePool modPool = structureTemplatePool.get(targetPool);

		if (modPool != null) {
			List<Pair<StructurePoolElement, Integer>> list = new ArrayList<>(modPool.rawTemplates);

			if(!list.stream().anyMatch(p -> p.getFirst().toString().equals(singlePoolElement.toString()))) {
				for (int i = 0; i < weight; i++) {
					modPool.templates.add(singlePoolElement);
				}

				list.add(new Pair<>(singlePoolElement, weight));
				modPool.rawTemplates = list;
			}
		}
	}
}
