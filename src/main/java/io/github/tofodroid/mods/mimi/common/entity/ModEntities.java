package io.github.tofodroid.mods.mimi.common.entity;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
    private static final List<EntityType<?>> ENTITY_TYPES = new ArrayList<>();

    public static final EntityType<EntitySeat> SEAT = buildType(MIMIMod.MODID + ":seat", EntityType.Builder.<EntitySeat>of((type, world) -> new EntitySeat(world), MobCategory.MISC).sized(0.0F, 0.0F).setCustomClientFactory((spawnEntity, world) -> new EntitySeat(world)));
    public static final EntityType<EntityNoteResponsiveTile> NOTERESPONSIVETILE = buildType(MIMIMod.MODID + ":noteresponsivetile", EntityType.Builder.<EntityNoteResponsiveTile>of((type, world) -> new EntityNoteResponsiveTile(world), MobCategory.MISC).sized(0.0F, 0.0F).setCustomClientFactory((spawnEntity, world) -> new EntityNoteResponsiveTile(world)));

    private static <T extends Entity> EntityType<T> buildType(String id, EntityType.Builder<T> builder) {
        EntityType<T> type = builder.fireImmune().noSummon().build(id);
        type.setRegistryName(id);
        ENTITY_TYPES.add(type);
        return type;
    }

    @SubscribeEvent
    public static void registerTypes(final RegistryEvent.Register<EntityType<?>> event) {
        ENTITY_TYPES.forEach(type -> event.getRegistry().register(type));
        ENTITY_TYPES.clear();
    }
}