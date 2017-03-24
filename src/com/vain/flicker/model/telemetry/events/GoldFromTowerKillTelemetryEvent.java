package com.vain.flicker.model.telemetry.events;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * @author Dominic Gunn (dominic@vain.gg)
 */
@JsonDeserialize(as = GoldFromTowerKillTelemetryEvent.class)
public class GoldFromTowerKillTelemetryEvent extends TelemetryEvent {

    private final static String KEY_AMOUNT = "Amount";

    public int getAmount() {
        return (int) payload.get(KEY_AMOUNT);
    }
}
