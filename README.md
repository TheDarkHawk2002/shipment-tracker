# Shipment Tracker

Eine kleine Java-Konsolenanwendung wertet Sendungsereignisse aus und erstellt einen nachvollziehbaren Status- und Lieferterminbericht. Sie zeigt einen reproduzierbaren Ablauf vom CSV-Einlesen über Eingabeprüfung und Fachlogik bis zum Text-, CSV- oder JSON-Bericht.

## Voraussetzungen

- JDK 11 oder neuer
- Maven 3.8 oder neuer

## Bauen und Tests ausführen

```powershell
mvn clean test package
```

Das ausführbare JAR liegt anschließend unter `target/shipment-tracker-1.0.0.jar`.

## Beispiel

```powershell
java -jar target/shipment-tracker-1.0.0.jar --input data/sample-shipments.csv --as-of 2026-10-16
```

Eine maschinenlesbare JSON-Ausgabe lässt sich in eine neue Datei schreiben:

```powershell
java -jar target/shipment-tracker-1.0.0.jar --input data/sample-shipments.csv --format json --output build/report.json --as-of 2026-10-16
```

Unterstützte Formate: `text` (Standard), `csv` und `json`. Ohne `--output` wird der Bericht auf der Konsole ausgegeben. Das Eingabedokument wird nie verändert; existierende Ausgabedateien werden nicht überschrieben.

## Eingabeformat

UTF-8 CSV mit fünf Spalten. Komma und Semikolon als Trennzeichen werden erkannt. Jede Zeile beschreibt ein Ereignis; mehrere Zeilen mit derselben Sendungs-ID werden zu einer Sendung zusammengeführt.

```csv
shipment_id,expected_delivery,timestamp,status,location
SHP-1001,2026-10-20,2026-10-15T09:00:00,CREATED,Berlin
SHP-1001,2026-10-20,2026-10-16T11:30:00,IN_TRANSIT,Dortmund
```

Datumsformat: `yyyy-MM-dd`. Zeitstempel: `yyyy-MM-ddTHH:mm:ss`. Leere Zeilen und Kommentarzeilen mit `#` am Anfang werden ignoriert.

Erlaubte Statuswerte:

`CREATED`, `PICKED_UP`, `IN_TRANSIT`, `OUT_FOR_DELIVERY`, `DELIVERED`, `EXCEPTION`

Eine reguläre Statusfolge darf vorwärts gehen oder denselben Status wiederholen. `EXCEPTION` kann zwischen regulären Ereignissen auftreten. Statusrückschritte, Zeitstempel in der Zukunft bezogen auf `--as-of` und mehrfach unterschiedliche Ereignisse zum selben Zeitpunkt werden mit Datensatzbezug gemeldet.

## Bericht und Risikoregel

- `DELIVERED`: Ein Zustellereignis wurde erfasst.
- `LATE`: Das erwartete Lieferdatum ist überschritten und es gibt kein Zustellereignis.
- `DUE_TODAY`: Lieferung ist für den Berichtstag erwartet, aber noch nicht als zugestellt erfasst.
- `ON_TRACK`: Das Lieferdatum liegt nach dem Berichtstag.

Ergebnisse werden reproduzierbar nach erwartetem Lieferdatum und Sendungs-ID sortiert. Jeder Eintrag enthält den Grund der Einstufung.

## Exit-Codes

- `0`: Auswertung erfolgreich (auch bei leerer Eingabedatei)
- `1`: Datei-, Format- oder fachlicher Fehler
- `2`: CLI-Aufruf ungültig

## Projektstruktur

```text
src/main/java/dev/okanyakin/shipmenttracker/  Anwendung und Fachlogik
src/test/java/dev/okanyakin/shipmenttracker/  automatisierte Tests
data/                                         Beispieldaten und Fehlerfall
```

## Grenzen

Das Projekt ist ein lokales Demonstrationswerkzeug. Es verbindet sich nicht mit Versanddiensten, speichert keinen dauerhaften Zustand und verarbeitet keine Zeitzonen. Die CSV-Unterstützung ist bewusst auf einfache, nicht gequotete Eingabespalten ausgelegt; im Bericht werden CSV-Felder korrekt gequotet. Das Berichtsdatum lässt sich über `--as-of` reproduzierbar festlegen.
