package com.jquezadp.ecommerce.shared.importing;

import java.util.List;

public record ImportReport(int totalRows, int created, int skipped, int failed, List<RowError> errors) {
}
