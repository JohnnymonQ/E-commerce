CREATE TABLE products (
    id          BIGINT         NOT NULL AUTO_INCREMENT,
    name        VARCHAR(255)   NOT NULL,
    sku         VARCHAR(64)    NOT NULL,
    description TEXT           NULL,
    category    VARCHAR(50)    NOT NULL,
    price       DECIMAL(12, 2) NOT NULL,
    stock       INT            NOT NULL DEFAULT 0,
    weight_kg   DECIMAL(8, 3)  NULL,
    version     BIGINT         NOT NULL DEFAULT 0,
    created_at  DATETIME(6)    NOT NULL,
    updated_at  DATETIME(6)    NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_products_sku UNIQUE (sku),
    CONSTRAINT ck_products_price CHECK (price >= 0),
    CONSTRAINT ck_products_stock CHECK (stock >= 0),
    CONSTRAINT ck_products_weight CHECK (weight_kg IS NULL OR weight_kg >= 0)
);

CREATE INDEX idx_products_category ON products (category);
