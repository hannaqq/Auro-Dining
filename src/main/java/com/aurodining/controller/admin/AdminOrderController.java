package com.aurodining.controller.admin;

import com.aurodining.common.R;
import com.aurodining.entity.Order;
import com.aurodining.repository.OrderRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/order")
@RequiredArgsConstructor
public class AdminOrderController {
    private final OrderRepository orderRepository;

    @GetMapping("/page")
    public R<Map<String, Object>> getPage(int page, int pageSize, String number,
                                          String beginTime, String endTime) {
        PageRequest pageRequest = PageRequest.of(page - 1, pageSize, Sort.by("orderTime").descending());
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        Specification<Order> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (number != null && !number.isEmpty()) predicates.add(cb.equal(root.get("number"), number));
            if (beginTime != null && !beginTime.isEmpty()) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("checkoutTime"), LocalDateTime.parse(beginTime, formatter)));
            }
            if (endTime != null && !endTime.isEmpty()) {
                predicates.add(cb.lessThanOrEqualTo(root.get("checkoutTime"), LocalDateTime.parse(endTime, formatter)));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<Order> orders = orderRepository.findAll(spec, pageRequest);
        Map<String, Object> result = new HashMap<>();
        result.put("records", orders.getContent());
        result.put("total", orders.getTotalElements());
        return R.success(result);
    }

    @PutMapping
    public R<String> editStatus(@RequestBody Order order) {
        Order existing = orderRepository.findById(order.getId()).orElse(null);
        if (existing != null) {
            existing.setStatus(order.getStatus());
            orderRepository.save(existing);
        }
        return R.success("edit status success");
    }
}
