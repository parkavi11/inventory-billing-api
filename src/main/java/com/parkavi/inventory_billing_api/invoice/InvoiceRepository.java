package com.parkavi.inventory_billing_api.invoice;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select i from Invoice i where i.id = :id")
    Optional<Invoice> findByIdForUpdate(@Param("id") Long id);

    // Fetch items and products in one query to avoid the N+1 problem on lists
    @EntityGraph(attributePaths = {"items", "items.product", "customer"})
    List<Invoice> findAllByOrderByIdDesc();

    @Query(value = "select nextval('invoice_number_seq')", nativeQuery = true)
    long nextInvoiceSequence();
}