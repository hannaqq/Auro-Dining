package com.aurodining.controller;

import com.aurodining.common.R;
import com.aurodining.entity.OrderDetail;
import com.aurodining.repository.OrderDetailRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for Backend Management - Order Details
 * Handles order detail queries for backend administrators
 */
@RestController
@Slf4j
@RequestMapping("/orderDetail")
public class OrderDetailController {

    @Autowired
    private OrderDetailRepository orderDetailRepository;

    @GetMapping("/{id}")
    public R<OrderDetail> get(@PathVariable Long id){
        log.info("Querying order detail for id: {}", id);
        OrderDetail orderDetail = orderDetailRepository.findById(id).orElse(null);

        if (orderDetail != null) {
            return R.success(orderDetail);
        }
        return R.error("Order detail not found");
    }
}
