package io.github.tofodroid.mods.mimi.common.entity;

import java.util.List;

import org.joml.Vector3d;

import io.github.tofodroid.mods.mimi.common.tile.TileInstrument;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;

public class EntitySeat extends Entity {
    protected BlockPos source;

    public EntitySeat(EntityType<? extends EntitySeat> type, Level world) {
        super(type, world);
        this.noPhysics = true;
    }

    private EntitySeat(Level world, BlockPos source, Vector3d offset) {
        this(ModEntities.SEAT.get(), world);
        this.source = source;
        this.setPos(source.getX() + offset.x, source.getY() + offset.y, source.getZ() + offset.z);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }
    
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        LivingEntity livingentity1;
        if (entity instanceof LivingEntity livingentity) {
            livingentity1 = livingentity;
        } else {
            livingentity1 = null;
        }

        return livingentity1;
    }

    @Override
    @SuppressWarnings("resource")
    public void tick() {
        super.tick();

        if(!this.level().isClientSide) {
            BlockEntity blockEntity = this.level().getBlockEntity(this.source);
            if(source == null || this.getPassengers().isEmpty() || blockEntity == null || !(blockEntity instanceof TileInstrument) || this.getPassengers().stream().allMatch(e -> !e.isAddedToWorld() || !e.isAlive() || !(e instanceof Player))) {
                this.ejectPassengers();

                if(blockEntity != null && blockEntity instanceof TileInstrument) {
                    ((TileInstrument)blockEntity).setCurrentPlayer(null);
                }

                this.discard();
                this.level().updateNeighbourForOutputSignal(source, this.level().getBlockState(source).getBlock());
            }
        }

        if(source == null) {
            this.source = this.blockPosition();
        }
    }

    @Override
    public double getPassengersRidingOffset() {
        return -0.2;
    }
    
    public BlockPos getSource() {
        return source;
    }

    public static Boolean seatExists(Level world, BlockPos pos, Vector3d sitOffsetPos) {
        EntitySeat newSeat = new EntitySeat(world, pos, sitOffsetPos);
        return seatExists(world, newSeat.getX(), newSeat.getY(), newSeat.getZ());
    }

    private static Boolean seatExists(Level world, Double posX, Double posY, Double posZ) {
        List<EntitySeat> seats = world.getEntitiesOfClass(EntitySeat.class, new AABB(posX - 0.05, posY - 0.05, posZ - 0.05, posX + 0.05, posY + 0.05, posZ + 0.05));
        return !seats.isEmpty();
    }
    
    public static Boolean create(Level world, BlockPos pos, Vector3d sitOffsetPos, Player player) {
        if(!world.isClientSide) {
            EntitySeat newSeat = new EntitySeat(world, pos, sitOffsetPos);
            
            if(!seatExists(world, newSeat.getX(), newSeat.getY(), newSeat.getZ())) {
                world.addFreshEntity(newSeat);
                player.startRiding(newSeat, false);
                return true;
            }
        }
        return false;
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