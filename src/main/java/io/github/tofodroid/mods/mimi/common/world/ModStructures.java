package io.github.tofodroid.mods.mimi.common.world;

import java.util.ArrayList;
import java.util.List;

import com.mojang.datafixers.util.Pair;

import io.github.tofodroid.mods.mimi.common.config.ConfigProxy;
import io.github.tofodroid.mods.mimi.util.ResourceUtils;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructurePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;

public class ModStructures {
    private static final ResourceLocation desertHouse = ResourceUtils.newModLocation("village/desert/houses/instrumentalist_house");
    private static final ResourceLocation plainsHouse = ResourceUtils.newModLocation("village/plains/houses/instrumentalist_house");
    private static final ResourceLocation savannaHouse = ResourceUtils.newModLocation("village/savanna/houses/instrumentalist_house");
    private static final ResourceLocation snowyHouse = ResourceUtils.newModLocation("village/snowy/houses/instrumentalist_house");
    private static final ResourceLocation taigaHouse = ResourceUtils.newModLocation("village/taiga/houses/instrumentalist_house");
    private static final ResourceKey<StructureProcessorList> EMPTY_PROCESSOR_LIST_KEY = ResourceKey.create(Registries.PROCESSOR_LIST, ResourceUtils.newRootLocation("empty"));

	public static void registerVillageStructures(Registry<StructureProcessorList> structureProcessorList, Registry<StructureTemplatePool> structureTemplatePool) {
		if(ConfigProxy.isInstrumentalistShopEnabled()) {
			addBuildingToPool(structureTemplatePool, structureProcessorList, ResourceUtils.newRootLocation("village/desert/houses"), desertHouse.toString(), 2);
			addBuildingToPool(structureTemplatePool, structureProcessorList, ResourceUtils.newRootLocation("village/plains/houses"), plainsHouse.toString(), 2);
			addBuildingToPool(structureTemplatePool, structureProcessorList, ResourceUtils.newRootLocation("village/savanna/houses"), savannaHouse.toString(),2);
			addBuildingToPool(structureTemplatePool, structureProcessorList, ResourceUtils.newRootLocation("village/snowy/houses"), snowyHouse.toString(), 2);
			addBuildingToPool(structureTemplatePool, structureProcessorList, ResourceUtils.newRootLocation("village/taiga/houses"), taigaHouse.toString(), 2);
		}
	}
    
	public static void addBuildingToPool(Registry<StructureTemplatePool> templatePoolRegistry, Registry<StructureProcessorList> processorListRegistry, ResourceLocation poolRL, String nbtPieceRL, int weight) {
		StructureTemplatePool pool = templatePoolRegistry.get(poolRL);
		
		if (pool != null) {
			Holder<StructureProcessorList> processorHolder = processorListRegistry.getHolderOrThrow(EMPTY_PROCESSOR_LIST_KEY);

			SinglePoolElement piece = SinglePoolElement.single(nbtPieceRL, processorHolder).apply(StructureTemplatePool.Projection.RIGID);

			for (int i = 0; i < weight; i++) {
				pool.templates.add(piece);
			}

			List<Pair<StructurePoolElement, Integer>> listOfPieceEntries = new ArrayList<>(pool.rawTemplates);
			listOfPieceEntries.add(new Pair<>(piece, weight));
			pool.rawTemplates = listOfPieceEntries;
		}
	}
}
