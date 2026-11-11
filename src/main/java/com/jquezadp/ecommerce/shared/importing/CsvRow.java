package com.jquezadp.ecommerce.shared.importing;

import org.apache.commons.csv.CSVRecord;

import java.math.BigDecimal;
import java.util.function.Function;

public class CsvRow {

    private final CSVRecord record;

    public CsvRow(CSVRecord record) {
        this.record = record;
    }

    public long line() {
        return record.getRecordNumber() + 1;
    }

    public boolean isBlank() {
        return record.stream().allMatch(value -> value == null || value.isBlank());
    }

    public String get(String header) {
        if (!record.isMapped(header) || !record.isSet(header)) {
            return null;
        }
        String value = record.get(header);
        return value == null || value.isBlank() ? null : value.trim();
    }

    public Integer getInteger(String header) {
        return parse(header, "must be a whole number", Integer::valueOf);
    }

    public BigDecimal getDecimal(String header) {
        return parse(header, "must be a number", BigDecimal::new);
    }

    public BigDecimal getMoney(String header) {
        return parse(header, "must be a number", value -> new BigDecimal(value.startsWith("$") ? value.substring(1).trim() : value));
    }

    private <R> R parse(String header, String message, Function<String, R> parser) {
        String value = get(header);
        if (value == null) {
            return null;
        }
        try {
            return parser.apply(value);
        } catch (NumberFormatException ex) {
            throw new CsvFieldException(header, message);
        }
    }
}
