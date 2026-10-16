package dev.okanyakin.shipmenttracker;

import java.time.LocalDateTime;
import java.util.Objects;

public final class ShipmentEvent {
    private final LocalDateTime timestamp;
    private final ShipmentStatus status;
    private final String location;

    public ShipmentEvent(LocalDateTime timestamp, ShipmentStatus status, String location) {
        this.timestamp = Objects.requireNonNull(timestamp, "timestamp");
        this.status = Objects.requireNonNull(status, "status");
        this.location = location == null ? "" : location.trim();
    }

    public LocalDateTime getTimestamp() { return timestamp; }
    public ShipmentStatus getStatus() { return status; }
    public String getLocation() { return location; }
}
