package io.github.tofodroid.mods.mimi.common.midi;

import io.github.tofodroid.mods.mimi.common.block.BlockDrums;
import io.github.tofodroid.mods.mimi.common.block.BlockInstrument;
import io.github.tofodroid.mods.mimi.common.block.BlockPiano;

public enum MidiInstrument {
    PIANO       (0,0, BlockPiano.class),
    //ORGAN       (1,19,null), //TODO CHANGE TO BLOCK
    ACGUITAR    (2,25,null),
    ELECGUITAR  (3,30,null),
    BASSGUITAR  (4,34,null),
    VIOLIN      (5,48,null),
    MICROPHONE  (6,53,null),
    TRUMPET     (7,57,null),
    FRENCHHORN  (8,60,null),
    SAXOPHONE   (9,64,null),
    CLARINET    (10,71,null),
    FLUTE       (11,73,null),
    OCARINA     (12,79,null),
    SYNLEAD     (13,80,null),
    KALIMBA     (14,108,null),
    BAGPIPE     (15,109,null),
    //STEELDRUM   (17,114,null), //TODO CHANGE TO BLOCK
    DRUMS       (18,15360,0,BlockDrums.class);

    private final Byte id;
    private final Integer bank;
    private final Integer patch;
    private final Class<? extends BlockInstrument> blockClass;

    MidiInstrument(final Integer id, final Integer bank, final Integer patch, final Class<? extends BlockInstrument> blockClass) {
        this.id = id.byteValue();
        this.bank = bank;
        this.patch = patch;
        this.blockClass = blockClass;
    }

    MidiInstrument(final Integer id, final Integer patch, final Class<? extends BlockInstrument> blockClass) {
        this.id = id.byteValue();
        this.bank = 0;
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

