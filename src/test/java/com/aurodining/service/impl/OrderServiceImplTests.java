package com.aurodining.service.impl;

import com.aurodining.common.AuthContext;
import com.aurodining.common.CustomException;
import com.aurodining.entity.AddressBook;
import com.aurodining.entity.Order;
import com.aurodining.entity.OrderDetail;
import com.aurodining.entity.ShoppingCart;
import com.aurodining.entity.User;
import com.aurodining.repository.OrderDetailRepository;
import com.aurodining.repository.OrderRepository;
import com.aurodining.service.AddressBookService;
import com.aurodining.service.ShoppingCartService;
import com.aurodining.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTests {

    @Mock private OrderRepository orderRepository;
    @Mock private OrderDetailRepository orderDetailRepository;
    @Mock private ShoppingCartService shoppingCartService;
    @Mock private UserService userService;
    @Mock private AddressBookService addressBookService;

    private OrderServiceImpl orderService;

    @BeforeEach
    void setUp() {
        orderService = new OrderServiceImpl(
                orderRepository, orderDetailRepository, shoppingCartService,
                userService, addressBookService);
        AuthContext.setCurrentId(7L);
    }

    @AfterEach
    void tearDown() {
        AuthContext.removeCurrentId();
    }

    @Test
    void submitRejectsEmptyCart() {
        when(shoppingCartService.list(7L)).thenReturn(List.of());

        CustomException exception = assertThrows(
                CustomException.class, () -> orderService.submit(new Order()));

        assertEquals("Shopping cart is empty", exception.getMessage());
        verifyNoInteractions(orderRepository, orderDetailRepository);
    }

    @Test
    void submitRejectsMissingUserOrAddress() {
        when(shoppingCartService.list(7L)).thenReturn(List.of(cart(10L, null, "2.50", 1)));
        when(userService.getById(7L)).thenReturn(null);

        assertEquals("User not found",
                assertThrows(CustomException.class, () -> orderService.submit(new Order())).getMessage());

        User user = new User();
        user.setId(7L);
        when(userService.getById(7L)).thenReturn(user);
        Order order = new Order();
        order.setAddressBookId(99L);
        when(addressBookService.getById(99L)).thenReturn(null);

        assertEquals("Address not found",
                assertThrows(CustomException.class, () -> orderService.submit(order)).getMessage());
        verifyNoInteractions(orderRepository, orderDetailRepository);
    }

    @Test
    void submitCalculatesAmountPersistsDetailsAndCleansCart() {
        ShoppingCart dish = cart(10L, null, "3.25", 2);
        dish.setName("Burger");
        ShoppingCart combo = cart(null, 20L, "8.50", 1);
        combo.setName("Combo");
        when(shoppingCartService.list(7L)).thenReturn(List.of(dish, combo));

        User user = new User();
        user.setId(7L);
        user.setName("Ada");
        when(userService.getById(7L)).thenReturn(user);

        AddressBook address = new AddressBook();
        address.setId(3L);
        address.setConsignee("Ada");
        address.setPhone("555-0100");
        address.setStreetAddress("1 Main St");
        address.setCity("Atlanta");
        address.setState("GA");
        address.setZipCode("30332");
        when(addressBookService.getById(3L)).thenReturn(address);

        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            saved.setId(100L);
            return saved;
        });

        Order order = new Order();
        order.setAddressBookId(3L);
        orderService.submit(order);

        assertEquals(0, new BigDecimal("15.00").compareTo(order.getAmount()));
        assertEquals(7L, order.getUserId());
        assertEquals("Ada", order.getUserName());
        assertEquals("1 Main St, Atlanta, GA 30332", order.getAddress());
        assertEquals(2, order.getStatus());
        assertNotNull(order.getNumber());
        assertNotNull(order.getOrderTime());

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<OrderDetail>> detailsCaptor = ArgumentCaptor.forClass(List.class);
        verify(orderDetailRepository).saveAll(detailsCaptor.capture());
        List<OrderDetail> details = detailsCaptor.getValue();
        assertEquals(2, details.size());
        assertTrue(details.stream().allMatch(detail -> detail.getOrderId().equals(100L)));
        assertEquals("Burger", details.get(0).getName());
        verify(shoppingCartService).clean(7L);
    }

    @Test
    void againCopiesOrderDetailsIntoCurrentUsersCart() {
        OrderDetail detail = new OrderDetail();
        detail.setName("Salmon");
        detail.setDishId(12L);
        detail.setNumber(2);
        detail.setAmount(new BigDecimal("9.00"));
        when(orderDetailRepository.findByOrderId(55L)).thenReturn(List.of(detail));

        Order order = new Order();
        order.setId(55L);
        orderService.again(order);

        ArgumentCaptor<ShoppingCart> captor = ArgumentCaptor.forClass(ShoppingCart.class);
        verify(shoppingCartService).add(captor.capture());
        assertEquals(7L, captor.getValue().getUserId());
        assertEquals(12L, captor.getValue().getDishId());
        assertEquals(2, captor.getValue().getNumber());
        assertNotNull(captor.getValue().getCreateTime());
    }

    private ShoppingCart cart(Long dishId, Long comboId, String amount, int number) {
        ShoppingCart cart = new ShoppingCart();
        cart.setUserId(7L);
        cart.setDishId(dishId);
        cart.setComboId(comboId);
        cart.setAmount(new BigDecimal(amount));
        cart.setNumber(number);
        return cart;
    }
}
