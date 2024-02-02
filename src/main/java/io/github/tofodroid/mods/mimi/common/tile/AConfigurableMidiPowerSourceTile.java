package io.github.tofodroid.mods.mimi.common.tile;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import io.github.tofodroid.mods.mimi.common.block.AConfigurableMidiPowerSourceBlock;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public abstract class AConfigurableMidiPowerSourceTile extends AConfigurableMidiNoteResponsiveTile {
    private Integer triggeredTickCount = 0;
    private Integer poweredTickCount = 0;

    public AConfigurableMidiPowerSourceTile(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        this(type, pos, state, 1);
    }

    protected AConfigurableMidiPowerSourceTile(BlockEntityType<?> type, BlockPos pos, BlockState state, Integer inventorySize) {
        super(type, pos, state, inventorySize);
    }

    public Boolean isBlockValid() {
        return getBlockState().getBlock() instanceof AConfigurableMidiPowerSourceBlock;
    }

    public Boolean isTriggered() {
        return getBlockState().getValue(AConfigurableMidiPowerSourceBlock.TRIGGERED);
    }

    public Boolean isPowered() {
        return getBlockState().getValue(AConfigurableMidiPowerSourceBlock.POWERED);
    }

    public Boolean isInverted() {
        return getBlockState().getValue(AConfigurableMidiPowerSourceBlock.INVERTED);
    }

    public Boolean stackIsInverted() {
        return MidiNbtDataUtils.getInvertSignal(getSourceStack());
    }

    public Integer stayPoweredForTicks() {
        return 2;
    };

    public Integer powerAfterTriggerTicks() {
        return 1;
    }

    public void setInverted(Boolean inverted) {
        this.getLevel().setBlockAndUpdate(
            getBlockPos(), 
            getBlockState()
                .setValue(AConfigurableMidiPowerSourceBlock.INVERTED, inverted)
        );
        
        for(Direction direction : Direction.values()) {
            getLevel().updateNeighborsAt(getBlockPos().relative(direction), getBlockState().getBlock());
        }
    }

    public void setTriggeredAndPower(Boolean triggered, Boolean powered) {
        this.getLevel().setBlockAndUpdate(
            getBlockPos(), 
            getBlockState()
                .setValue(AConfigurableMidiPowerSourceBlock.TRIGGERED, triggered)
                .setValue(AConfigurableMidiPowerSourceBlock.POWERED, powered)
        );
        
        for(Direction direction : Direction.values()) {
            getLevel().updateNeighborsAt(getBlockPos().relative(direction), getBlockState().getBlock());
        }
    }

    public static void doTick(Level world, BlockPos pos, BlockState state, AConfigurableMidiPowerSourceTile self) {
        self.tick(world, pos, state);
    }

    @Override
    public void execServerTick(ServerLevel world, BlockPos pos, BlockState state) {
        if(this.isBlockValid()) {
            if(this.stackIsInverted() != this.isInverted()) {
                this.setInverted(this.stackIsInverted());
            }

            if(this.isPowered()) {
                if(this.poweredTickCount > this.stayPoweredForTicks()) {
                    this.poweredTickCount = 0;
                    this.setTriggeredAndPower(false, false);
                } else {
                    this.poweredTickCount++;
                }
            } else if(this.isTriggered()) {
                if(this.triggeredTickCount > this.powerAfterTriggerTicks()) {
                    this.triggeredTickCount = 0;
                    this.setTriggeredAndPower(false, true);
                } else {
                    this.triggeredTickCount++;
                }
            }
        }
    }
    
    @Override
    public Boolean onTrigger(@Nullable Byte channel, @Nonnull Byte note, @Nonnull Byte velocity, @Nullable Byte instrumentId) {
        if(isBlockValid()) {
            if(isPowered()) {
                this.poweredTickCount = 0;
            } else if(!isTriggered()) {
                this.setTriggeredAndPower(true, false);
            }
            return true;
        }
        
        return false;
    }
}
