package dev.okanyakin.shipmenttracker;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public final class Shipment {
    private final String id;
    private final LocalDate expectedDelivery;
    private final List<ShipmentEvent> events;

    public Shipment(String id, LocalDate expectedDelivery, List<ShipmentEvent> events) {
        this.id = Objects.requireNonNull(id, "id").trim();
        this.expectedDelivery = Objects.requireNonNull(expectedDelivery, "expectedDelivery");
        this.events = Collections.unmodifiableList(new ArrayList<>(Objects.requireNonNull(events, "events")));
        if (this.id.isEmpty()) throw new IllegalArgumentException("Shipment ID must not be blank.");
        if (this.events.isEmpty()) throw new IllegalArgumentException("Shipment " + this.id + " needs at least one event.");
    }

    public String getId() { return id; }
    public LocalDate getExpectedDelivery() { return expectedDelivery; }
    public List<ShipmentEvent> getEvents() { return events; }
}
