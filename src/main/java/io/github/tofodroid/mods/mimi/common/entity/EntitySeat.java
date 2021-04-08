package io.github.tofodroid.mods.mimi.common.entity;

import java.util.List;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.IPacket;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraftforge.fml.network.NetworkHooks;

public class EntitySeat extends Entity {
    private BlockPos source;

    public EntitySeat(World world) {
        super(ModEntities.SEAT, world);
        this.noClip = true;
    }

    private EntitySeat(World world, BlockPos source, Vector3d offset) {
        this(world);
        this.source = source;
        this.setPosition(source.getX() + offset.getX(), source.getY() + offset.getY(), source.getZ() + offset.getZ());
    }

    @Override
    public void tick() {
        super.tick();
        if(source == null)
        {
            source = this.getPosition();
        }
        if(!this.world.isRemote)
        {
            if(this.getPassengers().isEmpty() || this.world.isAirBlock(source))
            {
                this.remove();
                world.updateComparatorOutputLevel(getPosition(), world.getBlockState(getPosition()).getBlock());
            }
        }
    }

    @Override
    public double getMountedYOffset() {
        return -0.2;
    }
    
    @Override
    protected void registerData() {}
    
    @Override
    protected void readAdditional(CompoundNBT compound) {}

    @Override
    protected void writeAdditional(CompoundNBT compound) {}

    @Override
    protected boolean canBeRidden(Entity entity)
    {
        return true;
    }

    @Override
    public IPacket<?> createSpawnPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
    
    public BlockPos getSource() {
        return source;
    }

    public static Boolean seatExists(World world, BlockPos pos, Vector3d sitOffsetPos) {
        EntitySeat newSeat = new EntitySeat(world, pos, sitOffsetPos);
        return seatExists(world, newSeat.getPosX(), newSeat.getPosY(), newSeat.getPosZ());
    }

    private static Boolean seatExists(World world, Double posX, Double posY, Double posZ) {
        List<EntitySeat> seats = world.getEntitiesWithinAABB(EntitySeat.class, new AxisAlignedBB(posX - 0.05, posY - 0.05, posZ - 0.05, posX + 0.05, posY + 0.05, posZ + 0.05));
        return !seats.isEmpty();
    }
    
    public static ActionResultType create(World world, BlockPos pos, Vector3d sitOffsetPos, PlayerEntity player) {
        if(!world.isRemote) {
            EntitySeat newSeat = new EntitySeat(world, pos, sitOffsetPos);
            
            if(!seatExists(world, newSeat.getPosX(), newSeat.getPosY(), newSeat.getPosZ())) {
                world.addEntity(newSeat);
                player.startRiding(newSeat, false);
            }
        }
        return ActionResultType.SUCCESS;
    }
}