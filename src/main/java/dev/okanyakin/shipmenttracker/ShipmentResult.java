package dev.okanyakin.shipmenttracker;

import java.time.LocalDate;
import java.time.LocalDateTime;

public final class ShipmentResult {
    private final String id;
    private final LocalDate expectedDelivery;
    private final ShipmentStatus currentStatus;
    private final LocalDateTime lastEventAt;
    private final String lastLocation;
    private final String risk;
    private final String reason;

    public ShipmentResult(String id, LocalDate expectedDelivery, ShipmentStatus currentStatus,
                          LocalDateTime lastEventAt, String lastLocation, String risk, String reason) {
        this.id = id;
        this.expectedDelivery = expectedDelivery;
        this.currentStatus = currentStatus;
        this.lastEventAt = lastEventAt;
        this.lastLocation = lastLocation;
        this.risk = risk;
        this.reason = reason;
    }

    public String getId() { return id; }
    public LocalDate getExpectedDelivery() { return expectedDelivery; }
    public ShipmentStatus getCurrentStatus() { return currentStatus; }
    public LocalDateTime getLastEventAt() { return lastEventAt; }
    public String getLastLocation() { return lastLocation; }
    public String getRisk() { return risk; }
    public String getReason() { return reason; }
}
