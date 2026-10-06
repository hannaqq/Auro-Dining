package com.aurodining.service;

import com.aurodining.dto.OrderDto;
import com.aurodining.entity.Order;
import org.springframework.data.domain.Page;

public interface OrderService {

    /**
     * Submit a new order (Transaction logic)
     */
    void submit(Order orders);

    /**
     * Re-order: add items from a past order back to shopping cart
     */
    void again(Order orders);

    /**
     * Pagination for order management
     */
    Page<OrderDto> page(int page, int pageSize, Long userId);
}
