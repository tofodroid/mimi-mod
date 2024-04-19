package io.github.tofodroid.mods.mimi.common.entity;

import java.util.List;

import org.joml.Vector3d;
import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import io.github.tofodroid.mods.mimi.server.events.broadcast.consumer.instrument.EntityInstrumentConsumerEventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public class EntitySeat extends Entity {
    protected BlockPos source;
    protected Player rider;

    public EntitySeat(EntityType<? extends EntitySeat> type, Level world) {
        super(type, world);
        this.noPhysics = true;
    }

    private EntitySeat(Level world, BlockPos source, Vector3d offset, Player rider) {
        this(ModEntities.SEAT, world);
        this.source = source;
        this.setPos(source.getX() + offset.x, source.getY() + offset.y, source.getZ() + offset.z);

        if(rider != null) {
            this.rider = rider;
            rider.startRiding(this);
        }
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    public Player getRider() {
        return this.rider;
    }

    @Override
    @SuppressWarnings("resource")
    public void remove(Entity.RemovalReason p_146834_) {
        if(!this.level().isClientSide) {
            this.ejectPassengers();

            if(this.rider != null) {
                EntityInstrumentConsumerEventHandler.reloadEntityInstrumentConsumers(this.rider);
            }

            this.level().updateNeighbourForOutputSignal(source, this.level().getBlockState(source).getBlock());
        }
        super.remove(p_146834_);
    }

    @Override
    @SuppressWarnings("resource")
    public void tick() {
        super.tick();

        if(!this.level().isClientSide) {
            BlockEntity blockEntity = this.level().getBlockEntity(this.source);
            if(source == null || this.getPassengers().isEmpty() || blockEntity == null || !(blockEntity instanceof TileInstrument) || this.getPassengers().stream().allMatch(e -> !e.isAddedToWorld() || !e.isAlive() || !(e instanceof Player))) {
                this.discard();
            }
        }

        if(source == null) {
            this.source = this.blockPosition();
        }
    }

    @Override
    public float ridingOffset(Entity e) {
        return -0.2f;
    }
    
    public BlockPos getSource() {
        return source;
    }

    public static Boolean seatExists(Level world, BlockPos pos, Vector3d sitOffsetPos) {
        EntitySeat newSeat = new EntitySeat(world, pos, sitOffsetPos, null);
        return seatExists(world, newSeat.getX(), newSeat.getY(), newSeat.getZ());
    }

    public static EntitySeat getExisting(Level world, BlockPos pos, Vector3d sitOffsetPos) {
        EntitySeat newSeat = new EntitySeat(world, pos, sitOffsetPos, null);
        return getExisting(world, newSeat.getX(), newSeat.getY(), newSeat.getZ());
    }

    private static Boolean seatExists(Level world, Double posX, Double posY, Double posZ) {
        List<EntitySeat> seats = world.getEntitiesOfClass(EntitySeat.class, new AABB(posX - 0.05, posY - 0.05, posZ - 0.05, posX + 0.05, posY + 0.05, posZ + 0.05));
        return !seats.isEmpty();
    }

    private static EntitySeat getExisting(Level world, Double posX, Double posY, Double posZ) {
        List<EntitySeat> seats = world.getEntitiesOfClass(EntitySeat.class, new AABB(posX - 0.05, posY - 0.05, posZ - 0.05, posX + 0.05, posY + 0.05, posZ + 0.05));
    
        if(!seats.isEmpty()) {
            return seats.get(0);
        }
        return null;
    }
    
    public static EntitySeat create(Level world, BlockPos pos, Vector3d sitOffsetPos, Player player) {
        if(!world.isClientSide) {
            EntitySeat newSeat = new EntitySeat(world, pos, sitOffsetPos, player);
            
            if(!seatExists(world, newSeat.getX(), newSeat.getY(), newSeat.getZ())) {
                world.addFreshEntity(newSeat);
                player.startRiding(newSeat, false);
                EntityInstrumentConsumerEventHandler.reloadEntityInstrumentConsumers(player);
                return newSeat;
            }
        }
        return null;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        this.source = new BlockPos(tag.getInt("source_x"), tag.getInt("source_y"), tag.getInt("source_z"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        if(this.source != null) {
            tag.putInt("source_x", source.getX());
            tag.putInt("source_y", source.getY());
            tag.putInt("source_z", source.getY());
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }

    @Override
    protected void defineSynchedData() { }
}