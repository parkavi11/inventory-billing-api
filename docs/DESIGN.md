# Design Notes

## Problem
A small retail business needs to track stock levels and issue invoices
without overselling or losing history.

## Core tables
- `products`: catalog with price, tax rate and reorder level
- `stock_movements`: append-only ledger of every stock change
- `customers`, `invoices`, `invoice_items`: billing
- `users`: login accounts with ADMIN or CASHIER role

## Key decisions
1. **Stock as movements, not a quantity column.** Current stock is the sum
   of movements. This gives a full audit trail and avoids lost updates
   when two cashiers sell at once.
2. **Price and tax snapshots on invoice items.** Changing a product's
   price later never alters past invoices.
3. **NUMERIC for money.** Floating-point types cause rounding errors.
4. **CHECK constraints in the database.** The DB rejects invalid data
   even if the application has a bug.
5. **Flyway owns the schema.** Hibernate only validates it
   (`ddl-auto: validate`), so every schema change is a versioned migration.

## Trade-offs
- Summing movements gets slower as rows grow. The index on
  `stock_movements(product_id)` covers this for now; a cached snapshot
  quantity is a future option at larger scale.