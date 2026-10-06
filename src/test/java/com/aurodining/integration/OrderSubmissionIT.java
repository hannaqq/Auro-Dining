package com.aurodining.integration;

import com.aurodining.common.AppJwtUtil;
import com.aurodining.entity.AddressBook;
import com.aurodining.entity.Order;
import com.aurodining.entity.OrderDetail;
import com.aurodining.entity.User;
import com.aurodining.repository.AddressBookRepository;
import com.aurodining.repository.OrderDetailRepository;
import com.aurodining.repository.OrderRepository;
import com.aurodining.repository.ShoppingCartRepository;
import com.aurodining.repository.UserRepository;
import com.aurodining.support.IntegrationTestContainers;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class OrderSubmissionIT extends IntegrationTestContainers {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private AddressBookRepository addressBookRepository;
    @Autowired private ShoppingCartRepository shoppingCartRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;

    @AfterEach
    void cleanData() {
        orderDetailRepository.deleteAll();
        orderRepository.deleteAll();
        shoppingCartRepository.deleteAll();
        addressBookRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void authenticatedUserCanAddCartItemSubmitOrderAndReadHistory() throws Exception {
        User user = new User();
        user.setName("Integration User");
        user.setEmail("order-integration@example.com");
        user.setStatus(1);
        user = userRepository.save(user);

        AddressBook address = new AddressBook();
        address.setUserId(user.getId());
        address.setConsignee("Integration User");
        address.setPhone("555-0100");
        address.setStreetAddress("1 Test St");
        address.setCity("Atlanta");
        address.setState("GA");
        address.setZipCode("30332");
        address = addressBookRepository.save(address);

        Cookie authCookie = new Cookie(
                "Auth-Token", AppJwtUtil.getToken(user.getId(), AppJwtUtil.ROLE_USER));

        mockMvc.perform(post("/shoppingCart/add")
                        .servletPath("/shoppingCart/add")
                        .cookie(authCookie)
                        .contentType("application/json")
                        .content("""
                                {
                                  "dishId": 101,
                                  "name": "Test Dish",
                                  "dishFlavor": "mild",
                                  "number": 99,
                                  "amount": 12.50
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.number").value(1));

        mockMvc.perform(post("/order/submit")
                        .servletPath("/order/submit")
                        .cookie(authCookie)
                        .contentType("application/json")
                        .content("{\"addressBookId\":" + address.getId() + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1));

        List<Order> orders = orderRepository
                .findByUserId(user.getId(), PageRequest.of(0, 10))
                .getContent();
        assertEquals(1, orders.size());
        Order order = orders.get(0);
        assertEquals(0, new BigDecimal("12.50").compareTo(order.getAmount()));
        assertEquals("1 Test St, Atlanta, GA 30332", order.getAddress());

        List<OrderDetail> details = orderDetailRepository.findByOrderId(order.getId());
        assertEquals(1, details.size());
        assertEquals("Test Dish", details.get(0).getName());
        assertEquals(1, details.get(0).getNumber());
        assertTrue(shoppingCartRepository.findByUserIdOrderByCreateTimeAsc(user.getId()).isEmpty());

        mockMvc.perform(get("/order/userPage")
                        .servletPath("/order/userPage")
                        .cookie(authCookie)
                        .param("page", "1")
                        .param("pageSize", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(1))
                .andExpect(jsonPath("$.data.total").value(1))
                .andExpect(jsonPath("$.data.records[0].orderDetails[0].name")
                        .value("Test Dish"));
    }
}
