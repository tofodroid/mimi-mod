/*
 * Copyright (c) 2003, 2015, Oracle and/or its affiliates. All rights reserved.
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

import java.util.Objects;

import javax.sound.midi.MidiDevice;
import javax.sound.midi.spi.MidiDeviceProvider;

/**
 * Provider for RealTimeSequencer objects.
 *
 * @author Florian Bomers
 */
public final class RealTimeSequencerProvider extends MidiDeviceProvider {

    @Override
    public MidiDevice.Info[] getDeviceInfo() {
        return new MidiDevice.Info[]{RealTimeSequencer.info};
    }

    @Override
    public MidiDevice getDevice(final MidiDevice.Info info) {
        Objects.requireNonNull(info);
        if (RealTimeSequencer.info.equals(info)) {
            return new RealTimeSequencer();
        }
        throw MidiUtils.unsupportedDevice(info);
    }
}
