package dev.okanyakin.shipmenttracker;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.util.List;

public final class Main {
    private Main() { }

    public static void main(String[] args) {
        System.exit(run(args, LocalDate.now()));
    }

    static int run(String[] args, LocalDate asOf) {
        if (args == null || args.length == 0 || args.length > 8) return usage();
        Path input = null;
        Path output = null;
        String format = "text";
        LocalDate reportDate = asOf;
        try {
            for (int i = 0; i < args.length; i++) {
                switch (args[i]) {
                    case "--input":
                        if (++i >= args.length || input != null) return usage();
                        input = Path.of(args[i]);
                        break;
                    case "--output":
                        if (++i >= args.length || output != null) return usage();
                        output = Path.of(args[i]);
                        break;
                    case "--format":
                        if (++i >= args.length) return usage();
                        format = args[i].toLowerCase(java.util.Locale.ROOT);
                        break;
                    case "--as-of":
                        if (++i >= args.length) return usage();
                        reportDate = LocalDate.parse(args[i]);
                        break;
                    case "--help":
                    case "-h":
                        printUsage();
                        return 0;
                    default:
                        return usage();
                }
            }
        } catch (RuntimeException ex) {
            System.err.println("Invalid command-line value: " + ex.getMessage());
            return 2;
        }
        if (input == null || !java.util.Arrays.asList("text", "csv", "json").contains(format)) return usage();
        try {
            Path normalizedInput = input.toAbsolutePath().normalize();
            if (!Files.isRegularFile(normalizedInput)) {
                System.err.println("Input file does not exist or is not a regular file: " + normalizedInput);
                return 1;
            }
            List<Shipment> shipments = new CsvShipmentReader().read(normalizedInput);
            List<ShipmentResult> results = new ShipmentAnalyzer().analyze(shipments, reportDate);
            ReportWriter writer = new ReportWriter();
            String report = "text".equals(format) ? writer.toText(results)
                    : "csv".equals(format) ? writer.toCsv(results) : writer.toJson(results);
            if (output == null) System.out.print(report);
            else writer.write(output, report, normalizedInput);
            return 0;
        } catch (InputValidationException ex) {
            System.err.println("Input validation failed:");
            ex.getErrors().forEach(error -> System.err.println(" - " + error));
            return 1;
        } catch (IllegalArgumentException ex) {
            System.err.println("Analysis failed: " + ex.getMessage());
            return 1;
        } catch (IOException ex) {
            System.err.println("I/O error: " + ex.getMessage());
            return 1;
        }
    }

    private static int usage() {
        printUsage();
        return 2;
    }

    private static void printUsage() {
        System.err.println("Usage: shipment-tracker --input <file.csv> [--format text|csv|json] [--output <new-file>] [--as-of yyyy-MM-dd]");
        System.err.println("Input columns: shipment_id,expected_delivery,timestamp,status,location");
    }
}
