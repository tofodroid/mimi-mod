package io.github.tofodroid.mods.mimi.server.events.broadcast;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentHandheld;
import io.github.tofodroid.mods.mimi.common.tile.TileMechanicalMaestro;
import io.github.tofodroid.mods.mimi.common.tile.TileReceiver;
import io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.ABroadcastConsumer;
import io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.BroadcastConsumerHolder;
import io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.InstrumentBroadcastConsumer;
import io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.ReceiverBroadcastConsumer;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class BroadcastManagerConsumerEventHooks {
    private static final List<InteractionHand> ENTITY_INSTRUMENT_ITER = Collections.unmodifiableList(
        Arrays.asList(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND, null)
    );

    public static void reloadMechanicalMaestroInstrumentConsumers(TileMechanicalMaestro tile) {
        if(tile == null || tile.getLevel() == null || !(tile.getLevel() instanceof ServerLevel)) {
            return;
        }

        BroadcastConsumerHolder holder = new BroadcastConsumerHolder(tile.getUUID());

        for(int i = 0; i < tile.getInstrumentStacks().size(); i++) {
            ItemStack instrumentStack = tile.getInstrumentStacks().get(i);
            if(instrumentStack != null && MidiNbtDataUtils.getMidiSource(instrumentStack) != null) {
                holder.putConsumer(i, new InstrumentBroadcastConsumer(
                    tile.getBlockPos(),
                    tile.getLevel().dimension(),
                    tile.getUUID(),
                    instrumentStack,
                    null
                ));
            }
        }

        BroadcastManager.registerConsumers(holder);
    }

    public static void reloadEntityInstrumentConsumers(LivingEntity entity) {
        if(entity == null || entity.level() == null || !(entity.level() instanceof ServerLevel)) {
            return;
        }

        BroadcastConsumerHolder holder = new BroadcastConsumerHolder(entity.getUUID());

        for(int i = 0; i < ENTITY_INSTRUMENT_ITER.size(); i++) {
            InteractionHand hand = ENTITY_INSTRUMENT_ITER.get(i);
            ItemStack instrumentStack = hand != null ? 
                ItemInstrumentHandheld.getEntityHeldInstrumentStack(entity, hand) : 
                BlockInstrument.getTileInstrumentStackForEntity(entity);
            
            if(instrumentStack != null && MidiNbtDataUtils.getMidiSource(instrumentStack) != null) {
                holder.putConsumer(i, new InstrumentBroadcastConsumer(
                    entity::getOnPos,
                    () -> entity.level().dimension(),
                    entity.getUUID(),
                    instrumentStack,
                    hand
                ));
            }
        }

        BroadcastManager.registerConsumers(holder);
    }

    public static void reloadReceiverTileConsumer(TileReceiver tile) { 
        if(tile == null || tile.getLevel() == null || !(tile.getLevel() instanceof ServerLevel)) {
            return;
        }

        BroadcastConsumerHolder holder = new BroadcastConsumerHolder(tile.getUUID());

        if(MidiNbtDataUtils.getMidiSource(tile.getSourceStack()) != null) {
            holder.putConsumer(0, new ReceiverBroadcastConsumer(tile));
        }
    
        BroadcastManager.registerConsumers(holder);
    }

    public static void onLivingEquipmentChange(ItemStack from, ItemStack to, LivingEntity entity) {
        if(from.getItem() instanceof ItemInstrumentHandheld || to.getItem() instanceof ItemInstrumentHandheld) {
            reloadEntityInstrumentConsumers(entity);
        }
    }

    public static void onLivingDeath(LivingEntity entity) {
        allInstrumentConsumerNotesOff(entity.getUUID());
    }

    public static void onEntityTeleport(Entity entity) {
        if(entity instanceof LivingEntity) {
            allInstrumentConsumerNotesOff(entity.getUUID());
        }
    }

    public static void onEntityChangeDimension(Entity entity) {
        if(entity instanceof LivingEntity) {
            allInstrumentConsumerNotesOff(entity.getUUID());
        }
    }

    public static void onPlayerLoggedIn(ServerPlayer player) {
        reloadEntityInstrumentConsumers(player);
    }

    public static void onPlayerRespawn(ServerPlayer player) {
        reloadEntityInstrumentConsumers(player);
    }

    public static void onPlayerLoggedOut(ServerPlayer player) {
        if(player.level() instanceof ServerLevel) {
            BroadcastManager.removeOwnedBroadcastConsumers(player.getUUID());
        }
    }

    private static void allInstrumentConsumerNotesOff(UUID ownerId) {
        BroadcastConsumerHolder holder = BroadcastManager.getOwnedBroadcastConsumers(ownerId);

        if(holder != null) {
            for(ABroadcastConsumer consumer : holder.getConsumers()) {
                if(consumer instanceof InstrumentBroadcastConsumer) {
                    ((InstrumentBroadcastConsumer)consumer).sendAllNotesOff();
                }
            }
        }
    }
}
