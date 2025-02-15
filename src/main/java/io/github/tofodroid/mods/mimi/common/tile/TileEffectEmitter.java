package io.github.tofodroid.mods.mimi.common.tile;

import com.mojang.math.Vector3d;

import io.github.tofodroid.mods.mimi.common.block.BlockEffectEmitter;
import io.github.tofodroid.mods.mimi.util.MidiNbtDataUtils;
import io.github.tofodroid.mods.mimi.util.ResourceUtils;
import io.github.tofodroid.mods.mimi.util.TagUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
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

    private Boolean _inverted = null;

    private SoundEvent _sound = null;
    private Float _volume = null;
    private Float _pitch = null;

    private ParticleOptions _particle = null;
    private Vector3d _offset = null;
    private Vector3d _speed = null;
    private Integer _spread = null;
    private Integer _count = null;
    private Integer _soundLoop = null;
    private Integer _particleLoop = null;

    private Boolean firstTick = true;
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
        this.ticksSinceSound = 0;
        this.ticksSinceParticle = 0;
        this.cacheEffectSettings();
        this.updateBlockstate();
        this.setChanged();
    }

    protected void cacheEffectSettings() {
        this._inverted = TagUtils.getBooleanOrDefault(getSourceStack(), INVERTED_TAG, false);
        this._particle = this.getParticleFromString(TagUtils.getStringOrDefault(getSourceStack(), PARTICLE_ID_TAG, null));
        this._speed = new Vector3d(
            TagUtils.getByteOrDefault(getSourceStack(), SPEED_X_TAG, 0)/40.0f,
            TagUtils.getByteOrDefault(getSourceStack(), SPEED_Y_TAG, 0)/40.0f,
            TagUtils.getByteOrDefault(getSourceStack(), SPEED_Z_TAG, 0)/40.0f
        );
        this._offset = this.getOffsetFromByte(TagUtils.getByteOrDefault(getSourceStack(), SIDE_TAG, 0));
        this._spread = TagUtils.getByteOrDefault(getSourceStack(), SPREAD_TAG, 0).intValue();
        this._count = TagUtils.getByteOrDefault(getSourceStack(), COUNT_TAG, 1).intValue();
        this._soundLoop = TagUtils.getIntOrDefault(getSourceStack(), SOUND_LOOP_TAG, 0);
        this._particleLoop = TagUtils.getIntOrDefault(getSourceStack(), PARTICLE_LOOP_TAG, 0);

        this._sound = this.getSoundFromString(TagUtils.getStringOrDefault(getSourceStack(), SOUND_ID_TAG, null));
        this._volume = 5 * (TagUtils.getByteOrDefault(getSourceStack(), VOLUME_TAG, 5) / 10.f);
        this._pitch = 1.0f + TagUtils.getByteOrDefault(getSourceStack(), PITCH_TAG, 0)/4.0f;
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
        this.cacheEffectSettings();
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
        if(world instanceof ServerLevel) {
            if(firstTick) {
                this.cacheEffectSettings();
                this.firstTick = false;
            }
        }

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
        return this._inverted;
    }

    public ParticleOptions getParticleFromString(String particleStr) {
        if(particleStr != null && !particleStr.isBlank()) {
            Boolean particleValid = false;
            ParticleOptions options = null;

            try {
                options = (ParticleOptions)this.level.registryAccess().registry(Registry.PARTICLE_TYPE_REGISTRY).get().get(ResourceUtils.parseLocation(particleStr));
                particleValid = options != null;
            } catch(Exception e) { /* No-op */ }

            if(particleValid) {
                return options;
            }
        }

        return null;
    }

    public SoundEvent getSoundFromString(String soundStr) {
        if(soundStr != null && !soundStr.isBlank()) {
            Boolean soundValid = false;

            try {
                soundValid = this.level.registryAccess().registry(Registry.SOUND_EVENT_REGISTRY).get().containsKey(ResourceUtils.parseLocation(soundStr));
            } catch(Exception e) { /* No-op */ }

            if(soundValid) {
                return new SoundEvent(ResourceUtils.parseLocation(soundStr));
            }
        }
        return null;
    }

    public ParticleOptions getParticle() { 
        return this._particle;
    }

    public SoundEvent getSound() {
        return this._sound;
    }

    public Float getVolume() {
        return this._volume;
    }

    public Float getPitch() {
        return this._pitch;
    }

    public Vector3d getSpeed() {
        return this._speed;
    }

    public Vector3d getOffsetFromByte(Byte sideByte) {
        switch(sideByte) {
            case 0:
            default:
                // Top
                return new Vector3d(0.5d, 1.1d, 0.5d);
            case 1:
                return new Vector3d(0.5d,-0.1d, 0.5d);
            case 2:
                return new Vector3d(0.5d, 0.5d, -0.1d);
            case 3:
                return new Vector3d(1.1d, 0.5d, 0.5d);
            case 4:
                return new Vector3d(0.5d, 0.5d, 1.1d);
            case 5:
                return new Vector3d(-0.1d, 0.5d, 0.5d);
        }
    }

    public Vector3d getOffset() {
        return this._offset;
    }

    public Integer getSpread() {
        return this._spread;
    }

    public Integer getCount() {
        return this._count;
    }

    public Integer getSoundLoopTicks() {
        return this._soundLoop;
    }

    public Integer getParticleLoopTicks() {
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