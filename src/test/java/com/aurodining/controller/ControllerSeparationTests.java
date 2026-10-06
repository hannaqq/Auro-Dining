package com.aurodining.controller;

import com.aurodining.controller.admin.*;
import org.junit.jupiter.api.Test;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.lang.reflect.Method;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

class ControllerSeparationTests {
    private static final List<Class<?>> CONTROLLERS = List.of(
            AddressBookController.class, CategoryController.class, ComboController.class,
            CommonController.class, DishController.class, HealthController.class,
            OrderController.class, ShoppingCartController.class, UserController.class,
            AdminCategoryController.class, AdminComboController.class, AdminDishController.class,
            AdminEmployeeController.class, AdminFileController.class,
            AdminOrderController.class, AdminOrderDetailController.class
    );

    @Test
    void controllerMappingsHaveExpectedMethodAndPath() {
        Set<String> mappings = collectMappings();

        assertTrue(mappings.contains("GET /admin/dish/page"));
        assertTrue(mappings.contains("POST /admin/dish"));
        assertTrue(mappings.contains("PUT /admin/dish"));
        assertTrue(mappings.contains("DELETE /admin/dish"));
        assertTrue(mappings.contains("POST /admin/dish/status/{status}"));
        assertTrue(mappings.contains("GET /admin/combo/page"));
        assertTrue(mappings.contains("GET /admin/category/page"));
        assertTrue(mappings.contains("GET /admin/order/page"));
        assertTrue(mappings.contains("GET /admin/orderDetail/{id}"));
        assertTrue(mappings.contains("POST /admin/employee/login"));
        assertTrue(mappings.contains("POST /admin/employee/logout"));
        assertTrue(mappings.contains("POST /admin/common/upload"));

        assertTrue(mappings.contains("GET /dish/list"));
        assertTrue(mappings.contains("GET /combo/list"));
        assertTrue(mappings.contains("GET /combo/dish/{id}"));
        assertTrue(mappings.contains("GET /category/list"));
        assertTrue(mappings.contains("GET /order/userPage"));
        assertTrue(mappings.contains("POST /order/submit"));
        assertTrue(mappings.contains("POST /order/again"));

        assertFalse(mappings.contains("GET /dish/page"));
        assertFalse(mappings.contains("GET /combo/page"));
        assertFalse(mappings.contains("GET /category/page"));
        assertFalse(mappings.contains("GET /order/page"));
        assertFalse(mappings.contains("POST /employee/login"));
        assertFalse(mappings.contains("POST /common/upload"));
    }

    @Test
    void controllerMappingsAreUnique() {
        List<String> mappings = collectMappingList();
        Set<String> uniqueMappings = new HashSet<>(mappings);
        assertEquals(uniqueMappings.size(), mappings.size(), "Duplicate controller mapping found");
    }

    @Test
    void cacheAnnotationsRemainOnQueriesAndEveryAdminMutation() throws Exception {
        assertNotNull(DishController.class.getMethod("getDishList", com.aurodining.entity.Dish.class)
                .getAnnotation(Cacheable.class));
        assertNotNull(ComboController.class.getMethod("getList", com.aurodining.entity.Combo.class)
                .getAnnotation(Cacheable.class));

        assertCacheEvict(AdminDishController.class, "save", com.aurodining.dto.DishDto.class);
        assertCacheEvict(AdminDishController.class, "update", com.aurodining.dto.DishDto.class);
        assertCacheEvict(AdminDishController.class, "changeStatus", List.class, Integer.class);
        assertCacheEvict(AdminDishController.class, "delete", List.class);
        assertCacheEvict(AdminComboController.class, "save", com.aurodining.dto.ComboDto.class);
        assertCacheEvict(AdminComboController.class, "update", com.aurodining.dto.ComboDto.class);
        assertCacheEvict(AdminComboController.class, "updateStatus", List.class, Integer.class);
        assertCacheEvict(AdminComboController.class, "delete", List.class);
    }

    private Set<String> collectMappings() {
        return new HashSet<>(collectMappingList());
    }

    private List<String> collectMappingList() {
        List<String> mappings = new ArrayList<>();
        for (Class<?> controller : CONTROLLERS) {
            RequestMapping classMapping = AnnotatedElementUtils.findMergedAnnotation(controller, RequestMapping.class);
            String basePath = firstPath(classMapping);
            for (Method method : controller.getDeclaredMethods()) {
                RequestMapping methodMapping = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
                if (methodMapping == null) continue;
                String path = normalize(basePath + firstPath(methodMapping));
                for (RequestMethod requestMethod : methodMapping.method()) {
                    mappings.add(requestMethod.name() + " " + path);
                }
            }
        }
        return mappings;
    }

    private String firstPath(RequestMapping mapping) {
        if (mapping == null || mapping.value().length == 0) return "";
        return mapping.value()[0];
    }

    private String normalize(String path) {
        return path.isEmpty() ? "/" : path.replaceAll("/{2,}", "/");
    }

    private void assertCacheEvict(Class<?> type, String methodName, Class<?>... parameterTypes)
            throws NoSuchMethodException {
        CacheEvict annotation = type.getMethod(methodName, parameterTypes).getAnnotation(CacheEvict.class);
        assertNotNull(annotation, type.getSimpleName() + "." + methodName + " must evict its cache");
        assertTrue(annotation.allEntries());
    }
}
