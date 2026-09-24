package com.aurodining.dto;

import com.aurodining.entity.OrderDetail;
import com.aurodining.entity.Orders;
import lombok.Data;

import java.util.List;

@Data
public class OrdersDto extends Orders {

    private List<OrderDetail> orderDetails;

    private String userName;

    private String address;
}
