CREATE TABLE users (
                       id            BIGSERIAL PRIMARY KEY,
                       username      VARCHAR(50)  NOT NULL UNIQUE,
                       password_hash VARCHAR(100) NOT NULL,
                       role          VARCHAR(20)  NOT NULL CHECK (role IN ('ADMIN', 'CASHIER')),
                       enabled       BOOLEAN      NOT NULL DEFAULT TRUE,
                       created_at    TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE products (
                          id            BIGSERIAL PRIMARY KEY,
                          sku           VARCHAR(50)   NOT NULL UNIQUE,
                          name          VARCHAR(200)  NOT NULL,
                          description   TEXT,
                          unit_price    NUMERIC(12,2) NOT NULL CHECK (unit_price >= 0),
                          tax_rate      NUMERIC(5,2)  NOT NULL DEFAULT 13.00,   -- Ontario HST
                          reorder_level INT           NOT NULL DEFAULT 0,
                          active        BOOLEAN       NOT NULL DEFAULT TRUE,
                          created_at    TIMESTAMPTZ   NOT NULL DEFAULT now()
);

-- Stock is never overwritten. Current stock = SUM(quantity_change).
CREATE TABLE stock_movements (
                                 id              BIGSERIAL PRIMARY KEY,
                                 product_id      BIGINT      NOT NULL REFERENCES products(id),
                                 quantity_change INT         NOT NULL CHECK (quantity_change <> 0),
                                 reason          VARCHAR(20) NOT NULL
                                     CHECK (reason IN ('PURCHASE','SALE','ADJUSTMENT','RETURN')),
                                 reference       VARCHAR(100),
                                 created_by      BIGINT REFERENCES users(id),
                                 created_at      TIMESTAMPTZ NOT NULL DEFAULT now()
);
CREATE INDEX idx_stock_movements_product ON stock_movements(product_id);

CREATE TABLE customers (
                           id         BIGSERIAL PRIMARY KEY,
                           name       VARCHAR(200) NOT NULL,
                           email      VARCHAR(200),
                           phone      VARCHAR(30),
                           created_at TIMESTAMPTZ  NOT NULL DEFAULT now()
);

CREATE TABLE invoices (
                          id             BIGSERIAL PRIMARY KEY,
                          invoice_number VARCHAR(30)   NOT NULL UNIQUE,
                          customer_id    BIGINT REFERENCES customers(id),
                          status         VARCHAR(20)   NOT NULL DEFAULT 'DRAFT'
                              CHECK (status IN ('DRAFT','CONFIRMED','CANCELLED')),
                          subtotal       NUMERIC(12,2) NOT NULL DEFAULT 0,
                          tax_total      NUMERIC(12,2) NOT NULL DEFAULT 0,
                          total          NUMERIC(12,2) NOT NULL DEFAULT 0,
                          created_by     BIGINT REFERENCES users(id),
                          created_at     TIMESTAMPTZ   NOT NULL DEFAULT now(),
                          confirmed_at   TIMESTAMPTZ
);

CREATE TABLE invoice_items (
                               id         BIGSERIAL PRIMARY KEY,
                               invoice_id BIGINT        NOT NULL REFERENCES invoices(id) ON DELETE CASCADE,
                               product_id BIGINT        NOT NULL REFERENCES products(id),
                               quantity   INT           NOT NULL CHECK (quantity > 0),
                               unit_price NUMERIC(12,2) NOT NULL,   -- price snapshot at time of sale
                               tax_rate   NUMERIC(5,2)  NOT NULL,
                               line_total NUMERIC(12,2) NOT NULL
);
CREATE INDEX idx_invoice_items_invoice ON invoice_items(invoice_id);