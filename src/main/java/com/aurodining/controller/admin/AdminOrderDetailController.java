package com.aurodining.controller.admin;

import com.aurodining.common.R;
import com.aurodining.entity.OrderDetail;
import com.aurodining.repository.OrderDetailRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orderDetail")
@RequiredArgsConstructor
public class AdminOrderDetailController {
    private final OrderDetailRepository orderDetailRepository;

    @GetMapping("/{id}")
    public R<OrderDetail> get(@PathVariable Long id) {
        OrderDetail detail = orderDetailRepository.findById(id).orElse(null);
        return detail == null ? R.error("Order detail not found") : R.success(detail);
    }
}
