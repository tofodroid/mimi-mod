package io.github.tofodroid.mods.mimi.common.entity;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = MIMIMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MIMIMod.MODID);
    
    public static final RegistryObject<EntityType<EntitySeat>> SEAT = ENTITY_TYPES.register("seat", () -> EntityType.Builder.of(EntitySeat::new, MobCategory.MISC).sized(0F, 0F).noSummon().fireImmune().build(new ResourceLocation(MIMIMod.MODID, "seat").toString()));
    public static final RegistryObject<EntityType<EntityNoteResponsiveTile>> NOTERESPONSIVETILE = ENTITY_TYPES.register("noteresponsivetile", () -> EntityType.Builder.of(EntityNoteResponsiveTile::new, MobCategory.MISC).sized(0F, 0F).noSummon().fireImmune().build(new ResourceLocation(MIMIMod.MODID, "noteresponsivetile").toString()));
}