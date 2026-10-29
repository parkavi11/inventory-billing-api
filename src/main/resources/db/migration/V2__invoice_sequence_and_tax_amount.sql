-- Invoice numbers come from a sequence so two requests can never get the same number
CREATE SEQUENCE invoice_number_seq START WITH 1 INCREMENT BY 1;

-- Store the tax per line so totals can be audited without recomputing
ALTER TABLE invoice_items ADD COLUMN tax_amount NUMERIC(12,2) NOT NULL DEFAULT 0;