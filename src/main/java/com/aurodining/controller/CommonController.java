package com.aurodining.controller;

import com.aurodining.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * Common Controller for Both Backend Management and User Frontend
 * Handles file upload and download operations used by both admin and user interfaces
 */
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${auro-dining.path}")
    private String basePath;

    /**
     * File Upload
     * @param file MultipartFile from frontend
     * @return R<String> containing the generated filename
     */
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        log.info("Uploading file to Pictures folder...");

        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        String fileName = UUID.randomUUID().toString() + suffix;

        File dir = new File(basePath);
        if(!dir.exists()){
            dir.mkdirs();
        }

        file.transferTo(new File(dir, fileName));

        return R.success(fileName);
    }

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