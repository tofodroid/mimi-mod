package io.github.tofodroid.mods.mimi.common.midi;

import io.github.tofodroid.mods.mimi.common.block.BlockDrums;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.block.BlockPiano;

public enum MidiInstrument {
    PIANO       (0,0, BlockPiano.class),
    BANJO       (1,105),
    ACGUITAR    (2,25),
    ELECGUITAR  (3,30),
    BASSGUITAR  (4,34),
    VIOLIN      (5,48),
    MICROPHONE  (6,53),
    TRUMPET     (7,56),
    FRENCHHORN  (8,60),
    SAXOPHONE   (9,64),
    CLARINET    (10,71),
    FLUTE       (11,73),
    OCARINA     (12,79),
    SYNLEAD     (13,80),
    KALIMBA     (14,108),
    BAGPIPE     (15,109),
    OBOE        (16,68),
    ACCORDION   (17,21),
    DRUMS       (18,15360,0,BlockDrums.class),
    HARMONICA   (19,22);

    private final Byte id;
    private final Integer bank;
    private final Integer patch;
    private final Class<? extends BlockInstrument> blockClass;

    MidiInstrument(final Integer id, final Integer patch) {
        this.id = id.byteValue();
        this.patch = patch;
        this.bank = 0;
        this.blockClass = null;
    }

    MidiInstrument(final Integer id, final Integer patch, final Integer bank) {
        this.id = id.byteValue();
        this.patch = patch;
        this.bank = bank;
        this.blockClass = null;
    }

    MidiInstrument(final Integer id, final Integer patch, final Class<? extends BlockInstrument> blockClass) {
        this.id = id.byteValue();
        this.bank = 0;
        this.patch = patch;
        this.blockClass = blockClass;
    }

    MidiInstrument(final Integer id, final Integer bank, final Integer patch, final Class<? extends BlockInstrument> blockClass) {
        this.id = id.byteValue();
        this.bank = bank;
        this.patch = patch;
        this.blockClass = blockClass;
    }

    public Byte getId() { return id; }
    public Integer getBank() { return bank; }
    public Integer getPatch() { return patch; }
    public Class<? extends BlockInstrument> getBlockClass() { return blockClass; }
    public Boolean isBlock() { return blockClass != null; }

    public static MidiInstrument getBydId(byte id) {
        for(MidiInstrument i : values()) {
            if(i.getId().equals(id)) return i;
        }
        return null;
    }
}

