package io.github.tofodroid.mods.mimi.common.entity;

import java.util.List;

import com.mojang.math.Vector3d;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

public class EntitySeat extends Entity {
    private BlockPos source;

    public EntitySeat(Level world) {
        super(ModEntities.SEAT, world);
        this.noPhysics = true;
    }

    private EntitySeat(Level world, BlockPos source, Vector3d offset) {
        this(world);
        this.source = source;
        this.setPos(source.getX() + offset.x, source.getY() + offset.y, source.getZ() + offset.z);
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if(source == null) {
            source = this.getOnPos();
        }

        if(!this.level.isClientSide) {
            if(this.getPassengers().isEmpty() || this.level.isEmptyBlock(source) || this.getPassengers().stream().allMatch(e -> !e.isAddedToWorld() || !e.isAlive())) {
                this.ejectPassengers();
                this.remove(RemovalReason.DISCARDED);
                level.updateNeighbourForOutputSignal(getOnPos(), level.getBlockState(getOnPos()).getBlock());
            }
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
    
    public static InteractionResult create(Level world, BlockPos pos, Vector3d sitOffsetPos, Player player) {
        if(!world.isClientSide) {
            EntitySeat newSeat = new EntitySeat(world, pos, sitOffsetPos);
            
            if(!seatExists(world, newSeat.getX(), newSeat.getY(), newSeat.getZ())) {
                world.addFreshEntity(newSeat);
                player.startRiding(newSeat, false);
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    protected void defineSynchedData() { }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) { }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) { }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}