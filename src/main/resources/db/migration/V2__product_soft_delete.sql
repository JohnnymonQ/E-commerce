ALTER TABLE products ADD COLUMN deleted BOOLEAN NOT NULL DEFAULT FALSE;

-- NULL for deleted rows, so the unique index only covers active products
ALTER TABLE products ADD COLUMN active_sku VARCHAR(64) AS (IF(deleted, NULL, sku)) VIRTUAL;

ALTER TABLE products DROP INDEX uk_products_sku;
CREATE UNIQUE INDEX uk_products_active_sku ON products (active_sku);
CREATE INDEX idx_products_sku ON products (sku);
