package com.aurodining.controller;

import lombok.RequiredArgsConstructor;

import com.aurodining.common.AuthContext;
import com.aurodining.common.R;
import com.aurodining.dto.OrderDto;
import com.aurodining.entity.OrderDetail;
import com.aurodining.entity.Order;
import com.aurodining.repository.OrderDetailRepository;
import com.aurodining.repository.OrderRepository;
import com.aurodining.service.OrderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Customer order endpoints.
 */
@RestController
@Slf4j
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;

    private final OrderRepository orderRepository;

    private final OrderDetailRepository orderDetailRepository;

    /**
     * User Frontend: Get user order history with pagination
     */
    @GetMapping("/userPage")
    public R<Map<String, Object>> getUserPage(int page, int pageSize){
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by("orderTime").descending());

        Page<Order> ordersPage = orderRepository.findByUserId(AuthContext.getCurrentId(), pageRequest);

        List<OrderDto> dtoList = ordersPage.getContent().stream().map(order -> {
            OrderDto dto = new OrderDto();
            BeanUtils.copyProperties(order, dto);

            List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId());
            dto.setOrderDetails(details);

            return dto;
        }).collect(Collectors.toList());

        Map<String, Object> pageData = new HashMap<>();
        pageData.put("records", dtoList);
        pageData.put("total", ordersPage.getTotalElements());

        return R.success(pageData);
    }

    /**
     * User Frontend: Submit order
     */
    @PostMapping("/submit")
    public R<String> submit(@RequestBody Order orders){
        orderService.submit(orders);
        return R.success("submit success");
    }

    /**
     * User Frontend: Reorder (add items from previous order to cart)
     */
    @PostMapping("/again")
    public R<String> again(@RequestBody Order orders){
        orderService.again(orders);
        return R.success("success");
    }

}
