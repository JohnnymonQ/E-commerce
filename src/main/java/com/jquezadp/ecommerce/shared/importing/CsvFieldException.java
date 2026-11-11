package com.jquezadp.ecommerce.shared.importing;

import lombok.Getter;

@Getter
public class CsvFieldException extends RuntimeException {

    private final String field;

    public CsvFieldException(String field, String message) {
        super(message);
        this.field = field;
    }
}
