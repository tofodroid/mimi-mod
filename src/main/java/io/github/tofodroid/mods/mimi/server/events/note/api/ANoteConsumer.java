package io.github.tofodroid.mods.mimi.server.events.note.api;

import java.util.UUID;


public abstract class ANoteConsumer implements INoteConsumer {
    protected UUID id;
    protected Byte instrumentId;

    public Byte getInstrumentId() {
        return instrumentId;
    }

    public ANoteConsumer(UUID id, Byte instrumentId) {
        this.id = id;
        this.instrumentId = instrumentId;
    }

    public void tickConsumer() {/*Default no-op*/}
    public void close() throws Exception {this.onConsumerRemoved();}
}
