package com.jquezadp.ecommerce.shared.importing;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CsvImportServiceTests {

    private static final Validator VALIDATOR = Validation.buildDefaultValidatorFactory().getValidator();

    private final FakeImporter importer = new FakeImporter();

    private ImportReport run(String csv, int batchSize) {
        CsvImportService service = new CsvImportService(VALIDATOR, new ImportProperties(batchSize));
        return service.importCsv(new ByteArrayInputStream(csv.getBytes(StandardCharsets.UTF_8)), importer);
    }

    @Test
    void importsQuotedValuesAndSkipsBlankRows() {
        String csv = """
                code,label,amount
                A-1,"Comma, inside",$5.50
                ,,
                A-2,"Quote \"\"inside\"\"",3
                """;

        ImportReport report = run(csv, 10);

        assertThat(report.totalRows()).isEqualTo(2);
        assertThat(report.created()).isEqualTo(2);
        assertThat(report.skipped()).isEqualTo(1);
        assertThat(report.failed()).isZero();
        assertThat(importer.inserted).extracting(Item::label).containsExactly("Comma, inside", "Quote \"inside\"");
        assertThat(importer.inserted.get(0).amount()).isEqualTo(5);
    }

    @Test
    void reportsParseAndValidationErrorsWithLineNumbers() {
        String csv = """
                code,label,amount
                A-1,Fine,1
                A-2,Bad,free
                A-3,   ,2
                A-4,Negative,-1
                """;

        ImportReport report = run(csv, 10);

        assertThat(report.created()).isEqualTo(1);
        assertThat(report.failed()).isEqualTo(3);
        assertThat(report.errors()).extracting(RowError::line).containsExactly(3L, 4L, 5L);
        assertThat(report.errors().get(0).errors()).containsKey("amount");
        assertThat(report.errors().get(1).errors()).containsKey("label");
        assertThat(report.errors().get(2).errors()).containsKey("amount");
    }

    @Test
    void rejectsDuplicatedKeysInsideFileKeepingFirstOccurrence() {
        String csv = """
                code,label,amount
                A-1,First,1
                A-1,Second,2
                """;

        ImportReport report = run(csv, 10);

        assertThat(report.created()).isEqualTo(1);
        assertThat(report.errors()).hasSize(1);
        assertThat(report.errors().get(0).line()).isEqualTo(3);
        assertThat(report.errors().get(0).errors().get("code")).contains("line 2");
        assertThat(importer.inserted).extracting(Item::label).containsExactly("First");
    }

    @Test
    void reportsKeysThatAlreadyExist() {
        importer.existing.add("A-2");
        String csv = """
                code,label,amount
                A-1,One,1
                A-2,Two,2
                """;

        ImportReport report = run(csv, 10);

        assertThat(report.created()).isEqualTo(1);
        assertThat(report.errors()).hasSize(1);
        assertThat(report.errors().get(0).line()).isEqualTo(3);
        assertThat(report.errors().get(0).errors().get("code")).isEqualTo("already exists");
    }

    @Test
    void writesInBatchesOfConfiguredSize() {
        StringBuilder csv = new StringBuilder("code,label,amount\n");
        for (int i = 1; i <= 5; i++) {
            csv.append("A-").append(i).append(",Item,1\n");
        }

        ImportReport report = run(csv.toString(), 2);

        assertThat(report.created()).isEqualTo(5);
        assertThat(importer.batchSizes).containsExactly(2, 2, 1);
    }

    @Test
    void failsWhenRequiredColumnIsMissing() {
        assertThatThrownBy(() -> run("code,label\nA-1,One\n", 10))
                .isInstanceOf(InvalidImportFileException.class)
                .hasMessageContaining("amount");
    }

    @Test
    void failsWhenFileIsEmpty() {
        assertThatThrownBy(() -> run("", 10)).isInstanceOf(InvalidImportFileException.class);
    }

    record Item(@NotBlank String code, @NotBlank String label, @PositiveOrZero Integer amount) {
    }

    static class FakeImporter implements CsvImporter<Item> {

        final Set<String> existing = new HashSet<>();
        final List<Item> inserted = new ArrayList<>();
        final List<Integer> batchSizes = new ArrayList<>();

        @Override
        public Set<String> requiredHeaders() {
            return Set.of("code", "label", "amount");
        }

        @Override
        public Item map(CsvRow row) {
            return new Item(row.get("code"), row.get("label"), row.getMoney("amount") == null ? null : row.getMoney("amount").intValue());
        }

        @Override
        public String keyField() {
            return "code";
        }

        @Override
        public String keyOf(Item item) {
            return item.code();
        }

        @Override
        public Set<String> insertNew(List<Item> items) {
            batchSizes.add(items.size());
            Set<String> skipped = new HashSet<>();
            for (Item item : items) {
                if (existing.contains(item.code())) {
                    skipped.add(item.code());
                } else {
                    inserted.add(item);
                }
            }
            return skipped;
        }
    }
}
