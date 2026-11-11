package com.jquezadp.ecommerce.shared.importing;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties("app.import")
public record ImportProperties(@DefaultValue("500") int batchSize) {
}
