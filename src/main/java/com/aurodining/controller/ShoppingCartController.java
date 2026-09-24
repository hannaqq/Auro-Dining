package com.aurodining.controller;

import com.aurodining.common.AuthContext;
import com.aurodining.common.R;
import com.aurodining.entity.ShoppingCart;
import com.aurodining.service.ShoppingCartService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller for User Frontend - Shopping Cart
 * Handles shopping cart operations for mobile clients
 */
@RestController
@Slf4j
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;

    /**
     * Get cart list for current user
     */
    @GetMapping("/list")
    public R<List<ShoppingCart>> list() {
        List<ShoppingCart> list = shoppingCartService.list(AuthContext.getCurrentId());
        return R.success(list);
    }

    /**
     * Add item to cart
     */
    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart) {
        shoppingCart.setUserId(AuthContext.getCurrentId());
        ShoppingCart cartItem = shoppingCartService.add(shoppingCart);
        return R.success(cartItem);
    }

    /**
     * Remove one item or decrease number
     */
    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart) {
        shoppingCartService.sub(shoppingCart, AuthContext.getCurrentId());
        return R.success("update success");
    }

    /**
     * Clean all items in cart
     */
    @DeleteMapping("/clean")
    public R<String> clean() {
        shoppingCartService.clean(AuthContext.getCurrentId());
        return R.success("delete success");
    }
}