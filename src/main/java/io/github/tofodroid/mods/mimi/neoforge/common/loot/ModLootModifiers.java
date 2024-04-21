package io.github.tofodroid.mods.mimi.neoforge.common.loot;

import com.mojang.serialization.Codec;

import io.github.tofodroid.mods.mimi.common.MIMIMod;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

public class ModLootModifiers {
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> REGISTER = DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, MIMIMod.MODID);
    public static final DeferredHolder<Codec<? extends IGlobalLootModifier>, Codec<GenericAddDropLootModifier>> GENERIC_ADD_DROP = REGISTER.register("generic_add_drop", () -> GenericAddDropLootModifier.CODEC);
}