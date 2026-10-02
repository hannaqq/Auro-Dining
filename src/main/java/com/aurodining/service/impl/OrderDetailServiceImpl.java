package com.aurodining.service.impl;

import lombok.RequiredArgsConstructor;

import com.aurodining.entity.OrderDetail;
import com.aurodining.repository.OrderDetailRepository;
import com.aurodining.service.OrderDetailService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderDetailServiceImpl implements OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;

    @Override
    @Transactional
    public void saveBatch(List<OrderDetail> orderDetails) {
        orderDetailRepository.saveAll(orderDetails);
    }

    @Override
    public List<OrderDetail> getByOrderId(Long orderId) {
        return orderDetailRepository.findByOrderId(orderId);
    }
}