package com.aurodining.service.impl;

import com.aurodining.entity.ShoppingCart;
import com.aurodining.repository.ShoppingCartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShoppingCartServiceImplTests {

    @Mock
    private ShoppingCartRepository repository;

    private ShoppingCartServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ShoppingCartServiceImpl(repository);
    }

    @Test
    void addIncrementsMatchingDishWithSameFlavor() {
        returnSavedArgument();
        ShoppingCart request = cart(5L, 10L, null, "spicy", null);
        ShoppingCart existing = cart(5L, 10L, null, "spicy", 2);
        when(repository.findByUserIdAndDishIdAndDishFlavor(5L, 10L, "spicy"))
                .thenReturn(existing);

        ShoppingCart result = service.add(request);

        assertEquals(3, result.getNumber());
        verify(repository).save(existing);
    }

    @Test
    void addIncrementsMatchingCombo() {
        returnSavedArgument();
        ShoppingCart request = cart(5L, null, 20L, null, null);
        ShoppingCart existing = cart(5L, null, 20L, null, 1);
        when(repository.findByUserIdAndComboId(5L, 20L)).thenReturn(existing);

        assertEquals(2, service.add(request).getNumber());
        verify(repository).save(existing);
    }

    @Test
    void addCreatesNewItemWithInitialNumberAndTime() {
        returnSavedArgument();
        ShoppingCart request = cart(5L, 10L, null, "mild", null);
        when(repository.findByUserIdAndDishIdAndDishFlavor(5L, 10L, "mild"))
                .thenReturn(null);

        ShoppingCart result = service.add(request);

        assertEquals(1, result.getNumber());
        assertNotNull(result.getCreateTime());
        verify(repository).save(request);
    }

    @Test
    void subDecrementsOrDeletesMatchingItem() {
        ShoppingCart request = cart(null, null, 20L, null, null);
        ShoppingCart existing = cart(5L, null, 20L, null, 2);
        when(repository.findByUserIdAndComboId(5L, 20L)).thenReturn(existing);

        service.sub(request, 5L);
        assertEquals(1, existing.getNumber());
        verify(repository).save(existing);

        reset(repository);
        existing.setNumber(1);
        when(repository.findByUserIdAndComboId(5L, 20L)).thenReturn(existing);
        service.sub(request, 5L);
        verify(repository).delete(existing);
        verify(repository, never()).save(any());
    }

    @Test
    void subDoesNothingWhenItemDoesNotExistAndCleanUsesCurrentUserId() {
        ShoppingCart request = cart(null, 10L, null, null, null);

        service.sub(request, 5L);
        verify(repository, never()).save(any());
        verify(repository, never()).delete(any());

        service.clean(5L);
        verify(repository).deleteByUserId(5L);
    }

    private ShoppingCart cart(
            Long userId, Long dishId, Long comboId, String flavor, Integer number) {
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(userId);
        cart.setDishId(dishId);
        cart.setComboId(comboId);
        cart.setDishFlavor(flavor);
        cart.setNumber(number);
        return cart;
    }

    private void returnSavedArgument() {
        when(repository.save(any(ShoppingCart.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }
}
