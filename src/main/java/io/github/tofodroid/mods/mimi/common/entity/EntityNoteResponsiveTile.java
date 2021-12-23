package io.github.tofodroid.mods.mimi.common.entity;

import java.util.List;

import io.github.tofodroid.mods.mimi.common.tile.ANoteResponsiveTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

public class EntityNoteResponsiveTile extends Entity {
    public EntityNoteResponsiveTile(Level world) {
        super(ModEntities.NOTERESPONSIVETILE, world);
        this.noPhysics = true;
    }

    private EntityNoteResponsiveTile(Level world, BlockPos pos) {
        this(world);
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        if(!this.level.isClientSide && !this.isRemoved()) {
            if(this.level.isEmptyBlock(this.getOnPos())) {
                this.remove(RemovalReason.DISCARDED);
            }
        }
    }

    public ANoteResponsiveTile getTile() {
        BlockEntity tile = this.isAddedToWorld() && this.isAlive() ? this.getLevel().getBlockEntity(this.getOnPos()) : null;
        return tile != null && tile instanceof ANoteResponsiveTile ? (ANoteResponsiveTile) tile : null;
    }
    
    protected static EntityNoteResponsiveTile getAtPos(Level world, Double posX, Double posY, Double posZ) {
        List<EntityNoteResponsiveTile> entities = world.getEntitiesOfClass(EntityNoteResponsiveTile.class, new AABB(posX - 0.05, posY - 0.05, posZ - 0.05, posX + 0.05, posY + 0.05, posZ + 0.05));
        return !entities.isEmpty() ? entities.get(0) : null;
    }

    private static Boolean entityExists(Level world, Double posX, Double posY, Double posZ) {
        return getAtPos(world, posX, posY, posZ) != null;
    }
    
    public static Boolean create(Level world, BlockPos pos) {
        if(!world.isClientSide) {
            EntityNoteResponsiveTile newMaestro = new EntityNoteResponsiveTile(world, pos);
            
            if(!entityExists(world, newMaestro.getX(), newMaestro.getY(), newMaestro.getZ())) {
                world.addFreshEntity(newMaestro);
                return true;
            }
        }
        return false;
    }

    public static Boolean remove(Level world, BlockPos pos) {
        if(!world.isClientSide) {
            EntityNoteResponsiveTile entity = getAtPos(world, Double.valueOf(pos.getX()), Double.valueOf(pos.getY()), Double.valueOf(pos.getZ()));
            if(entity != null) {
                entity.remove(RemovalReason.DISCARDED);
                return true;
            }
        }
        return false;
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