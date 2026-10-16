package dev.okanyakin.shipmenttracker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CsvShipmentReader {
    public List<Shipment> read(Path path) throws IOException, InputValidationException {
        List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
        Map<String, MutableShipment> shipments = new LinkedHashMap<>();
        List<String> errors = new ArrayList<>();
        char delimiter = detectDelimiter(lines);
        boolean headerSkipped = false;
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            int lineNumber = i + 1;
            if (line.isEmpty() || line.startsWith("#")) continue;
            if (!headerSkipped && line.toLowerCase(java.util.Locale.ROOT).startsWith("shipment_id")) {
                headerSkipped = true;
                continue;
            }
            String[] columns = line.split(java.util.regex.Pattern.quote(String.valueOf(delimiter)), -1);
            if (columns.length != 5) {
                errors.add("Line " + lineNumber + ": expected 5 columns (shipment_id, expected_delivery, timestamp, status, location), found " + columns.length + ".");
                continue;
            }
            try {
                String id = columns[0].trim();
                if (id.isEmpty()) throw new IllegalArgumentException("shipment_id must not be blank.");
                LocalDate expected = LocalDate.parse(columns[1].trim());
                LocalDateTime timestamp = LocalDateTime.parse(columns[2].trim());
                ShipmentStatus status = ShipmentStatus.parse(columns[3]);
                MutableShipment current = shipments.get(id);
                if (current == null) {
                    current = new MutableShipment(expected);
                    shipments.put(id, current);
                } else if (!current.expected.equals(expected)) {
                    throw new IllegalArgumentException("expected_delivery differs from earlier rows for shipment " + id + ".");
                }
                current.events.add(new ShipmentEvent(timestamp, status, columns[4]));
            } catch (DateTimeParseException ex) {
                errors.add("Line " + lineNumber + ": invalid date/time; use yyyy-MM-dd and yyyy-MM-ddTHH:mm:ss.");
            } catch (IllegalArgumentException ex) {
                errors.add("Line " + lineNumber + ": " + ex.getMessage());
            }
        }
        if (!errors.isEmpty()) throw new InputValidationException(errors);
        List<Shipment> result = new ArrayList<>();
        for (Map.Entry<String, MutableShipment> entry : shipments.entrySet()) {
            try {
                result.add(new Shipment(entry.getKey(), entry.getValue().expected, entry.getValue().events));
            } catch (IllegalArgumentException ex) {
                errors.add("Shipment " + entry.getKey() + ": " + ex.getMessage());
            }
        }
        if (!errors.isEmpty()) throw new InputValidationException(errors);
        return result;
    }

    private char detectDelimiter(List<String> lines) {
        for (String line : lines) {
            String candidate = line.trim();
            if (candidate.isEmpty() || candidate.startsWith("#")) continue;
            long commas = candidate.chars().filter(ch -> ch == ',').count();
            long semicolons = candidate.chars().filter(ch -> ch == ';').count();
            return semicolons > commas ? ';' : ',';
        }
        return ',';
    }

    private static final class MutableShipment {
        private final LocalDate expected;
        private final List<ShipmentEvent> events = new ArrayList<>();
        private MutableShipment(LocalDate expected) { this.expected = expected; }
    }
}
