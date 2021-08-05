package io.github.tofodroid.mods.mimi.common.entity;

import java.util.List;

import io.github.tofodroid.mods.mimi.common.tile.ANoteResponsiveTile;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntityNoteResponsiveTile extends Entity {
    public EntityNoteResponsiveTile(World world) {
        super(ModEntities.NOTERESPONSIVETILE, world);
        this.noClip = true;
    }

    private EntityNoteResponsiveTile(World world, BlockPos pos) {
        this(world);
        this.setPosition(pos.getX(), pos.getY(), pos.getZ());
    }

    @Override
    @SuppressWarnings("deprecation")
    public void tick() {
        super.tick();
        if(!this.world.isRemote && !this.removed) {
            if(this.world.isAirBlock(this.getPosition())) {
                this.remove();
            }
        }
    }

    @Override
    protected void registerData() {}
    
    @Override
    protected void readAdditional(CompoundNBT compound) {}

    @Override
    protected void writeAdditional(CompoundNBT compound) {}

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public ANoteResponsiveTile getTile() {
        TileEntity tile = this.isAddedToWorld() && this.isAlive() ? this.getEntityWorld().getTileEntity(this.getPosition()) : null;
        return tile != null && tile instanceof ANoteResponsiveTile ? (ANoteResponsiveTile) tile : null;
    }
    
    protected static EntityNoteResponsiveTile getAtPos(World world, Double posX, Double posY, Double posZ) {
        List<EntityNoteResponsiveTile> entities = world.getEntitiesWithinAABB(EntityNoteResponsiveTile.class, new AxisAlignedBB(posX - 0.05, posY - 0.05, posZ - 0.05, posX + 0.05, posY + 0.05, posZ + 0.05));
        return !entities.isEmpty() ? entities.get(0) : null;
    }

    private static Boolean entityExists(World world, Double posX, Double posY, Double posZ) {
        return getAtPos(world, posX, posY, posZ) != null;
    }
    
    public static void create(World world, BlockPos pos) {
        if(!world.isRemote) {
            EntityNoteResponsiveTile newMaestro = new EntityNoteResponsiveTile(world, pos);
            
            if(!entityExists(world, newMaestro.getPosX(), newMaestro.getPosY(), newMaestro.getPosZ())) {
                world.addEntity(newMaestro);
            }
        }
    }

    public static void remove(World world, BlockPos pos) {
        if(!world.isRemote) {
            EntityNoteResponsiveTile entity = getAtPos(world, new Double(pos.getX()), new Double(pos.getY()), new Double(pos.getZ()));
            if(entity != null) {
                entity.remove();
            }
        }
    }
}