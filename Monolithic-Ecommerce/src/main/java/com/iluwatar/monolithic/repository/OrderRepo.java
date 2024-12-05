package com.iluwatar.monolithic.repository;

import com.iluwatar.monolithic.model.Orders;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 * This interface allows JpaRepository to generate queries for the required tables.
 */
public interface OrderRepo extends JpaRepository<Orders, Long> {
}
