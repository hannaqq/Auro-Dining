package com.aurodining.service;

import com.aurodining.entity.ShoppingCart;
import java.util.List;

public interface ShoppingCartService {
    List<ShoppingCart> list(Long userId);
    ShoppingCart add(ShoppingCart shoppingCart);
    void clean(Long userId);
    void sub(ShoppingCart shoppingCart, Long userId);
}