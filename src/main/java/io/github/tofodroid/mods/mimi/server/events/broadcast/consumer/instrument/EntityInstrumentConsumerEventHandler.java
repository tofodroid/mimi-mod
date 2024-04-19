package io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.instrument;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.item.ItemInstrumentHandheld;
import io.github.tofodroid.mods.mimi.server.events.broadcast.BroadcastManager;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.BroadcastConsumerInventoryHolder;
import io.github.tofodroid.mods.mimi.server.events.broadcast.api.IBroadcastConsumer;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class EntityInstrumentConsumerEventHandler {
    private static final List<InteractionHand> ENTITY_INSTRUMENT_ITER = Collections.unmodifiableList(
        Arrays.asList(InteractionHand.MAIN_HAND, InteractionHand.OFF_HAND, null)
    );

    @SuppressWarnings("resource")
    public static void reloadEntityInstrumentConsumers(LivingEntity entity) {
        if(entity == null || entity.getLevel() == null || !(entity.getLevel() instanceof ServerLevel)) {
            return;
        }

        BroadcastConsumerInventoryHolder holder = new BroadcastConsumerInventoryHolder(entity.getUUID());

        for(int i = 0; i < ENTITY_INSTRUMENT_ITER.size(); i++) {
            InteractionHand hand = ENTITY_INSTRUMENT_ITER.get(i);
            ItemStack instrumentStack = hand != null ? 
                ItemInstrumentHandheld.getEntityHeldInstrumentStack(entity, hand) : 
                BlockInstrument.getTileInstrumentStackForEntity(entity);
            
            if(instrumentStack != null && MidiNbtDataUtils.getMidiSource(instrumentStack) != null) {
                holder.putConsumer(i, new InstrumentBroadcastConsumer(
                    entity::getOnPos,
                    () -> entity.getLevel().dimension(),
                    entity.getUUID(),
                    instrumentStack,
                    hand
                ));
            }
        }

        BroadcastManager.removeOwnedBroadcastConsumers(holder.getOwnerId());
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
        if(player.getLevel() instanceof ServerLevel) {
            BroadcastManager.removeOwnedBroadcastConsumers(player.getUUID());
        }
    }

    private static void allInstrumentConsumerNotesOff(UUID ownerId) {
        BroadcastConsumerInventoryHolder holder = BroadcastManager.getOwnedBroadcastConsumers(ownerId);

        if(holder != null) {
            for(IBroadcastConsumer consumer : holder.getConsumers()) {
                if(consumer instanceof InstrumentBroadcastConsumer) {
                    ((InstrumentBroadcastConsumer)consumer).sendAllNotesOff();
                }
            }
        }
    }
}
