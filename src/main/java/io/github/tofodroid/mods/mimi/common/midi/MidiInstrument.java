package io.github.tofodroid.mods.mimi.common.midi;

public enum MidiInstrument {
    PIANO       (0,0,true),
    ORGAN       (1,19,true), //TODO CHANGE TO BLOCK
    ACGUITAR    (2,25,false),
    ELECGUITAR  (3,30,false),
    BASSGUITAR  (4,34,false),
    VIOLIN      (5,48,false),
    MICROPHONE  (6,53,false),
    TRUMPET     (7,57,false),
    FRENCHHORN  (8,60,false),
    SAXOPHONE   (9,64,false),
    CLARINET    (10,71,false),
    FLUTE       (11,73,false),
    OCARINA     (12,79,false),
    SYNLEAD     (13,80,false),
    KALIMBA     (14,108,false),
    BAGPIPE     (15,109,false),
    STEELDRUM   (17,114,true), //TODO CHANGE TO BLOCK
    DRUMS       (18,15360,0,true);

    private final Byte id;
    private final Integer bank;
    private final Integer patch;
    private final Boolean isBlock;

    MidiInstrument(final Integer id, final Integer bank, final Integer patch, final Boolean block) {
        this.id = id.byteValue();
        this.bank = bank;
        this.patch = patch;
        this.isBlock = block;
    }

    MidiInstrument(final Integer id, final Integer patch, final Boolean block) {
        this.id = id.byteValue();
        this.bank = 0;
        this.patch = patch;
        this.isBlock = block;
    }

    public Byte getId() { return id; }
    public Integer getBank() { return bank; }
    public Integer getPatch() { return patch; }
    public Boolean isBlock() { return isBlock; }

    public static MidiInstrument getBydId(byte id) {
        for(MidiInstrument i : values()) {
            if(i.getId().equals(id)) return i;
        }
        return null;
    }
}

