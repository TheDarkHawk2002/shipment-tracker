package dev.okanyakin.shipmenttracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CsvShipmentReaderTest {
    @TempDir Path tempDir;

    @Test
    void readsSemicolonSeparatedDataAndGroupsEvents() throws Exception {
        Path file = tempDir.resolve("input.csv");
        Files.write(file, ("shipment_id;expected_delivery;timestamp;status;location\n"
                + "A;2026-10-20;2026-10-15T09:00:00;CREATED;Münster\n"
                + "A;2026-10-20;2026-10-15T10:00:00;IN_TRANSIT;Köln\n").getBytes(java.nio.charset.StandardCharsets.UTF_8));
        assertEquals(1, new CsvShipmentReader().read(file).size());
        assertEquals(2, new CsvShipmentReader().read(file).get(0).getEvents().size());
    }

    @Test
    void reportsInvalidRowsWithLineNumber() throws Exception {
        Path file = tempDir.resolve("invalid.csv");
        Files.write(file, "shipment_id,expected_delivery,timestamp,status,location\nA,not-a-date,nope,WRONG,Here\n".getBytes(java.nio.charset.StandardCharsets.UTF_8));
        InputValidationException error = assertThrows(InputValidationException.class, () -> new CsvShipmentReader().read(file));
        assertEquals(true, error.getErrors().get(0).contains("Line 2"));
    }

    @Test
    void emptyFileReturnsEmptyList() throws Exception {
        Path file = tempDir.resolve("empty.csv");
        Files.write(file, Collections.<String>emptyList());
        assertEquals(0, new CsvShipmentReader().read(file).size());
    }
}
