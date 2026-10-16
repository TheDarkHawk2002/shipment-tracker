package dev.okanyakin.shipmenttracker;

public enum ShipmentStatus {
    CREATED,
    PICKED_UP,
    IN_TRANSIT,
    OUT_FOR_DELIVERY,
    DELIVERED,
    EXCEPTION;

    public static ShipmentStatus parse(String value) {
        try {
            return ShipmentStatus.valueOf(value.trim().toUpperCase(java.util.Locale.ROOT));
        } catch (RuntimeException ex) {
            throw new IllegalArgumentException("Unknown status '" + value + "'. Allowed: "
                    + "CREATED, PICKED_UP, IN_TRANSIT, OUT_FOR_DELIVERY, DELIVERED, EXCEPTION.");
        }
    }
}
