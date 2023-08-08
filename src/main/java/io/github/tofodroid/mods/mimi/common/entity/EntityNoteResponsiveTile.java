package io.github.tofodroid.mods.mimi.common.entity;

import java.util.List;

import io.github.tofodroid.mods.mimi.common.tile.INoteResponsiveTile;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkHooks;

public class EntityNoteResponsiveTile extends Entity {
    public BlockPos source;

    public EntityNoteResponsiveTile(EntityType<? extends EntityNoteResponsiveTile> type, Level world) {
        super(ModEntities.NOTERESPONSIVETILE.get(), world);
        this.noPhysics = true;
    }

    private EntityNoteResponsiveTile(Level world, BlockPos pos) {
        this(ModEntities.NOTERESPONSIVETILE.get(), world);
        this.source = pos;
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    public boolean canBeCollidedWith() {
        return false;
    }

    @Override
    @SuppressWarnings("resource")
    public void tick() {
        super.tick();

        if(!this.level().isClientSide && !this.isRemoved()) {
            if(source == null || this.level().getBlockEntity(this.source) == null || !(this.level().getBlockEntity(this.source) instanceof INoteResponsiveTile)) {
                this.remove(RemovalReason.DISCARDED);
            }
        }

        if(source == null) {
            this.source = this.blockPosition();
        }
    }

    @SuppressWarnings("unchecked")
    public INoteResponsiveTile<? extends BlockEntity> getTile() {
        BlockEntity tile = this.isAddedToWorld() && this.isAlive() ? this.level().getBlockEntity(this.source) : null;
        return tile != null && tile instanceof INoteResponsiveTile ? (INoteResponsiveTile<? extends BlockEntity>) tile : null;
    }
    
    protected static EntityNoteResponsiveTile getAtPos(Level world, Double posX, Double posY, Double posZ) {
        List<EntityNoteResponsiveTile> entities = world.getEntitiesOfClass(EntityNoteResponsiveTile.class, new AABB(posX - 0.05, posY - 0.05, posZ - 0.05, posX + 0.05, posY + 0.05, posZ + 0.05));
        return !entities.isEmpty() ? entities.get(0) : null;
    }

    public static Boolean entityExists(Level world, Double posX, Double posY, Double posZ) {
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
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}