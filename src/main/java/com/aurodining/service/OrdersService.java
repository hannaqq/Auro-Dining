package com.aurodining.service;

import com.aurodining.dto.OrdersDto;
import com.aurodining.entity.Orders;
import org.springframework.data.domain.Page;

public interface OrdersService {

    /**
     * Submit a new order (Transaction logic)
     */
    void submit(Orders orders);

    /**
     * Re-order: add items from a past order back to shopping cart
     */
    void again(Orders orders);

    /**
     * Pagination for order management
     */
    Page<OrdersDto> page(int page, int pageSize, Long userId);
}
