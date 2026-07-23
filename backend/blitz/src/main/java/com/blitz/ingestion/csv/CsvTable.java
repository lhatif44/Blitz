package com.blitz.ingestion.csv;

import com.blitz.exception.IngestionException;
import com.opencsv.CSVReader;
import com.opencsv.exceptions.CsvValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// parses a raw CSV stream into rows keyed by header name — the first line is always the header
public class CsvTable {

    private final List<CsvRow> rows;

    private CsvTable(List<CsvRow> rows) {
        this.rows = rows;
    }

    public List<CsvRow> rows() {
        return rows;
    }

    public static CsvTable parse(InputStream inputStream) {
        try (CSVReader reader = new CSVReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String[] header = reader.readNext();
            if (header == null) {
                return new CsvTable(List.of());
            }

            Map<String, Integer> headerIndex = new HashMap<>();
            for (int i = 0; i < header.length; i++) {
                headerIndex.put(header[i].trim(), i);
            }

            List<CsvRow> rows = new ArrayList<>();
            String[] line;
            while ((line = reader.readNext()) != null) {
                rows.add(new CsvRow(headerIndex, line));
            }
            return new CsvTable(rows);
        } catch (IOException | CsvValidationException e) {
            throw new IngestionException("Failed to parse CSV", e);
        }
    }
}
