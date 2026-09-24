package com.aurodining.service;

import com.aurodining.entity.OrderDetail;
import java.util.List;

public interface OrderDetailService {

    /**
     * Save a list of order details in batch
     */
    void saveBatch(List<OrderDetail> orderDetails);

    /**
     * Get details for a specific order
     */
    List<OrderDetail> getByOrderId(Long orderId);
}
