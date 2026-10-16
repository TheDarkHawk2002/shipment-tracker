package dev.okanyakin.shipmenttracker;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public final class ReportWriter {
    public String toCsv(List<ShipmentResult> results) {
        StringBuilder csv = new StringBuilder("shipment_id,expected_delivery,current_status,last_event_at,last_location,risk,reason\n");
        for (ShipmentResult result : results) {
            csv.append(cell(result.getId())).append(',')
                    .append(result.getExpectedDelivery()).append(',')
                    .append(result.getCurrentStatus()).append(',')
                    .append(result.getLastEventAt()).append(',')
                    .append(cell(result.getLastLocation())).append(',')
                    .append(result.getRisk()).append(',')
                    .append(cell(result.getReason())).append('\n');
        }
        return csv.toString();
    }

    public String toJson(List<ShipmentResult> results) {
        StringBuilder json = new StringBuilder("[\n");
        for (int i = 0; i < results.size(); i++) {
            ShipmentResult r = results.get(i);
            json.append("  {\"shipmentId\":\"").append(escape(r.getId())).append("\",")
                    .append("\"expectedDelivery\":\"").append(r.getExpectedDelivery()).append("\",")
                    .append("\"currentStatus\":\"").append(r.getCurrentStatus()).append("\",")
                    .append("\"lastEventAt\":\"").append(r.getLastEventAt()).append("\",")
                    .append("\"lastLocation\":\"").append(escape(r.getLastLocation())).append("\",")
                    .append("\"risk\":\"").append(r.getRisk()).append("\",")
                    .append("\"reason\":\"").append(escape(r.getReason())).append("\"}")
                    .append(i + 1 < results.size() ? "," : "").append('\n');
        }
        return json.append("]\n").toString();
    }

    public String toText(List<ShipmentResult> results) {
        if (results.isEmpty()) return "No shipments found.\n";
        int width = results.stream().mapToInt(r -> r.getId().length()).max().orElse(11);
        width = Math.max(width, "SHIPMENT".length());
        StringBuilder text = new StringBuilder(String.format(Locale.ROOT,
                "%-" + width + "s  %-10s  %-16s  %-17s  %-14s  %s%n", "SHIPMENT", "EXPECTED", "STATUS", "LAST EVENT", "RISK", "REASON"));
        text.append(repeat('-', width)).append("  ----------  ----------------  -----------------  --------------  ------\n");
        for (ShipmentResult r : results) {
            text.append(String.format(Locale.ROOT, "%-" + width + "s  %-10s  %-16s  %-17s  %-14s  %s%n", r.getId(),
                    r.getExpectedDelivery(), r.getCurrentStatus(), r.getLastEventAt(), r.getRisk(), r.getReason()));
        }
        return text.toString();
    }

    public void write(Path path, String content, Path input) throws IOException {
        Path output = path.toAbsolutePath().normalize();
        if (input != null && output.equals(input.toAbsolutePath().normalize())) {
            throw new IOException("Output path must not overwrite the input file.");
        }
        if (Files.exists(output)) throw new IOException("Output file already exists; choose a new path to protect existing data: " + output);
        Path parent = output.getParent();
        if (parent != null) Files.createDirectories(parent);
        Files.write(output, content.getBytes(StandardCharsets.UTF_8));
    }

    private String cell(String value) {
        return "\"" + value.replace("\"", "\"\"") + "\"";
    }

    private String escape(String value) {
        return value.replace("\\", "\\\\").replace("\"", "\\\"")
                .replace("\n", "\\n").replace("\r", "\\r").replace("\t", "\\t");
    }

    private String repeat(char ch, int count) {
        return java.util.stream.IntStream.range(0, count).mapToObj(i -> String.valueOf(ch)).collect(Collectors.joining());
    }
}
