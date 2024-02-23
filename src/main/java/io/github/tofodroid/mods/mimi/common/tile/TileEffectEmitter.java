package io.github.tofodroid.mods.mimi.common.tile;

import com.mojang.math.Vector3d;

import io.github.tofodroid.mods.mimi.common.block.BlockEffectEmitter;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class TileEffectEmitter extends AConfigurableTile {
    public static final String REGISTRY_NAME = "effectemitter";
    public static final String INVERTED_TAG = MidiNbtDataUtils.INVERT_SIGNAL_TAG;
    public static final String SOUND_ID_TAG = "sound";
    public static final String PARTICLE_ID_TAG = "particle";
    public static final String VOLUME_TAG = "volume";
    public static final String PITCH_TAG = "pitch";
    public static final String SIDE_TAG = "side";
    public static final String SPREAD_TAG = "spread";
    public static final String COUNT_TAG = "count";
    public static final String SPEED_X_TAG = "speed_x";
    public static final String SPEED_Y_TAG = "speed_y";
    public static final String SPEED_Z_TAG = "speed_z";
    public static final String PARTICLE_LOOP_TAG = "particle_loop";
    public static final String SOUND_LOOP_TAG = "sound_loop";

    private SoundEvent _sound = null;
    private Boolean _inverted = null;
    private Float _volume = null;
    private Float _pitch = null;
    private ParticleOptions _particle = null;
    private Vector3d _offset = null;
    private Vector3d _speed = null;
    private Integer _spread = null;
    private Integer _count = null;
    private Integer _soundLoop = null;
    private Integer _particleLoop = null;

    private Integer ticksSinceSound = 0;
    private Integer ticksSinceParticle = 0;
    private Boolean wasPowered;
    private Boolean wasInverted;

    public TileEffectEmitter(BlockPos pos, BlockState state) {
        super(ModTiles.EFFECTEMITTER, pos, state, 1);
        wasPowered = state.getValue(BlockEffectEmitter.POWERED);
        wasInverted = state.getValue(BlockEffectEmitter.INVERTED);
    }

    @Override
    protected void onSourceStackChanged() {
        this._inverted = null;
        this._sound = null;
        this._volume = null;
        this._pitch = null;
        this._particle = null;
        this._offset = null;
        this._speed = null;
        this._spread = null;
        this._count = null;
        this._soundLoop = null;
        this._particleLoop = null;
        this.ticksSinceSound = 0;
        this.ticksSinceParticle = 0;
        this.updateBlockstate();
        this.setChanged();
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        
        if(this.level != null && this.getBlockState() != null) {
            if(this.level.isClientSide) {
                // Client-side trigger
                if(this.wasPowered != this.getBlockState().getValue(BlockEffectEmitter.POWERED) || this.wasInverted != this.getBlockState().getValue(BlockEffectEmitter.INVERTED)) {
                    this.wasPowered = this.getBlockState().getValue(BlockEffectEmitter.POWERED);
                    this.wasInverted = this.getBlockState().getValue(BlockEffectEmitter.INVERTED);

                    if(this.wasPowered != this.isInverted()) {
                        this.playParticleLocal();
                        this.playSoundLocal();
                    }
                }
            } else {
                // Server-side state
                this.updateBlockstate();
            }
        }
    }

    public void updateBlockstate() {
        if(this.level != null && this.getBlockState() != null && !this.level.isClientSide) {
            BlockState current = this.getBlockState();

            if(this.isInverted() != current.getValue(BlockEffectEmitter.INVERTED)) {
                this.level.setBlock(this.getBlockPos(), current.cycle(BlockEffectEmitter.INVERTED), 2);
            }
        }
    }

    public static void doTick(Level world, BlockPos pos, BlockState state, TileEffectEmitter self) {
        self.tick(world, pos, state);
    }

    public void tick(Level world, BlockPos pos, BlockState state) {
        if(world.isClientSide && !this.isRemoved() && world.isLoaded(pos)) {
            Boolean powered = state.getValue(BlockEffectEmitter.POWERED);

            if(powered != this.isInverted()) {
                if(this.getSoundLoopTicks() > 0) {
                    this.ticksSinceSound++;

                    if(this.ticksSinceSound >= this.getSoundLoopTicks()) {
                        this.playSoundLocal();
                        this.ticksSinceSound = 0;
                    }
                }

                if(this.getParticleLoopTicks() > 0) {
                    this.ticksSinceParticle++;

                    if(this.ticksSinceParticle >= this.getParticleLoopTicks()) {
                        this.playParticleLocal();
                        this.ticksSinceParticle = 0;
                    }
                }
            }
        }
    }

    public Boolean isInverted() {
        if(this._inverted == null) {
            this._inverted = TagUtils.getBooleanOrDefault(getSourceStack(), INVERTED_TAG, false);
        }
        return this._inverted;
    }

    public ParticleOptions getParticle() { 
        if(this._particle == null) {
            String particleStr = TagUtils.getStringOrDefault(getSourceStack(), PARTICLE_ID_TAG, null);

            if(particleStr != null && !particleStr.isBlank()) {
                Boolean particleValid = false;
                ParticleOptions options = null;

                try {
                    options = (ParticleOptions)this.level.registryAccess().registry(Registry.PARTICLE_TYPE_REGISTRY).get().get(new ResourceLocation(particleStr));
                    particleValid = options != null;
                } catch(Exception e) { /* No-op */ }

                if(particleValid) {
                    this._particle = options;
                }
            }
        }
        return this._particle;
    }

    public SoundEvent getSound() {
        if(this._sound == null) {
            String soundStr = TagUtils.getStringOrDefault(getSourceStack(), SOUND_ID_TAG, null);

            if(soundStr != null && !soundStr.isBlank()) {
                Boolean soundValid = false;

                try {
                    soundValid = this.level.registryAccess().registry(Registry.SOUND_EVENT_REGISTRY).get().containsKey(new ResourceLocation(soundStr));
                } catch(Exception e) { /* No-op */ }

                if(soundValid) {
                    this._sound = new SoundEvent(new ResourceLocation(soundStr));
                }
            }
        }

        return this._sound;
    }

    public Float getVolume() {
        if(this._volume == null) {
            Byte volByte = TagUtils.getByteOrDefault(getSourceStack(), VOLUME_TAG, 5);
            this._volume = 5 * (volByte / 10.f);
        }
        return this._volume;
    }

    public Float getPitch() {
        if(this._pitch == null) {
            Byte pitchByte = TagUtils.getByteOrDefault(getSourceStack(), PITCH_TAG, 0);
            this._pitch = 1.0f + pitchByte/4.0f;
        }
        return this._pitch;
    }

    public Vector3d getSpeed() {
        if(this._speed == null) {
            this._speed = new Vector3d(
                TagUtils.getByteOrDefault(getSourceStack(), SPEED_X_TAG, 0)/40.0f,
                TagUtils.getByteOrDefault(getSourceStack(), SPEED_Y_TAG, 0)/40.0f,
                TagUtils.getByteOrDefault(getSourceStack(), SPEED_Z_TAG, 0)/40.0f
            );
        }
        return this._speed;
    }

    public Vector3d getOffset() {
        if(this._offset == null) {
            Byte sideByte = TagUtils.getByteOrDefault(getSourceStack(), SIDE_TAG, 0);
            
            switch(sideByte) {
                case 0:
                default:
                    // Top
                    this._offset = new Vector3d(0.5d, 1.1d, 0.5d);
                    break;
                case 1:
                    this._offset = new Vector3d(0.5d,-0.1d, 0.5d);
                    // Bottom
                    break;
                case 2:
                    this._offset = new Vector3d(0.5d, 0.5d, -0.1d);
                    // North
                    break;
                case 3:
                    this._offset = new Vector3d(1.1d, 0.5d, 0.5d);
                    // East
                    break;
                case 4:
                    this._offset = new Vector3d(0.5d, 0.5d, 1.1d);
                    // South
                    break;
                case 5:
                    this._offset = new Vector3d(-0.1d, 0.5d, 0.5d);
                    // West
                    break;
            }
        }
        return this._offset;
    }

    public Integer getSpread() {
        if(this._spread == null) {
            Byte spreadByte = TagUtils.getByteOrDefault(getSourceStack(), SPREAD_TAG, 0);
            this._spread = spreadByte.intValue();
        }
        return this._spread;
    }

    public Integer getCount() {
        if(this._count == null) {
            Byte countByte = TagUtils.getByteOrDefault(getSourceStack(), COUNT_TAG, 1);
            this._count = countByte.intValue();
        }
        return this._count;
    }

    public Integer getSoundLoopTicks() {
        if(this._soundLoop == null) {
            this._soundLoop = TagUtils.getIntOrDefault(getSourceStack(), SOUND_LOOP_TAG, 0);
        }
        return this._soundLoop;
    }

    public Integer getParticleLoopTicks() {
        if(this._particleLoop == null) {
            this._particleLoop = TagUtils.getIntOrDefault(getSourceStack(), PARTICLE_LOOP_TAG, 0);
        }
        return this._particleLoop;
    }

    public void playSoundLocal() {
        SoundEvent sound = this.getSound();

        if(sound != null && this.level.isClientSide) {
            this.getLevel().playLocalSound(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), sound, SoundSource.BLOCKS, this.getVolume(), this.getPitch(), false);
        }
    }

    public void playParticleLocal() {
        ParticleOptions particle = this.getParticle();

        if(particle != null && this.level.isClientSide) {
            for(int i = 0; i < this.getCount(); i++) {
                this.getLevel().addAlwaysVisibleParticle(
                    particle,
                    getParticlePositionDim(this.getBlockPos().getX(), this.getOffset().x, this.getSpread(), this.level.getRandom()),
                    getParticlePositionDim(this.getBlockPos().getY(), this.getOffset().y, this.getSpread(), this.level.getRandom()),
                    getParticlePositionDim(this.getBlockPos().getZ(), this.getOffset().z, this.getSpread(), this.level.getRandom()),
                    this.getSpeed().x,
                    this.getSpeed().y,
                    this.getSpeed().z
                );
            }
        }
    }

    protected Double getParticlePositionDim(Integer blockPosDim, Double sideOffsetDim, Integer spread, RandomSource random) {
        if(sideOffsetDim <= 0 || sideOffsetDim >= 1) {
            return blockPosDim + sideOffsetDim;
        } else {
            return blockPosDim + sideOffsetDim + (random.nextBoolean() ? 1.0d : -1.0d) * random.nextIntBetweenInclusive(0, spread)/10.0d;
        }
    }
}