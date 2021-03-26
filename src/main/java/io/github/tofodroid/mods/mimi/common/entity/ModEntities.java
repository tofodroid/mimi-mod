package io.github.tofodroid.mods.mimi.common.entity;

import java.util.ArrayList;
import java.util.List;

import io.github.tofodroid.mods.mimi.common.MIMIMod;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityClassification;
import net.minecraft.entity.EntityType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
    private static final List<EntityType<?>> ENTITY_TYPES = new ArrayList<>();

    public static final EntityType<EntitySeat> SEAT = buildType(MIMIMod.MODID + ":seat", EntityType.Builder.<EntitySeat>create((type, world) -> new EntitySeat(world), EntityClassification.MISC).size(0.0F, 0.0F).setCustomClientFactory((spawnEntity, world) -> new EntitySeat(world)));

    private static <T extends Entity> EntityType<T> buildType(String id, EntityType.Builder<T> builder) {
        EntityType<T> type = builder.build(id);
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