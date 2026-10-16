package dev.okanyakin.shipmenttracker;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class ReportWriterTest {
    @TempDir Path tempDir;

    @Test
    void emitsEmptyJsonArrayForNoShipments() {
        assertEquals("[\n]\n", new ReportWriter().toJson(Collections.emptyList()));
    }

    @Test
    void refusesToOverwriteInput() {
        Path input = tempDir.resolve("input.csv");
        assertThrows(IOException.class, () -> new ReportWriter().write(input, "report", input));
    }

    @Test
    void refusesToOverwriteExistingOutput() throws Exception {
        Path input = tempDir.resolve("input.csv");
        Path output = tempDir.resolve("report.json");
        Files.write(output, new byte[] {1});
        assertThrows(IOException.class, () -> new ReportWriter().write(output, "report", input));
        assertEquals(1, Files.size(output));
    }

    @Test
    void jsonEscapesQuotesAndNewLines() {
        ShipmentResult result = new ShipmentResult("A\"B", LocalDate.parse("2026-10-20"), ShipmentStatus.CREATED,
                LocalDateTime.parse("2026-10-15T09:00:00"), "A\nB", "ON_TRACK", "A\"B");
        String json = new ReportWriter().toJson(Collections.singletonList(result));
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("A\\\"B"));
        org.junit.jupiter.api.Assertions.assertTrue(json.contains("A\\nB"));
    }
}
