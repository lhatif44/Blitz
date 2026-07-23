package com.blitz.ingestion.csv;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

// wraps one parsed CSV row — header name -> value, with typed accessors.
// blank strings and the literal "NA"/"NaN" (how nflverse marks missing values) become null
public class CsvRow {

    private final Map<String, Integer> headerIndex;
    private final String[] values;

    public CsvRow(Map<String, Integer> headerIndex, String[] values) {
        this.headerIndex = headerIndex;
        this.values = values;
    }

    private String raw(String column) {
        Integer index = headerIndex.get(column);
        if (index == null || index >= values.length) {
            return null;
        }
        String value = values[index];
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        if (trimmed.isEmpty() || trimmed.equalsIgnoreCase("NA") || trimmed.equalsIgnoreCase("NaN")) {
            return null;
        }
        return trimmed;
    }

    public String getString(String column) {
        return raw(column);
    }

    public Integer getInt(String column) {
        String value = raw(column);
        if (value == null) {
            return null;
        }
        // some numeric columns are written as floats (e.g. "4.0") — round rather than fail to parse
        return (int) Math.round(Double.parseDouble(value));
    }

    public BigDecimal getBigDecimal(String column) {
        String value = raw(column);
        return value == null ? null : new BigDecimal(value);
    }

    public LocalDate getLocalDate(String column) {
        String value = raw(column);
        return value == null ? null : LocalDate.parse(value);
    }

    // parses a semicolon-separated list column (e.g. "40;41;53") into individual ints
    public List<Integer> getIntList(String column) {
        String value = raw(column);
        if (value == null) {
            return List.of();
        }
        return Arrays.stream(value.split(";"))
                .map(String::trim)
                .filter(part -> !part.isEmpty())
                .map(part -> (int) Math.round(Double.parseDouble(part)))
                .toList();
    }
}
