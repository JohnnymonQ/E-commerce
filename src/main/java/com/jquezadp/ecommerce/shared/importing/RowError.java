package com.jquezadp.ecommerce.shared.importing;

import java.util.Map;

public record RowError(long line, Map<String, String> errors) {
}
