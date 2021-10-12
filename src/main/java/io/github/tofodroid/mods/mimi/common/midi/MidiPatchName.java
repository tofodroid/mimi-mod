package io.github.tofodroid.mods.mimi.common.midi;

import java.util.Arrays;
import java.util.Optional;
import java.util.function.Function;

// * - Denotes a patch that is used by an in-game instrument
// + - Denotes a patch that is on the short-list to add next
// ^ - Denotes a patch that is going to be added next
// ^N - Denotes a patch that is going to be added next as a sub

public enum MidiPatchName {
	AcousticGrandPiano(0, "Acoustic Grand Piano"), // *
    BrightAcousticPiano(1, "Bright Acoustic Piano"), // *
    ElectricGrandPiano(2, "Electric Grand Piano"), // *
    HonkyTonkPiano(3, "Honky-tonk Piano"), // *
    ElectricPiano1(4, "Electric Piano 1"),
    ElectricPiano2(5, "Electric Piano 2"),
    Harpsichord(6, "Harpsichord"),
    Clavi(7, "Clavi"),
    Celesta(8, "Celesta"),
    Glockenspiel(9, "Glockenspiel"),
    MusicBox(10, "Music Box"), // +
    Vibraphone(11, "Vibraphone"),
    Marimba(12, "Marimba"),
    Xylophone(13, "Xylophone"), // +
    TubularBells(14, "Tubular Bells"),
    Dulcimer(15, "Dulcimer"),
    DrawbarOrgan(16, "Drawbar Organ"), // *
    PercussiveOrgan(17, "Percussive Organ"), // *
    RockOrgan(18, "Rock Organ"), // *
    ChurchOrgan(19, "Church Organ"), // *
    ReedOrgan(20, "Reed Organ"), // *
    Accordion(21, "Accordion"), // * 
    Harmonica(22, "Harmonica"), // *
    TangoAccordion(23, "Tango Accordion"),
    AcousticGuitarNylon(24, "Acoustic Guitar (nylon)"), // *
    AcousticGuitarSteel(25, "Acoustic Guitar (steel)"), // *
    ElectricGuitarJazz(26, "Electric Guitar (jazz)"),
    ElectricGuitarClean(27, "Electric Guitar (clean)"),
    ElectricGuitarMuted(28, "Electric Guitar (muted)"),
    OverdrivenGuitar(29, "Overdriven Guitar"),
    DistortionGuitar(30, "Distortion Guitar"), // *
    Guitarharmonics(31, "Guitar harmonics"),
    AcousticBass(32, "Acoustic Bass"),  // *
    ElectricBassFinger(33, "Electric Bass (finger)"), // ^34
    ElectricBassPick(34, "Electric Bass (pick)"), // *
    FretlessBass(35, "Fretless Bass"), // ^34
    SlapBass1(36, "Slap Bass 1"),
    SlapBass2(37, "Slap Bass 2"),
    SynthBass1(38, "Synth Bass 1"),
    SynthBass2(39, "Synth Bass 2"),
    Violin(40, "Violin"),
    Viola(41, "Viola"),
    Cello(42, "Cello"),
    Contrabass(43, "Contrabass"),
    TremoloStrings(44, "Tremolo Strings"),
    PizzicatoStrings(45, "Pizzicato Strings"),
    OrchestralHarp(46, "Orchestral Harp"),
    Timpani(47, "Timpani"), // + 
    StringEnsemble1(48, "String Ensemble 1"), // *
    StringEnsemble2(49, "String Ensemble 2"),
    SynthStrings1(50, "SynthStrings 1"),
    SynthStrings2(51, "SynthStrings 2"),
    ChoirAahs(52, "Choir Aahs"), // *
    VoiceOohs(53, "Voice Oohs"), // *
    SynthVoice(54, "Synth Voice"), // *
    OrchestraHit(55, "Orchestra Hit"),
    Trumpet(56, "Trumpet"), // *
    Trombone(57, "Trombone"),
    Tuba(58, "Tuba"),
    MutedTrumpet(59, "Muted Trumpet"),
    FrenchHorn(60, "French Horn"), // *
    BrassSection(61, "Brass Section"),
    SynthBrass1(62, "SynthBrass 1"),
    SynthBrass2(63, "SynthBrass 2"),
    SopranoSax(64, "Soprano Sax"), // *
    AltoSax(65, "Alto Sax"), // *
    TenorSax(66, "Tenor Sax"), // *
    BaritoneSax(67, "Baritone Sax"), // *
    Oboe(68, "Oboe"), // *
    EnglishHorn(69, "English Horn"),
    Bassoon(70, "Bassoon"),
    Clarinet(71, "Clarinet"), // *
    Piccolo(72, "Piccolo"),
    Flute(73, "Flute"), // *
    Recorder(74, "Recorder"),
    PanFlute(75, "Pan Flute"),
    BlownBottle(76, "Blown Bottle"),
    Shakuhachi(77, "Shakuhachi"),
    Whistle(78, "Whistle"),
    Ocarina(79, "Ocarina"), // *
    Lead1Square(80, "Lead 1 (square)"), // *
    Lead2Sawtooth(81, "Lead 2 (sawtooth)"),
    Lead3Calliope(82, "Lead 3 (calliope)"),
    Lead4Chiff(83, "Lead 4 (chiff)"),
    Lead5Charang(84, "Lead 5 (charang)"),
    Lead6Voice(85, "Lead 6 (voice)"),
    Lead7Fifths(86, "Lead 7 (fifths)"),
    Lead8BassLead(87, "Lead 8 (bass + lead)"),
    Pad1NewAge(88, "Pad 1 (new age)"),
    Pad2Warm(89, "Pad 2 (warm)"),
    Pad3Polysynth(90, "Pad 3 (polysynth)"),
    Pad4Choir(91, "Pad 4 (choir)"),
    Pad5Bowed(92, "Pad 5 (bowed)"),
    Pad6Metallic(93, "Pad 6 (metallic)"),
    Pad7Halo(94, "Pad 7 (halo)"),
    Pad8Sweep(95, "Pad 8 (sweep)"),
    FX1Rain(96, "FX 1 (rain)"),
    FX2Soundtrack(97, "FX 2 (soundtrack)"), // ^
    FX3Crystal(98, "FX 3 (crystal)"),
    FX4Atmosphere(99, "FX 4 (atmosphere)"),
    FX5Brightness(100, "FX 5 (brightness)"),
    FX6Goblins(101, "FX 6 (goblins)"), // ^
    FX7Echoes(102, "FX 7 (echoes)"),
    FX8SciFi(103, "FX 8 (sci-fi)"),
    Sitar(104, "Sitar"),
    Banjo(105, "Banjo"), // *
    Shamisen(106, "Shamisen"),
    Koto(107, "Koto"),
    Kalimba(108, "Kalimba"), // *
    Bagpipe(109, "Bag pipe"), // *
    Fiddle(110, "Fiddle"),
    Shanai(111, "Shanai"),
    TinkleBell(112, "Tinkle Bell"),  // ^
    Agogo(113, "Agogo"),
    SteelDrums(114, "Steel Drums"), // +
    Woodblock(115, "Woodblock"),
    TaikoDrum(116, "Taiko Drum"),
    MelodicTom(117, "Melodic Tom"),
    SynthDrum(118, "Synth Drum"),
    ReverseCymbal(119, "Reverse Cymbal"),
    GuitarFretNoise(120, "Guitar Fret Noise"),
    BreathNoise(121, "Breath Noise"),
    Seashore(122, "Seashore"),
    BirdTweet(123, "Bird Tweet"),
    TelephoneRing(124, "Telephone Ring"),
    Helicopter(125, "Helicopter"),
    Applause(126, "Applause"),
    Gunshot(127, "Gunshot"),
    UNKNOWN(-1, "Unknown Instrument");

    public final String name;
    public final Integer patch;

    MidiPatchName(Integer patch, String name) {
        this.name = name;
        this.patch = patch;
    }

    public static MidiPatchName getForPatch(Integer patch) {
        return Arrays.asList(MidiPatchName.values()).stream().filter(p -> p.patch.equals(patch)).map(Optional::ofNullable).findFirst().flatMap(Function.identity()).orElse(UNKNOWN);
    }
}
