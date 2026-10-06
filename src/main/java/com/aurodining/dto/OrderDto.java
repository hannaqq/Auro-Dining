package com.aurodining.dto;

import com.aurodining.entity.OrderDetail;
import com.aurodining.entity.Order;
import lombok.Data;

import java.util.List;

@Data
public class OrderDto extends Order {

    private List<OrderDetail> orderDetails;

    private String userName;

    private String address;
}
