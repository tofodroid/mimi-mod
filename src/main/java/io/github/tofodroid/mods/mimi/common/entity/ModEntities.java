package io.github.tofodroid.mods.mimi.common.entity;

import java.util.HashMap;
import java.util.Map;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {
    public static final Map<ResourceLocation, EntityType<?>> ENTITES = new HashMap<>();

    public static final EntityType<EntitySeat> SEAT = create("seat", EntityType.Builder.of(EntitySeat::new, MobCategory.MISC).sized(0F, 0F).noSummon().fireImmune());
    public static final EntityType<EntityNoteResponsiveTile> NOTERESPONSIVETILE = create("noteresponsivetile", EntityType.Builder.of(EntityNoteResponsiveTile::new, MobCategory.MISC).sized(0F, 0F).noSummon().fireImmune());

    public static <T extends Entity> EntityType<T> create(String id, EntityType.Builder<T> builder) {
        ResourceLocation location = new ResourceLocation(MIMIMod.MODID, id);
        EntityType<T> type = builder.build(location.toString());
        ENTITES.put(location, type);
        return type;
    }
}