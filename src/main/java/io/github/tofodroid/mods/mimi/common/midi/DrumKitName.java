package io.github.tofodroid.mods.mimi.common.midi;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

public enum DrumKitName {
	Standard(0, "Standard Drum Kit"), // *
	Standard2(0, "Standard Drum Kit"), // *
	Room(8, "Room Drum Kit"), // *
	Room2(9, "Room Drum Kit"), // *
	Power(16, "Power Drum Kit"), // *
	Power2(17, "Power Drum Kit"), // *
	Electronic2(24, "Electronic Drum Kit"),
	TR808(25, "TR-808 Drum Kit"),
	TR8082(26, "TR-808 Drum Kit"),
	Jazz(32, "Jazz Drum Kit"), // *
	Jazz2(33, "Jazz Drum Kit"), // *
	Brush(40, "Brush Drum Kit"), // *
	Brush2(41, "Brush Drum Kit"), // *
	Orchestra(48, "Orchestra Drum Kit"),
	Orchestra2(49, "Orchestra Drum Kit"),
	SoundFX(56, "Sound FX Drum Kit"),
	SoundFX2(57, "Sound FX Drum Kit"),
	CM64(127, "CM-64/CM-32L Drum Kit"),
	CM642(128, "CM-64/CM-32L Drum Kit"),
	UNKNOWN(-1, "Unknown Drum Kit");

    public final String name;
    public final Integer patch;

    DrumKitName(Integer patch, String name) {
        this.name = name;
        this.patch = patch;
    }

    public static DrumKitName getForPatch(Integer patch) {
        return Arrays.asList(DrumKitName.values()).stream().filter(p -> p.patch.equals(patch)).map(Optional::ofNullable).findFirst().flatMap(Function.identity()).orElse(UNKNOWN);
    }
}
