package dev.okanyakin.shipmenttracker;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public final class ShipmentAnalyzer {
    private static final List<ShipmentStatus> NORMAL_FLOW = java.util.Arrays.asList(
            ShipmentStatus.CREATED, ShipmentStatus.PICKED_UP, ShipmentStatus.IN_TRANSIT,
            ShipmentStatus.OUT_FOR_DELIVERY, ShipmentStatus.DELIVERED);

    public List<ShipmentResult> analyze(List<Shipment> shipments, LocalDate asOf) {
        if (shipments == null || asOf == null) throw new IllegalArgumentException("Shipments and report date are required.");
        List<ShipmentResult> results = new ArrayList<>();
        for (Shipment shipment : shipments) results.add(analyzeOne(shipment, asOf));
        results.sort(Comparator.comparing(ShipmentResult::getExpectedDelivery).thenComparing(ShipmentResult::getId));
        return results;
    }

    private ShipmentResult analyzeOne(Shipment shipment, LocalDate asOf) {
        List<ShipmentEvent> events = shipment.getEvents().stream()
                .sorted(Comparator.comparing(ShipmentEvent::getTimestamp)).collect(Collectors.toList());
        LocalDateTime previous = null;
        ShipmentStatus previousNormalStatus = null;
        boolean delivered = false;
        Set<String> seen = new HashSet<>();
        for (ShipmentEvent event : events) {
            if (delivered) {
                throw new IllegalArgumentException("Shipment " + shipment.getId() + " has an event after delivery.");
            }
            if (event.getTimestamp().toLocalDate().isAfter(asOf)) {
                throw new IllegalArgumentException("Shipment " + shipment.getId() + " has an event after report date " + asOf + ".");
            }
            if (previous != null && event.getTimestamp().equals(previous)) {
                throw new IllegalArgumentException("Shipment " + shipment.getId() + " has multiple events at " + previous + ".");
            }
            String signature = event.getTimestamp() + "|" + event.getStatus() + "|" + event.getLocation();
            if (!seen.add(signature)) throw new IllegalArgumentException("Shipment " + shipment.getId() + " contains a duplicate event.");
            if (event.getStatus() != ShipmentStatus.EXCEPTION) {
                if (previousNormalStatus != null) {
                    int prior = NORMAL_FLOW.indexOf(previousNormalStatus);
                    int current = NORMAL_FLOW.indexOf(event.getStatus());
                    if (current < 0 || current < prior) {
                        throw new IllegalArgumentException("Shipment " + shipment.getId() + " has invalid status sequence: "
                                + previousNormalStatus + " -> " + event.getStatus() + ".");
                    }
                } else if (!NORMAL_FLOW.contains(event.getStatus())) {
                    throw new IllegalArgumentException("Shipment " + shipment.getId() + " must begin with a normal status.");
                }
                previousNormalStatus = event.getStatus();
                if (event.getStatus() == ShipmentStatus.DELIVERED) delivered = true;
            }
            previous = event.getTimestamp();
        }

        ShipmentEvent latest = events.get(events.size() - 1);
        ShipmentStatus currentStatus = latest.getStatus();
        String risk;
        String reason;
        if (currentStatus == ShipmentStatus.DELIVERED) {
            risk = "DELIVERED";
            reason = "Delivery event recorded on " + latest.getTimestamp().toLocalDate() + ".";
        } else if (asOf.isAfter(shipment.getExpectedDelivery())) {
            risk = "LATE";
            reason = "Expected delivery date " + shipment.getExpectedDelivery() + " has passed without a delivery event.";
        } else if (asOf.equals(shipment.getExpectedDelivery())) {
            risk = "DUE_TODAY";
            reason = "Delivery is expected today and no delivery event is recorded.";
        } else {
            risk = "ON_TRACK";
            reason = "Expected delivery date is " + shipment.getExpectedDelivery() + ".";
        }
        return new ShipmentResult(shipment.getId(), shipment.getExpectedDelivery(), currentStatus,
                latest.getTimestamp(), latest.getLocation(), risk, reason);
    }
}
