package io.github.tofodroid.mods.mimi.client.midi.synth;

import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.ShortMessage;

/**
 * A ShortMessage that can address channels beyond the standard MIDI 0-15 range.
 *
 * AMIMISynth's internal SoftSynthesizer is configured with 64 "midi channels" to allow
 * more simultaneous distinct instruments than the MIDI spec's 16-channel limit, but a
 * plain ShortMessage rejects any channel above 15 in its constructor (InvalidMidiDataException).
 * The vendored SoftReceiver/SoftMainMixer already special-case ShortMessage.getChannel() > 0xF
 * (see SoftReceiver#send and SoftMainMixer#processMessage) and route on the value returned by
 * getChannel() rather than the raw status byte, so overriding getChannel() here is sufficient
 * to reach the extra channels while still producing a byte-valid message via the super
 * constructor (built with the channel masked to its low nibble).
 */
public class ExtendedChannelShortMessage extends ShortMessage {
    private final int extendedChannel;

    public ExtendedChannelShortMessage(int command, int channel, int data1, int data2) throws InvalidMidiDataException {
        super(command, channel & 0xF, data1, data2);
        this.extendedChannel = channel;
    }

    @Override
    public int getChannel() {
        return extendedChannel;
    }

    @Override
    public Object clone() {
        try {
            return new ExtendedChannelShortMessage(getCommand(), extendedChannel, getData1(), getData2());
        } catch (InvalidMidiDataException e) {
            throw new IllegalStateException(e);
        }
    }
}
