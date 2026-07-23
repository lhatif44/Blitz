package com.blitz.ingestion.csv;

import java.util.LinkedHashMap;
import java.util.Map;

// builds a fixture CsvRow from a plain column-name -> value map, for tests that don't
// want to go through CsvTable.parse() against a real CSV file
public final class CsvRowTestSupport {

    private CsvRowTestSupport() {
    }

    public static CsvRow row(Map<String, String> columns) {
        Map<String, Integer> headerIndex = new LinkedHashMap<>();
        String[] values = new String[columns.size()];
        int i = 0;
        for (Map.Entry<String, String> entry : columns.entrySet()) {
            headerIndex.put(entry.getKey(), i);
            values[i] = entry.getValue();
            i++;
        }
        return new CsvRow(headerIndex, values);
    }
}
