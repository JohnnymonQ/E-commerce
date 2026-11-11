package com.jquezadp.ecommerce.shared.importing;

import java.util.List;
import java.util.Set;

public interface CsvImporter<T> {

    Set<String> requiredHeaders();

    T map(CsvRow row);

    String keyField();

    String keyOf(T item);

    Set<String> insertNew(List<T> items);
}
