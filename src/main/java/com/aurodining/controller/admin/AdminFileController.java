package com.aurodining.controller.admin;

import com.aurodining.common.R;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/admin/common")
@Slf4j
public class AdminFileController {
    @Value("${auro-dining.path}")
    private String basePath;

    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String fileName = UUID.randomUUID() + suffix;
        File directory = new File(basePath);
        if (!directory.exists()) directory.mkdirs();
        file.transferTo(new File(directory, fileName));
        log.info("Uploaded file {}", fileName);
        return R.success(fileName);
    }
}
