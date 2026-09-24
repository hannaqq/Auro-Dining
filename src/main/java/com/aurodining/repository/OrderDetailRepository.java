package com.aurodining.repository;

import com.aurodining.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Long> {

    // Find all details for a specific order
    List<OrderDetail> findByOrderId(Long orderId);
}