package io.github.tofodroid.mods.mimi.common.tile;

import java.util.UUID;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.block.APoweredConfigurableMidiBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class APoweredConfigurableMidiTile extends AConfigurableMidiTile {
    private Integer triggeredTickCount = 0;
    private Integer poweredTickCount = 0;

    public APoweredConfigurableMidiTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, 1);
    }

    protected APoweredConfigurableMidiTile(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public Boolean isBlockValid() {
        return getBlockState().getBlock() instanceof APoweredConfigurableMidiBlock;
    }

    public Boolean isTriggered() {
        return getBlockState().getValue(APoweredConfigurableMidiBlock.TRIGGERED);
    }

    public Boolean isPowered() {
        return getBlockState().getValue(APoweredConfigurableMidiBlock.POWER) > 0;
    }

    public Integer stayPoweredForTicks() {
        return 2;
    };

    public Integer powerAfterTriggerTicks() {
        return 1;
    }

    @SuppressWarnings("null")
    public void setTriggeredAndPower(Boolean triggered, Integer power) {
        this.getLevel().setBlockAndUpdate(
            getBlockPos(), 
            getBlockState()
                .setValue(APoweredConfigurableMidiBlock.TRIGGERED, triggered)
                .setValue(APoweredConfigurableMidiBlock.POWER, power)
        );
        
        for(Direction direction : Direction.values()) {
            getLevel().updateNeighborsAt(getBlockPos().relative(direction), getBlockState().getBlock());
        }
    }

    @Override
    public void execServerTick(ServerLevel world, BlockPos pos, BlockState state, AConfigurableMidiTile self) {
        if(this.isBlockValid()) {
            if(this.isPowered()) {
                if(this.poweredTickCount > this.stayPoweredForTicks()) {
                    this.poweredTickCount = 0;
                    this.setTriggeredAndPower(false, 0);
                } else {
                    this.poweredTickCount++;
                }
            } else if(this.isTriggered()) {
                if(this.triggeredTickCount > this.powerAfterTriggerTicks()) {
                    this.triggeredTickCount = 0;
                    this.setTriggeredAndPower(false, 15);
                } else {
                    this.triggeredTickCount++;
                }
            }
        }
    }
    
    @Override
    @SuppressWarnings("null")
    public Boolean onTrigger(@Nullable UUID sender, @Nullable Byte channel, @Nonnull Byte note, @Nullable Byte instrumentId) {
        if(isBlockValid()) {
            if(isPowered()) {
                this.poweredTickCount = 0;
            } else if(!isTriggered()) {
                this.setTriggeredAndPower(true, 0);
            }
            return true;
        }
        
        return false;
    }
}
