package com.aurodining.controller;

import com.aurodining.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Public file download endpoint.
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${auro-dining.path}")
    private String basePath;

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws IOException {
        try (
                // Use try-with-resources for automatic stream closing
                FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
                ServletOutputStream outputStream = response.getOutputStream()
        ) {
            // Set response type as image
            response.setContentType("image/jpeg");

            int len = 0;
            byte[] bytes = new byte[1024];
            while ((len = fileInputStream.read(bytes)) != -1){
                outputStream.write(bytes, 0, len);
                outputStream.flush();
            }
        } catch (Exception e) {
            log.error("File download error: {}", e.getMessage());
        }
    }
}
