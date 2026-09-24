package com.aurodining.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@ControllerAdvice(annotations = {RestController.class, Controller.class})
@Slf4j
@ResponseBody
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public R<String> handleGeneralException(Exception ex) {
        log.error("System error: ", ex);

        if (ex.getMessage() != null && ex.getMessage().contains("already exists")) {
            return R.error("Data conflict: The information already exists.");
        }

        return R.error("An unexpected error occurred. Please try again.");
    }

    @ExceptionHandler(CustomException.class)
    public R<String> exceptionHandler(CustomException ex){
        log.error(ex.getMessage());
        return R.error(ex.getMessage());
    }
}

