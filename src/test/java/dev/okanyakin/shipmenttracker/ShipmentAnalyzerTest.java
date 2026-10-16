package dev.okanyakin.shipmenttracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.Test;

class ShipmentAnalyzerTest {
    private final ShipmentAnalyzer analyzer = new ShipmentAnalyzer();

    @Test
    void classifiesLateUndeliveredShipment() {
        Shipment shipment = shipment("S1", "2026-10-12", event("2026-10-11T09:00:00", ShipmentStatus.IN_TRANSIT));
        ShipmentResult result = analyzer.analyze(Collections.singletonList(shipment), LocalDate.parse("2026-10-15")).get(0);
        assertEquals("LATE", result.getRisk());
        assertEquals("Expected delivery date 2026-10-12 has passed without a delivery event.", result.getReason());
    }

    @Test
    void deliveredShipmentIsNotClassifiedAsLate() {
        Shipment shipment = shipment("S2", "2026-10-12",
                event("2026-10-11T09:00:00", ShipmentStatus.IN_TRANSIT),
                event("2026-10-12T12:00:00", ShipmentStatus.DELIVERED));
        ShipmentResult result = analyzer.analyze(Collections.singletonList(shipment), LocalDate.parse("2026-10-15")).get(0);
        assertEquals("DELIVERED", result.getRisk());
    }

    @Test
    void emptyShipmentListProducesEmptyResults() {
        assertEquals(Collections.emptyList(), analyzer.analyze(Collections.emptyList(), LocalDate.parse("2026-10-15")));
    }

    @Test
    void rejectsBackwardStatusTransition() {
        Shipment shipment = shipment("S3", "2026-10-20",
                event("2026-10-11T09:00:00", ShipmentStatus.IN_TRANSIT),
                event("2026-10-12T12:00:00", ShipmentStatus.CREATED));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> analyzer.analyze(Collections.singletonList(shipment), LocalDate.parse("2026-10-15")));
        assertEquals(true, error.getMessage().contains("invalid status sequence"));
    }

    @Test
    void rejectsEventsAfterDelivery() {
        Shipment shipment = shipment("S5", "2026-10-20",
                event("2026-10-11T09:00:00", ShipmentStatus.DELIVERED),
                event("2026-10-12T12:00:00", ShipmentStatus.EXCEPTION));
        IllegalArgumentException error = assertThrows(IllegalArgumentException.class,
                () -> analyzer.analyze(Collections.singletonList(shipment), LocalDate.parse("2026-10-15")));
        assertEquals(true, error.getMessage().contains("event after delivery"));
    }

    @Test
    void eventsAreSortedBeforeCurrentStatusIsDerived() {
        Shipment shipment = shipment("S4", "2026-10-20",
                event("2026-10-12T12:00:00", ShipmentStatus.IN_TRANSIT),
                event("2026-10-11T09:00:00", ShipmentStatus.CREATED));
        assertEquals(ShipmentStatus.IN_TRANSIT,
                analyzer.analyze(Collections.singletonList(shipment), LocalDate.parse("2026-10-15")).get(0).getCurrentStatus());
    }

    @Test
    void stableOrderUsesExpectedDateThenId() {
        List<Shipment> shipments = Arrays.asList(
                shipment("B", "2026-10-20", event("2026-10-11T09:00:00", ShipmentStatus.CREATED)),
                shipment("A", "2026-10-20", event("2026-10-11T09:00:00", ShipmentStatus.CREATED)),
                shipment("C", "2026-10-18", event("2026-10-11T09:00:00", ShipmentStatus.CREATED)));
        List<ShipmentResult> results = analyzer.analyze(shipments, LocalDate.parse("2026-10-15"));
        assertEquals(Arrays.asList("C", "A", "B"), Arrays.asList(results.get(0).getId(), results.get(1).getId(), results.get(2).getId()));
    }

    private Shipment shipment(String id, String expected, ShipmentEvent... events) {
        return new Shipment(id, LocalDate.parse(expected), Arrays.asList(events));
    }

    private ShipmentEvent event(String timestamp, ShipmentStatus status) {
        return new ShipmentEvent(LocalDateTime.parse(timestamp), status, "Test location");
    }
}
