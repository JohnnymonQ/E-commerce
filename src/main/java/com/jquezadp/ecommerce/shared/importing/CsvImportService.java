package com.jquezadp.ecommerce.shared.importing;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@EnableConfigurationProperties(ImportProperties.class)
@RequiredArgsConstructor
public class CsvImportService {

    private static final CSVFormat FORMAT = CSVFormat.DEFAULT.builder()
            .setHeader()
            .setSkipHeaderRecord(true)
            .setIgnoreHeaderCase(true)
            .setIgnoreEmptyLines(true)
            .setTrim(true)
            .get();

    private final Validator validator;
    private final ImportProperties properties;

    public <T> ImportReport importCsv(InputStream input, CsvImporter<T> importer) {
        try (CSVParser parser = FORMAT.parse(new InputStreamReader(BOMInputStream.builder().setInputStream(input).get(), StandardCharsets.UTF_8))) {
            validateHeaders(parser, importer);
            return process(parser, importer);
        } catch (IOException ex) {
            throw new InvalidImportFileException("The file could not be read as CSV", ex);
        } catch (UncheckedIOException | IllegalArgumentException ex) {
            throw new InvalidImportFileException("The file is not a valid CSV: " + ex.getMessage(), ex);
        }
    }

    private <T> void validateHeaders(CSVParser parser, CsvImporter<T> importer) {
        Set<String> present = new HashSet<>();
        parser.getHeaderNames().forEach(header -> present.add(header.trim().toLowerCase()));
        List<String> missing = importer.requiredHeaders().stream()
                .filter(header -> !present.contains(header.toLowerCase()))
                .toList();
        if (!missing.isEmpty()) {
            throw new InvalidImportFileException("Missing required columns: " + String.join(", ", missing));
        }
    }

    private <T> ImportReport process(CSVParser parser, CsvImporter<T> importer) {
        ImportState<T> state = new ImportState<>();
        Map<String, Long> firstLineByKey = new HashMap<>();
        Map<String, Long> lineByAcceptedKey = new HashMap<>();

        for (CSVRecord record : parser) {
            CsvRow row = new CsvRow(record);
            if (row.isBlank()) {
                state.skipped++;
                continue;
            }
            state.totalRows++;
            processRow(row, importer, state, firstLineByKey, lineByAcceptedKey);
            if (state.buffer.size() >= properties.batchSize()) {
                flush(importer, state, lineByAcceptedKey);
            }
        }
        flush(importer, state, lineByAcceptedKey);
        state.errors.sort(Comparator.comparingLong(RowError::line));
        return new ImportReport(state.totalRows, state.created, state.skipped, state.errors.size(), state.errors);
    }

    private <T> void processRow(CsvRow row, CsvImporter<T> importer, ImportState<T> state,
                                Map<String, Long> firstLineByKey, Map<String, Long> lineByAcceptedKey) {
        T item;
        try {
            item = importer.map(row);
        } catch (CsvFieldException ex) {
            state.errors.add(new RowError(row.line(), Map.of(ex.getField(), ex.getMessage())));
            return;
        }

        Map<String, String> violations = new LinkedHashMap<>();
        for (ConstraintViolation<T> violation : validator.validate(item)) {
            violations.putIfAbsent(violation.getPropertyPath().toString(), violation.getMessage());
        }

        String key = importer.keyOf(item);
        if (key != null) {
            Long firstLine = firstLineByKey.putIfAbsent(key, row.line());
            if (firstLine != null) {
                violations.putIfAbsent(importer.keyField(), "duplicated in file, first seen on line " + firstLine);
            }
        }

        if (!violations.isEmpty()) {
            state.errors.add(new RowError(row.line(), violations));
            return;
        }
        state.buffer.add(item);
        lineByAcceptedKey.put(key, row.line());
    }

    private <T> void flush(CsvImporter<T> importer, ImportState<T> state, Map<String, Long> lineByAcceptedKey) {
        if (state.buffer.isEmpty()) {
            return;
        }
        List<T> batch = new ArrayList<>(state.buffer);
        state.buffer.clear();
        Set<String> existing = importer.insertNew(batch);
        for (String key : existing) {
            state.errors.add(new RowError(lineByAcceptedKey.get(key),
                    Map.of(importer.keyField(), "already exists")));
        }
        state.created += batch.size() - existing.size();
        batch.forEach(item -> lineByAcceptedKey.remove(importer.keyOf(item)));
    }

    private static class ImportState<T> {
        private final List<T> buffer = new ArrayList<>();
        private final List<RowError> errors = new ArrayList<>();
        private int totalRows;
        private int created;
        private int skipped;
    }
}
