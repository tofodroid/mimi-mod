/*
* Copyright (c) 2003, 2019, Oracle and/or its affiliates. All rights reserved.
* DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
*
* This code is free software; you can redistribute it and/or modify it
* under the terms of the GNU General Public License version 2 only, as
* published by the Free Software Foundation.  Oracle designates this
* particular file as subject to the "Classpath" exception as provided
* by Oracle in the LICENSE file that accompanied this code.
*
* This code is distributed in the hope that it will be useful, but WITHOUT
* ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
* FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
* version 2 for more details (a copy is included in the LICENSE file that
* accompanied this code).
*
* You should have received a copy of the GNU General Public License version
* 2 along with this work; if not, write to the Free Software Foundation,
* Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
*
* Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
* or visit www.oracle.com if you need additional information or have any
* questions.
*/

package io.github.tofodroid.com.sun.media.sound;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.InvalidMidiDataException;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiEvent;
import javax.sound.midi.MidiMessage;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Sequence;
import javax.sound.midi.Sequencer;
import javax.sound.midi.ShortMessage;
import javax.sound.midi.Track;
import javax.sound.midi.Transmitter;

/**
 * Based on RealTimeSequencer, but with a simplified set of capabilities to reduce overhead
*
* @author Florian Bomers
*/

/* TODO:
* - rename PlayThread to PlayEngine (because isn't a thread)
*/
public final class SimpleThreadSequencer<R extends Receiver> extends AbstractMidiDevice
        implements Sequencer, AutoConnectSequencer {

    /**
     * Event Dispatcher thread. Should be using a shared event
    * dispatcher instance with a factory in EventDispatcher
    */
    private static final Map<ThreadGroup, EventDispatcher> dispatchers =
            new WeakHashMap<>();

    /**
     * All SimpleThreadSequencers share this info object.
    */
    static final MidiDevice.Info info = new SimpleThreadSequencerInfo();


    private static final Sequencer.SyncMode[] masterSyncModes = { Sequencer.SyncMode.INTERNAL_CLOCK };
    private static final Sequencer.SyncMode[] slaveSyncModes  = { Sequencer.SyncMode.NO_SYNC };

    private static final Sequencer.SyncMode masterSyncMode    = Sequencer.SyncMode.INTERNAL_CLOCK;
    private static final Sequencer.SyncMode slaveSyncMode     = Sequencer.SyncMode.NO_SYNC;

    /**
     * Sequence on which this sequencer is operating.
    */
    private Sequence sequence = null;

    // caches

    /**
     * Same for setTempoInMPQ...
    * -1 means not set.
    */
    private double cacheTempoMPQ = -1;

    /**
     * cache value for tempo factor until sequence is set
    * -1 means not set.
    */
    private float cacheTempoFactor = -1;

    /** if a particular track is muted */
    private boolean[] trackMuted = null;
    /** if a particular track is solo */
    private boolean[] trackSolo = null;

    /** tempo cache for getMicrosecondPosition */
    private final MidiUtils.TempoCache tempoCache = new MidiUtils.TempoCache();

    /**
     * True if the sequence is running.
    */
    private volatile boolean running;
    private volatile boolean paused = false;

    /**
     * the thread for pushing out the MIDI messages.
    */
    private PlayThread playThread;

    /**
     * Meta event listeners.
    */
    private final ArrayList<Object> metaEventListeners = new ArrayList<>();

    /**
     * Control change listeners.
    */
    private final ArrayList<ControllerListElement> controllerEventListeners = new ArrayList<>();

    /**
     * the receiver that this device is auto-connected to.
    */
    R autoConnectedReceiver = null;


    /* ****************************** CONSTRUCTOR ****************************** */

    public SimpleThreadSequencer(R autoConnectReceiver){
        super(info);
        this.autoConnectedReceiver = autoConnectReceiver;
    }

    /* ****************************** SEQUENCER METHODS ******************** */

    @Override
    public synchronized void setSequence(Sequence sequence)
        throws InvalidMidiDataException {
        if (sequence != this.sequence) {
            if (this.sequence != null && sequence == null) {
                setCaches();
                stop();
                // initialize some non-cached values
                trackMuted = null;
                trackSolo = null;
                if (getDataPump() != null) {
                    getDataPump().setTickPos(0);
                }
            }

            if (playThread != null) {
                playThread.setSequence(sequence);
            }

            // store this sequence (do not copy - we want to give the possibility
            // of modifying the sequence at runtime)
            this.sequence = sequence;

            if (sequence != null) {
                tempoCache.refresh(sequence);
                // rewind to the beginning
                setTickPosition(0);
                // propagate caches
                propagateCaches();
            }
        }
        else if (sequence != null) {
            tempoCache.refresh(sequence);
            if (playThread != null) {
                playThread.setSequence(sequence);
            }
        }
    }

    @Override
    public synchronized void setSequence(InputStream stream) throws IOException, InvalidMidiDataException {
        if (stream == null) {
            setSequence((Sequence) null);
            return;
        }

        Sequence seq = MidiSystem.getSequence(stream); // can throw IOException, InvalidMidiDataException

        setSequence(seq);
    }

    @Override
    public Sequence getSequence() {
        return sequence;
    }

    @Override
    public synchronized void start() {
        // sequencer not open: throw an exception
        if (!isOpen()) {
            throw new IllegalStateException("sequencer not open");
        }

        // sequence not available: throw an exception
        if (sequence == null) {
            throw new IllegalStateException("sequence not set");
        }

        // already running: return quietly
        if (running == true && paused == false) {
            return;
        }

        // start playback
        implStart();
    }

    public synchronized void pause() {
        if (!isOpen()) {
            throw new IllegalStateException("sequencer not open");
        }

        // not running or already paused; just return
        if (running == false || paused == true) {
            return;
        }

        // stop playback
        playThread.pause();
    }

    @Override
    public synchronized void stop() {
        if (!isOpen()) {
            throw new IllegalStateException("sequencer not open");
        }

        // not running; just return
        if (running == false) {
            return;
        }

        // stop playback
        implStop();
    }

    @Override
    public boolean isRunning() {
        return running && !paused;
    }

    @Override
    public void startRecording() {
        // Not implemented
    }

    @Override
    public void stopRecording() {
        // Not implemented
    }

    @Override
    public boolean isRecording() {
        return false;
    }

    @Override
    public void recordEnable(Track track, int channel) {
        // Not implemented
    }

    @Override
    public void recordDisable(Track track) {
        // Not implemented
    }

    @Override
    public float getTempoInBPM() {
        return (float) MidiUtils.convertTempo(getTempoInMPQ());
    }

    @Override
    public void setTempoInBPM(float bpm) {
        if (bpm <= 0) {
            // should throw IllegalArgumentException
            bpm = 1.0f;
        }

        setTempoInMPQ((float) MidiUtils.convertTempo((double) bpm));
    }

    @Override
    public float getTempoInMPQ() {
        if (needCaching()) {
            // if the sequencer is closed, return cached value
            if (cacheTempoMPQ != -1) {
                return (float) cacheTempoMPQ;
            }
            // if sequence is set, return current tempo
            if (sequence != null) {
                return tempoCache.getTempoMPQAt(getTickPosition());
            }

            // last resort: return a standard tempo: 120bpm
            return (float) MidiUtils.DEFAULT_TEMPO_MPQ;
        }
        return getDataPump().getTempoMPQ();
    }

    @Override
    public void setTempoInMPQ(float mpq) {
        if (mpq <= 0) {
            // should throw IllegalArgumentException
            mpq = 1.0f;
        }
        if (needCaching()) {
            // cache the value
            cacheTempoMPQ = mpq;
        } else {
            // set the native tempo in MPQ
            getDataPump().setTempoMPQ(mpq);

            // reset the tempoInBPM and tempoInMPQ values so we won't use them again
            cacheTempoMPQ = -1;
        }
    }

    @Override
    public void setTempoFactor(float factor) {
        if (factor <= 0) {
            // should throw IllegalArgumentException
            return;
        }
        if (needCaching()) {
            cacheTempoFactor = factor;
        } else {
            getDataPump().setTempoFactor(factor);
            // don't need cache anymore
            cacheTempoFactor = -1;
        }
    }

    @Override
    public float getTempoFactor() {
        if (needCaching()) {
            if (cacheTempoFactor != -1) {
                return cacheTempoFactor;
            }
            return 1.0f;
        }
        return getDataPump().getTempoFactor();
    }

    @Override
    public long getTickLength() {
        if (sequence == null) {
            return 0;
        }

        return sequence.getTickLength();
    }

    @Override
    public synchronized long getTickPosition() {
        if (getDataPump() == null || sequence == null) {
            return 0;
        }

        return getDataPump().getTickPos();
    }

    @Override
    public synchronized void setTickPosition(long tick) {
        if (tick < 0) {
            // should throw IllegalArgumentException
            return;
        }
        if (getDataPump() == null) {
            if (tick != 0) {
                // throw new InvalidStateException("cannot set position in closed state");
            }
        }
        else if (sequence == null) {
            if (tick != 0) {
                // throw new InvalidStateException("cannot set position if sequence is not set");
            }
        } else {
            getDataPump().setTickPos(tick);
        }
    }

    @Override
    public long getMicrosecondLength() {
        if (sequence == null) {
            return 0;
        }

        return sequence.getMicrosecondLength();
    }

    @Override
    public long getMicrosecondPosition() {
        if (getDataPump() == null || sequence == null) {
            return 0;
        }
        synchronized (tempoCache) {
            return MidiUtils.tick2microsecond(sequence, getDataPump().getTickPos(), tempoCache);
        }
    }

    @Override
    public void setMicrosecondPosition(long microseconds) {
        if (microseconds < 0) {
            // should throw IllegalArgumentException
            return;
        }
        if (getDataPump() == null) {
            if (microseconds != 0) {
                // throw new InvalidStateException("cannot set position in closed state");
            }
        }
        else if (sequence == null) {
            if (microseconds != 0) {
                // throw new InvalidStateException("cannot set position if sequence is not set");
            }
        } else {
            synchronized(tempoCache) {
                setTickPosition(MidiUtils.microsecond2tick(sequence, microseconds, tempoCache));
            }
        }
    }

    @Override
    public void setMasterSyncMode(Sequencer.SyncMode sync) {
        // not supported
    }

    @Override
    public Sequencer.SyncMode getMasterSyncMode() {
        return masterSyncMode;
    }

    @Override
    public Sequencer.SyncMode[] getMasterSyncModes() {
        Sequencer.SyncMode[] returnedModes = new Sequencer.SyncMode[masterSyncModes.length];
        System.arraycopy(masterSyncModes, 0, returnedModes, 0, masterSyncModes.length);
        return returnedModes;
    }

    @Override
    public void setSlaveSyncMode(Sequencer.SyncMode sync) {
        // not supported
    }

    @Override
    public Sequencer.SyncMode getSlaveSyncMode() {
        return slaveSyncMode;
    }

    @Override
    public Sequencer.SyncMode[] getSlaveSyncModes() {
        Sequencer.SyncMode[] returnedModes = new Sequencer.SyncMode[slaveSyncModes.length];
        System.arraycopy(slaveSyncModes, 0, returnedModes, 0, slaveSyncModes.length);
        return returnedModes;
    }

    int getTrackCount() {
        Sequence seq = getSequence();
        if (seq != null) {
            // $$fb wish there was a nicer way to get the number of tracks...
            return sequence.getTracks().length;
        }
        return 0;
    }

    @Override
    public synchronized void setTrackMute(int track, boolean mute) {
        int trackCount = getTrackCount();
        if (track < 0 || track >= getTrackCount()) return;
        trackMuted = ensureBoolArraySize(trackMuted, trackCount);
        trackMuted[track] = mute;
        if (getDataPump() != null) {
            getDataPump().muteSoloChanged();
        }
    }

    @Override
    public synchronized boolean getTrackMute(int track) {
        if (track < 0 || track >= getTrackCount()) return false;
        if (trackMuted == null || trackMuted.length <= track) return false;
        return trackMuted[track];
    }

    @Override
    public synchronized void setTrackSolo(int track, boolean solo) {
        int trackCount = getTrackCount();
        if (track < 0 || track >= getTrackCount()) return;
        trackSolo = ensureBoolArraySize(trackSolo, trackCount);
        trackSolo[track] = solo;
        if (getDataPump() != null) {
            getDataPump().muteSoloChanged();
        }
    }

    @Override
    public synchronized boolean getTrackSolo(int track) {
        if (track < 0 || track >= getTrackCount()) return false;
        if (trackSolo == null || trackSolo.length <= track) return false;
        return trackSolo[track];
    }

    @Override
    public boolean addMetaEventListener(MetaEventListener listener) {
        synchronized(metaEventListeners) {
            if (! metaEventListeners.contains(listener)) {

                metaEventListeners.add(listener);
            }
            return true;
        }
    }

    @Override
    public void removeMetaEventListener(MetaEventListener listener) {
        synchronized(metaEventListeners) {
            int index = metaEventListeners.indexOf(listener);
            if (index >= 0) {
                metaEventListeners.remove(index);
            }
        }
    }

    @Override
    public int[] addControllerEventListener(ControllerEventListener listener, int[] controllers) {
        synchronized(controllerEventListeners) {

            // first find the listener.  if we have one, add the controllers
            // if not, create a new element for it.
            ControllerListElement cve = null;
            boolean flag = false;
            for(int i=0; i < controllerEventListeners.size(); i++) {

                cve = controllerEventListeners.get(i);

                if (cve.listener.equals(listener)) {
                    cve.addControllers(controllers);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                cve = new ControllerListElement(listener, controllers);
                controllerEventListeners.add(cve);
            }

            // and return all the controllers this listener is interested in
            return cve.getControllers();
        }
    }

    @Override
    public int[] removeControllerEventListener(ControllerEventListener listener, int[] controllers) {
        synchronized(controllerEventListeners) {
            ControllerListElement cve = null;
            boolean flag = false;
            for (int i=0; i < controllerEventListeners.size(); i++) {
                cve = controllerEventListeners.get(i);
                if (cve.listener.equals(listener)) {
                    cve.removeControllers(controllers);
                    flag = true;
                    break;
                }
            }
            if (!flag) {
                return new int[0];
            }
            if (controllers == null) {
                int index = controllerEventListeners.indexOf(cve);
                if (index >= 0) {
                    controllerEventListeners.remove(index);
                }
                return new int[0];
            }
            return cve.getControllers();
        }
    }

    ////////////////// LOOPING (added in 1.5) ///////////////////////

    @Override
    public void setLoopStartPoint(long tick) {
        // Not implemented
    }

    @Override
    public long getLoopStartPoint() {
        return 0l;
    }

    @Override
    public void setLoopEndPoint(long tick) {
        // Not implemented
    }

    @Override
    public long getLoopEndPoint() {
        return 0l;
    }

    @Override
    public void setLoopCount(int count) {
        // Not implemented
    }

    @Override
    public int getLoopCount() {
        return 0;
    }

    /* *********************************** play control ************************* */

    @Override
    public void open() {
        try {
            super.open();
        } catch(MidiUnavailableException e) {
            Printer.err("Failed to open SimpleThreadSequencer: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void implOpen() {
        // create PlayThread
        playThread = new PlayThread();

        if (sequence != null) {
            playThread.setSequence(sequence);
        }

        // propagate caches
        propagateCaches();
        doAutoConnect();
    }

    private void doAutoConnect() {
        if (autoConnectedReceiver != null) {
            try {
                getTransmitter().setReceiver(autoConnectedReceiver);
            } catch (Exception e) {}
        }
    }

    private synchronized void propagateCaches() {
        // only set caches if open and sequence is set
        if (sequence != null && isOpen()) {
            if (cacheTempoFactor != -1) {
                setTempoFactor(cacheTempoFactor);
            }
            if (cacheTempoMPQ == -1) {
                setTempoInMPQ((new MidiUtils.TempoCache(sequence)).getTempoMPQAt(getTickPosition()));
            } else {
                setTempoInMPQ((float) cacheTempoMPQ);
            }
        }
    }

    /**
     * populate the caches with the current values.
    */
    private synchronized void setCaches() {
        cacheTempoFactor = getTempoFactor();
        cacheTempoMPQ = getTempoInMPQ();
    }

    @Override
    protected synchronized void implClose() {
        if (playThread == null) {
            if (Printer.err) Printer.err("RealTimeSequencer.implClose() called, but playThread not instanciated!");
        } else {
            // Interrupt playback loop.
            playThread.close();
            playThread = null;
        }

        super.implClose();

        sequence = null;
        running = false;
        cacheTempoMPQ = -1;
        cacheTempoFactor = -1;
        trackMuted = null;
        trackSolo = null;
    }

    void implStart() {
        if (playThread == null) {
            if (Printer.err) Printer.err("RealTimeSequencer.implStart() called, but playThread not instanciated!");
            return;
        }

        tempoCache.refresh(sequence);
        if (!running || paused) {
            running  = true;
            playThread.start();
        }
    }

    void implPause() {
        if (playThread == null) {
            if (Printer.err) Printer.err("RealTimeSequencer.implPause() called, but playThread not instanciated!");
            return;
        }

        if (running) {
            playThread.pause();
        }
    }

    void implStop() {
        if (playThread == null) {
            if (Printer.err) Printer.err("RealTimeSequencer.implStop() called, but playThread not instanciated!");
            return;
        }

        if (running) {
            running = false;
            playThread.stop();
        }
    }

    private static EventDispatcher getEventDispatcher() {
        // create and start the global event thread
        final ThreadGroup tg = Thread.currentThread().getThreadGroup();
        synchronized (dispatchers) {
            EventDispatcher eventDispatcher = dispatchers.get(tg);
            if (eventDispatcher == null) {
                eventDispatcher = new EventDispatcher();
                dispatchers.put(tg, eventDispatcher);
                eventDispatcher.start();
            }
            return eventDispatcher;
        }
    }

    /**
     * Send midi player events.
    * must not be synchronized on "this"
    */
    void sendMetaEvents(MidiMessage message) {
        if (metaEventListeners.size() == 0) return;

        getEventDispatcher().sendAudioEvents(message, metaEventListeners);
    }

    /**
     * Send midi player events.
    */
    void sendControllerEvents(MidiMessage message) {
        int size = controllerEventListeners.size();
        if (size == 0) return;

        if (! (message instanceof ShortMessage)) {
            return;
        }
        ShortMessage msg = (ShortMessage) message;
        int controller = msg.getData1();
        List<Object> sendToListeners = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            ControllerListElement cve = controllerEventListeners.get(i);
            for(int j = 0; j < cve.controllers.length; j++) {
                if (cve.controllers[j] == controller) {
                    sendToListeners.add(cve.listener);
                    break;
                }
            }
        }
        getEventDispatcher().sendAudioEvents(message, sendToListeners);
    }

    private boolean needCaching() {
        return !isOpen() || (sequence == null) || (playThread == null);
    }

    /**
     * return the data pump instance, owned by play thread
    * if playthread is null, return null.
    * This method is guaranteed to return non-null if
    * needCaching returns false
    */
    private DataPump getDataPump() {
        if (playThread != null) {
            return playThread.getDataPump();
        }
        return null;
    }

    private MidiUtils.TempoCache getTempoCache() {
        return tempoCache;
    }

    private static boolean[] ensureBoolArraySize(boolean[] array, int desiredSize) {
        if (array == null) {
            return new boolean[desiredSize];
        }
        if (array.length < desiredSize) {
            boolean[] newArray = new boolean[desiredSize];
            System.arraycopy(array, 0, newArray, 0, array.length);
            return newArray;
        }
        return array;
    }

    // OVERRIDES OF ABSTRACT MIDI DEVICE METHODS

    @Override
    protected boolean hasReceivers() {
        return true;
    }

    // for recording
    @Override
    protected Receiver createReceiver() throws MidiUnavailableException {
        return new SequencerReceiver();
    }

    @Override
    protected boolean hasTransmitters() {
        return true;
    }

    @Override
    protected Transmitter createTransmitter() throws MidiUnavailableException {
        return new SequencerTransmitter();
    }

    // interface AutoConnectSequencer
    @Override
    @SuppressWarnings("unchecked")
    public void setAutoConnect(Receiver autoConnectedReceiver) {
        try {
            this.autoConnectedReceiver = (R)autoConnectedReceiver;
        } catch(ClassCastException e) {
            this.autoConnectedReceiver = null;
        }
    }

    public R getAutoConnectedReceiver() {
        return autoConnectedReceiver;
    }


    /**
     * An own class to distinguish the class name from
    * the transmitter of other devices.
    */
    private class SequencerTransmitter extends BasicTransmitter {
        private SequencerTransmitter() {
            super();
        }
    }

    final class SequencerReceiver extends AbstractReceiver {

        @Override
        void implSend(MidiMessage message, long timeStamp) {
            // No-op
        }
    }

    private static class SimpleThreadSequencerInfo extends MidiDevice.Info {

        private static final String name = "Simple Thread Sequencer";
        private static final String vendor = "Tofodroid";
        private static final String description = "Simple threaded sequencer";
        private static final String version = "Version 1.0";

        SimpleThreadSequencerInfo() {
            super(name, vendor, description, version);
        }
    } // class Info

    private class ControllerListElement {

        // $$jb: using an array for controllers b/c its
        //       easier to deal with than turning all the
        //       ints into objects to use a Vector
        int []  controllers;
        final ControllerEventListener listener;

        private ControllerListElement(ControllerEventListener listener, int[] controllers) {

            this.listener = listener;
            if (controllers == null) {
                controllers = new int[128];
                for (int i = 0; i < 128; i++) {
                    controllers[i] = i;
                }
            }
            this.controllers = controllers;
        }

        private void addControllers(int[] c) {

            if (c==null) {
                controllers = new int[128];
                for (int i = 0; i < 128; i++) {
                    controllers[i] = i;
                }
                return;
            }
            int[] temp = new int[ controllers.length + c.length ];
            int elements;

            // first add what we have
            for(int i=0; i<controllers.length; i++) {
                temp[i] = controllers[i];
            }
            elements = controllers.length;
            // now add the new controllers only if we don't already have them
            for(int i=0; i<c.length; i++) {
                boolean flag = false;

                for(int j=0; j<controllers.length; j++) {
                    if (c[i] == controllers[j]) {
                        flag = true;
                        break;
                    }
                }
                if (!flag) {
                    temp[elements++] = c[i];
                }
            }
            // now keep only the elements we need
            int[] newc = new int[ elements ];
            for(int i=0; i<elements; i++){
                newc[i] = temp[i];
            }
            controllers = newc;
        }

        private void removeControllers(int[] c) {

            if (c==null) {
                controllers = new int[0];
            } else {
                int[] temp = new int[ controllers.length ];
                int elements = 0;


                for(int i=0; i<controllers.length; i++){
                    boolean flag = false;
                    for(int j=0; j<c.length; j++) {
                        if (controllers[i] == c[j]) {
                            flag = true;
                            break;
                        }
                    }
                    if (!flag){
                        temp[elements++] = controllers[i];
                    }
                }
                // now keep only the elements remaining
                int[] newc = new int[ elements ];
                for(int i=0; i<elements; i++) {
                    newc[i] = temp[i];
                }
                controllers = newc;

            }
        }

        private int[] getControllers() {

            // return a copy of our array of controllers,
            // so others can't mess with it
            if (controllers == null) {
                return null;
            }

            int[] c = new int[controllers.length];

            for(int i=0; i<controllers.length; i++){
                c[i] = controllers[i];
            }
            return c;
        }

    } // class ControllerListElement

    final class PlayThread implements Runnable {
        private Thread thread;
        private final Object lock = new Object();

        /** true if playback is interrupted (in close) */
        boolean interrupted = false;
        boolean isPumping = false;

        private final DataPump dataPump = new DataPump();


        PlayThread() {
            // MAX_PRIORITY
            int priority = Thread.MAX_PRIORITY;
            thread = JSSecurityManager.createThread(this,
                                                    "Simple Sound Sequencer", // name
                                                    false,                  // daemon
                                                    priority,               // priority
                                                    true);                  // doStart
        }

        DataPump getDataPump() {
            return dataPump;
        }

        synchronized void setSequence(Sequence seq) {
            dataPump.setSequence(seq);
        }


        /** start thread and pump. Requires up-to-date tempoCache */
        synchronized void start() {
            // mark the sequencer running
            running = true;
            paused = false;

            if (!dataPump.hasCachedTempo()) {
                long tickPos = getTickPosition();
                dataPump.setTempoMPQ(tempoCache.getTempoMPQAt(tickPos));
            }
            dataPump.checkPointMillis = 0; // means restarted
            dataPump.clearNoteOnCache();
            dataPump.needReindex = true;

            // notify the thread
            synchronized(lock) {
                lock.notifyAll();
            }
        }

        synchronized void pause() {
            playThreadImplPause();
        }

        // doesn't waits until stopped
        synchronized void stop() {
            playThreadImplStop();

            if(isPumping) {
                Printer.err("Failed to stop play thread after 250ms. Leaving for GC.");
            }
        }

        void playThreadImplPause() {
            // mark the sequencer paused
            paused = true;
            synchronized(lock) {
                lock.notifyAll();
            }
        }

        void playThreadImplStop() {
            // mark the sequencer stopped
            running = false;
            paused = false;
            synchronized(lock) {
                lock.notifyAll();
            }
        }

        void close() {
            Thread oldThread = null;
            synchronized (this) {
                // dispose of thread
                interrupted = true;
                oldThread = thread;
                thread = null;
            }
            if (oldThread != null) {
                // wake up the thread if it's in wait()
                synchronized(lock) {
                    lock.notifyAll();
                }
            }
        }

        /**
         * Main process loop driving the media flow.
        *
        * Make sure to NOT synchronize on SimpleThreadSequencer
        * anywhere here (even implicit). That is a sure deadlock!
        */
        @Override
        public void run() {

            while (!interrupted) {
                boolean EOM = false;
                boolean wasRunning = running;
                isPumping = !interrupted && running;
                while (!EOM && !interrupted && running) {
                    if(paused) {
                        dataPump.setCheckPointMillis(0l);
                    } else {
                        EOM = dataPump.pump();
                    }

                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ie) {
                        // ignore
                    }
                }

                playThreadImplStop();
                if (wasRunning) {
                    dataPump.notesOff(true);
                }
                if (EOM) {
                    dataPump.setTickPos(sequence.getTickLength());

                    // send EOT event (mis-used for end of media)
                    MetaMessage message = new MetaMessage();
                    try{
                        message.setMessage(MidiUtils.META_END_OF_TRACK_TYPE, new byte[0], 0);
                    } catch(InvalidMidiDataException e1) {}
                    sendMetaEvents(message);
                }
                synchronized (lock) {
                    isPumping = false;
                    // wake up a waiting stop() method
                    lock.notifyAll();
                    while (!running && !interrupted) {
                        try {
                            lock.wait();
                        } catch (Exception ex) {}
                    }
                }
            } // end of while(!EOM && !interrupted && running)
        }
    }

    /**
     * class that does the actual dispatching of events,
    * used to be in native in MMAPI.
    */
    private class DataPump {
        private float currTempo;         // MPQ tempo
        private float tempoFactor;       // 1.0 is default
        private float inverseTempoFactor;// = 1.0 / tempoFactor
        private long ignoreTempoEventAt; // ignore next META tempo during playback at this tick pos only
        private int resolution;
        private float divisionType;
        private long checkPointMillis;   // microseconds at checkoint
        private long checkPointTick;     // ticks at checkpoint
        private int[] noteOnCache;       // bit-mask of notes that are currently on
        private Track[] tracks;
        private boolean[] trackDisabled; // if true, do not play this track
        private int[] trackReadPos;      // read index per track
        private long lastTick;
        private boolean needReindex = false;

        DataPump() {
            init();
        }

        synchronized void init() {
            ignoreTempoEventAt = -1;
            tempoFactor = 1.0f;
            inverseTempoFactor = 1.0f;
            noteOnCache = new int[128];
            tracks = null;
            trackDisabled = null;
        }

        synchronized void setCheckPointMillis(long checkPointMillis) {
            this.checkPointMillis = checkPointMillis;
        }

        synchronized void setTickPos(long tickPos) {
            lastTick = tickPos;

            if (running) {
                notesOff(false);
            }

            needReindex = true;

            if (!hasCachedTempo()) {
                setTempoMPQ(getTempoCache().getTempoMPQAt(lastTick, currTempo));
                // treat this as if it is a real time tempo change
                ignoreTempoEventAt = -1;
            }
            // trigger re-configuration
            checkPointMillis = 0;
        }

        long getTickPos() {
            return lastTick;
        }

        // hasCachedTempo is only valid if it is the current position
        boolean hasCachedTempo() {
            if (ignoreTempoEventAt != lastTick) {
                ignoreTempoEventAt = -1;
            }
            return ignoreTempoEventAt >= 0;
        }

        // this method is also used internally in the pump!
        synchronized void setTempoMPQ(float tempoMPQ) {
            if (tempoMPQ > 0 && tempoMPQ != currTempo) {
                ignoreTempoEventAt = lastTick;
                this.currTempo = tempoMPQ;
                // re-calculate check point
                checkPointMillis = 0;
            }
        }

        float getTempoMPQ() {
            return currTempo;
        }

        synchronized void setTempoFactor(float factor) {
            if (factor > 0 && factor != this.tempoFactor) {
                tempoFactor = factor;
                inverseTempoFactor = 1.0f / factor;
                // re-calculate check point
                checkPointMillis = 0;
            }
        }

        float getTempoFactor() {
            return tempoFactor;
        }

        synchronized void muteSoloChanged() {
            boolean[] newDisabled = makeDisabledArray();
            if (running) {
                applyDisabledTracks(trackDisabled, newDisabled);
            }
            trackDisabled = newDisabled;
        }

        synchronized void setSequence(Sequence seq) {
            if (seq == null) {
                init();
                return;
            }
            tracks = seq.getTracks();
            muteSoloChanged();
            resolution = seq.getResolution();
            divisionType = seq.getDivisionType();
            trackReadPos = new int[tracks.length];
            // trigger re-initialization
            checkPointMillis = 0;
            needReindex = true;
        }
    
        void clearNoteOnCache() {
            for (int i = 0; i < 128; i++) {
                noteOnCache[i] = 0;
            }
        }

        void notesOff(boolean doControllers) {
            for (int ch=0; ch<16; ch++) {
                int channelMask = (1<<ch);
                for (int i=0; i<128; i++) {
                    if ((noteOnCache[i] & channelMask) != 0) {
                        noteOnCache[i] ^= channelMask;
                        // send note on with velocity 0
                        getTransmitterList().sendMessage((ShortMessage.NOTE_ON | ch) | (i<<8), -1);
                    }
                }
                /* all notes off */
                getTransmitterList().sendMessage((ShortMessage.CONTROL_CHANGE | ch) | (123<<8), -1);
                /* sustain off */
                getTransmitterList().sendMessage((ShortMessage.CONTROL_CHANGE | ch) | (64<<8), -1);
                if (doControllers) {
                    /* reset all controllers */
                    getTransmitterList().sendMessage((ShortMessage.CONTROL_CHANGE | ch) | (121<<8), -1);
                }
            }
        }

        private boolean[] makeDisabledArray() {
            if (tracks == null) {
                return null;
            }
            boolean[] newTrackDisabled = new boolean[tracks.length];
            boolean[] solo;
            boolean[] mute;
            synchronized(SimpleThreadSequencer.this) {
                mute = trackMuted;
                solo = trackSolo;
            }
            // if one track is solo, then only play solo
            boolean hasSolo = false;
            if (solo != null) {
                for (int i = 0; i < solo.length; i++) {
                    if (solo[i]) {
                        hasSolo = true;
                        break;
                    }
                }
            }
            if (hasSolo) {
                // only the channels with solo play, regardless of mute
                for (int i = 0; i < newTrackDisabled.length; i++) {
                    newTrackDisabled[i] = (i >= solo.length) || (!solo[i]);
                }
            } else {
                // mute the selected channels
                for (int i = 0; i < newTrackDisabled.length; i++) {
                    newTrackDisabled[i] = (mute != null) && (i < mute.length) && (mute[i]);
                }
            }
            return newTrackDisabled;
        }

        /**
         * chase all events from beginning of Track
        * and send note off for those events that are active
        * in noteOnCache array.
        * It is possible, of course, to catch notes from other tracks,
        * but better than more complicated logic to detect
        * which notes are really from this track
        */
        private void sendNoteOffIfOn(Track track, long endTick) {
            int size = track.size();
            try {
                for (int i = 0; i < size; i++) {
                    MidiEvent event = track.get(i);
                    if (event.getTick() > endTick) break;
                    MidiMessage msg = event.getMessage();
                    int status = msg.getStatus();
                    int len = msg.getLength();
                    if (len == 3 && ((status & 0xF0) == ShortMessage.NOTE_ON)) {
                        int note = -1;
                        if (msg instanceof ShortMessage) {
                            ShortMessage smsg = (ShortMessage) msg;
                            if (smsg.getData2() > 0) {
                                // only consider Note On with velocity > 0
                                note = smsg.getData1();
                            }
                        } else {
                            byte[] data = msg.getMessage();
                            if ((data[2] & 0x7F) > 0) {
                                // only consider Note On with velocity > 0
                                note = data[1] & 0x7F;
                            }
                        }
                        if (note >= 0) {
                            int bit = 1<<(status & 0x0F);
                            if ((noteOnCache[note] & bit) != 0) {
                                // the bit is set. Send Note Off
                                getTransmitterList().sendMessage(status | (note<<8), -1);
                                // clear the bit
                                noteOnCache[note] &= (0xFFFF ^ bit);
                            }
                        }
                    }
                }
            } catch (ArrayIndexOutOfBoundsException aioobe) {
                // this happens when messages are removed
                // from the track while this method executes
            }
        }

        /**
         * Runtime application of mute/solo:
        * if a track is muted that was previously playing, send
        *    note off events for all currently playing notes.
        */
        private void applyDisabledTracks(boolean[] oldDisabled, boolean[] newDisabled) {
            byte[][] tempArray = null;
            synchronized(SimpleThreadSequencer.this) {
                for (int i = 0; i < newDisabled.length; i++) {
                    if (((oldDisabled == null)
                        || (i >= oldDisabled.length)
                        || !oldDisabled[i])
                        && newDisabled[i]) {
                        // case that a track gets muted: need to
                        // send appropriate note off events to prevent
                        // hanging notes

                        if (tracks.length > i) {
                            sendNoteOffIfOn(tracks[i], lastTick);
                        }
                    }
                    else if ((oldDisabled != null)
                            && (i < oldDisabled.length)
                            && oldDisabled[i]
                            && !newDisabled[i]) {
                        // case that a track was muted and is now unmuted
                        // need to chase events and re-index this track
                        if (tempArray == null) {
                            tempArray = new byte[128][16];
                        }
                    }
                }
            }
        }

        // playback related methods (pumping)

        private long getCurrentTimeMillis() {
            return System.nanoTime() / 1000000l;
            //return perf.highResCounter() * 1000 / perfFreq;
        }

        private long millis2tick(long millis) {
            if (divisionType != Sequence.PPQ) {
                double dTick = ((((double) millis) * tempoFactor)
                                * ((double) divisionType)
                                * ((double) resolution))
                    / ((double) 1000);
                return (long) dTick;
            }
            return MidiUtils.microsec2ticks(millis * 1000,
                                            currTempo * inverseTempoFactor,
                                            resolution);
        }

        private void ReindexTrack(int trackNum, long tick) {
            if (trackNum < trackReadPos.length && trackNum < tracks.length) {
                trackReadPos[trackNum] = MidiUtils.tick2index(tracks[trackNum], tick);
            }
        }

        /* returns if changes are pending */
        private boolean dispatchMessage(int trackNum, MidiEvent event) {
            boolean changesPending = false;
            MidiMessage message = event.getMessage();
            int msgStatus = message.getStatus();
            int msgLen = message.getLength();
            if (msgStatus == MetaMessage.META && msgLen >= 2) {
                // a meta message. Do not send it to the device.
                // 0xFF with length=1 is a MIDI realtime message
                // which shouldn't be in a Sequence, but we play it
                // nonetheless.

                // see if this is a tempo message. Only on track 0.
                if (trackNum == 0) {
                    int newTempo = MidiUtils.getTempoMPQ(message);
                    if (newTempo > 0) {
                        if (event.getTick() != ignoreTempoEventAt) {
                            setTempoMPQ(newTempo); // sets ignoreTempoEventAt!
                            changesPending = true;
                        }
                        // next loop, do not ignore anymore tempo events.
                        ignoreTempoEventAt = -1;
                    }
                }
                // send to listeners
                sendMetaEvents(message);

            } else {
                // not meta, send to device
                getTransmitterList().sendMessage(message, -1);

                switch (msgStatus & 0xF0) {
                case ShortMessage.NOTE_OFF: {
                    // note off - clear the bit in the noteOnCache array
                    int note = ((ShortMessage) message).getData1() & 0x7F;
                    noteOnCache[note] &= (0xFFFF ^ (1<<(msgStatus & 0x0F)));
                    break;
                }

                case ShortMessage.NOTE_ON: {
                    // note on
                    ShortMessage smsg = (ShortMessage) message;
                    int note = smsg.getData1() & 0x7F;
                    int vel = smsg.getData2() & 0x7F;
                    if (vel > 0) {
                        // if velocity > 0 set the bit in the noteOnCache array
                        noteOnCache[note] |= 1<<(msgStatus & 0x0F);
                    } else {
                        // if velocity = 0 clear the bit in the noteOnCache array
                        noteOnCache[note] &= (0xFFFF ^ (1<<(msgStatus & 0x0F)));
                    }
                    break;
                }

                case ShortMessage.CONTROL_CHANGE:
                    // if controller message, send controller listeners
                    sendControllerEvents(message);
                    break;

                }
            }
            return changesPending;
        }

        /** the main pump method
         * @return true if end of sequence is reached
        */
        synchronized boolean pump() {
            long currMillis;
            long targetTick = lastTick;
            MidiEvent currEvent;
            boolean changesPending = false;
            boolean EOM = false;

            currMillis = getCurrentTimeMillis();
            int finishedTracks = 0;
            do {
                changesPending = false;

                // need to re-find indexes in tracks?
                if (needReindex) {
                    if (trackReadPos.length < tracks.length) {
                        trackReadPos = new int[tracks.length];
                    }
                    for (int t = 0; t < tracks.length; t++) {
                        ReindexTrack(t, targetTick);
                    }
                    needReindex = false;
                    checkPointMillis = 0;
                }

                // get target tick from current time in millis
                if (checkPointMillis == 0) {
                    // new check point
                    currMillis = getCurrentTimeMillis();
                    checkPointMillis = currMillis;
                    targetTick = lastTick;
                    checkPointTick = targetTick;
                } else {
                    // calculate current tick based on current time in milliseconds
                    targetTick = checkPointTick + millis2tick(currMillis - checkPointMillis);
                    lastTick = targetTick;
                }

                finishedTracks = 0;

                for (int t = 0; t < tracks.length; t++) {
                    try {
                        boolean disabled = trackDisabled[t];
                        Track thisTrack = tracks[t];
                        int readPos = trackReadPos[t];
                        int size = thisTrack.size();
                        // play all events that are due until targetTick
                        while (!changesPending && (readPos < size)
                            && (currEvent = thisTrack.get(readPos)).getTick() <= targetTick) {

                            if ((readPos == size -1) &&  MidiUtils.isMetaEndOfTrack(currEvent.getMessage())) {
                                // do not send out this message. Finished with this track
                                readPos = size;
                                break;
                            }
                            // significantly (i.e. deleted or inserted a bunch of messages)
                            // since last time. Would need to set needReindex = true then
                            readPos++;
                            // only play this event if the track is enabled,
                            // or if it is a tempo message on track 0
                            // Note: cannot put this check outside
                            //       this inner loop in order to detect end of file
                            if (!disabled ||
                                ((t == 0) && (MidiUtils.isMetaTempo(currEvent.getMessage())))) {
                                changesPending = dispatchMessage(t, currEvent);
                            }
                        }
                        if (readPos >= size) {
                            finishedTracks++;
                        }
                        trackReadPos[t] = readPos;
                    } catch(Exception e) {
                        if (Printer.err) e.printStackTrace();
                        if (e instanceof ArrayIndexOutOfBoundsException) {
                            needReindex = true;
                            changesPending = true;
                        }
                    }
                    if (changesPending) {
                        break;
                    }
                }
                EOM = (finishedTracks == tracks.length);
            } while (changesPending);

            return EOM;
        }
    } // class DataPump
}
