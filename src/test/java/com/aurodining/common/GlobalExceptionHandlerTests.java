package com.aurodining.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTests {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void customExceptionPreservesBusinessMessage() {
        R<String> response = handler.exceptionHandler(new CustomException("Shopping cart is empty"));

        assertEquals(0, response.getCode());
        assertEquals("Shopping cart is empty", response.getMsg());
    }

    @Test
    void duplicateAndUnexpectedExceptionsReturnSafeMessages() {
        R<String> duplicate = handler.handleGeneralException(
                new IllegalStateException("email already exists"));
        assertEquals("Data conflict: The information already exists.", duplicate.getMsg());

        R<String> unexpected = handler.handleGeneralException(
                new IllegalStateException("database details"));
        assertEquals("An unexpected error occurred. Please try again.", unexpected.getMsg());
    }
}
