package com.example.reggie.controller;

import com.example.reggie.common.R;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.UUID;

@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;

    /**
     * 文件上传
     * @param file
     * @return
     */
    @RequestMapping("/upload")
    public R<String> upload(MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String name = UUID.randomUUID().toString();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf('.'));
        //
        name = name + suffix;

        // create dir
        File dir = new File(basePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        // save file
        try {
            file.transferTo(new File(basePath + name));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return R.success(name);
    }

    /**
     * 文件下载
     * @param response
     * @param name
     */
    @RequestMapping("/download")
    public void download(HttpServletResponse response, @RequestParam String name) {
        // InputStream read
        try {
            FileInputStream fileInputStream = new FileInputStream(new File(basePath + name));
            ServletOutputStream outputStream = response.getOutputStream();

            outputStream.write(fileInputStream.readAllBytes());
            outputStream.flush();

            outputStream.close();
            fileInputStream.close();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        // OutputStream write back

    }
}
