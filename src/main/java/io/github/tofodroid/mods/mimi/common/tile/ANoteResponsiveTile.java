package io.github.tofodroid.mods.mimi.common.tile;

import io.github.tofodroid.mods.mimi.common.entity.EntityNoteResponsiveTile;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntityType;

public abstract class ANoteResponsiveTile extends ATileInventory implements ITickableTileEntity {
    public static final Integer UPDATE_EVERY_TICKS = 8;

    private Integer tickCount = 0;

    public ANoteResponsiveTile(TileEntityType<?> type, Integer inventorySize) {
        super(type, inventorySize);
    }

    @Override
    public void tick() {
        if(tickCount >= UPDATE_EVERY_TICKS) {
            tickCount = 0;
            if(this.hasWorld() && !this.world.isRemote && !this.isRemoved()) {
                if(this.shouldHaveEntity()) {
                    EntityNoteResponsiveTile.create(this.world, this.pos);
                } else {
                    EntityNoteResponsiveTile.remove(this.world, this.pos);
                }
            }
        } else {
            tickCount ++;
        }        
    }

    @Override
    public void remove() {
        super.remove();
        EntityNoteResponsiveTile.remove(this.world, this.pos);
    }

    protected abstract Boolean shouldHaveEntity();
}
