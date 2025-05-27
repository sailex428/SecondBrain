package me.sailex.secondbrain.callback;

import me.sailex.secondbrain.model.stt.STTType;
import net.fabricmc.fabric.api.event.Event;
import net.fabricmc.fabric.api.event.EventFactory;

public interface STTCallback {

    Event<STTCallback> EVENT = EventFactory.createArrayBacked(
            STTCallback.class, listeners -> (type) -> {
                for (STTCallback listener : listeners) {
                    listener.onSTTAction(type);
                }
            });

    void onSTTAction(STTType type);
}
