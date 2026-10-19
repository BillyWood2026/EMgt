package com.example.filemgt.controller;

import com.example.filemgt.common.BizException;
import com.example.filemgt.common.ResponseResult;
import com.example.filemgt.entity.FileLog;
import com.example.filemgt.entity.FileRecord;
import com.example.filemgt.mapper.FileLogMapper;
import com.example.filemgt.service.FileRecordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@RestController
@RequestMapping("/api/file")
public class FileUploadController {

    @Value("${file.store-root:d:\\EMgtv3\\file-store}")
    private String storeRoot;

    private final FileRecordService fileRecordService;
    private final FileLogMapper fileLogMapper;

    public FileUploadController(FileRecordService fileRecordService, FileLogMapper fileLogMapper) {
        this.fileRecordService = fileRecordService;
        this.fileLogMapper = fileLogMapper;
    }

    // 统一上传: 存储到 {storeRoot}\{yyyyMM}\，落库文件记录并写操作日志
    @PostMapping("/upload")
    public ResponseResult<FileRecord> upload(@RequestParam("file") MultipartFile file,
                                             @RequestParam(required = false) String uploader,
                                             @RequestParam(required = false) String serviceCode,
                                             HttpServletRequest request) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new BizException("上传文件为空");
        }
        String original = file.getOriginalFilename() == null ? "unknown" : file.getOriginalFilename();
        String ext = "";
        int dot = original.lastIndexOf('.');
        if (dot >= 0 && dot < original.length() - 1) {
            ext = original.substring(dot + 1);
        }
        String month = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        File dir = new File(storeRoot + File.separator + month);
        if (!dir.exists() && !dir.mkdirs()) {
            throw new BizException("创建存储目录失败");
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + (ext.isEmpty() ? "" : "." + ext);
        File dest = new File(dir, storedName);
        file.transferTo(dest);

        MessageDigest md5 = MessageDigest.getInstance("MD5");
        try (FileInputStream in = new FileInputStream(dest)) {
            byte[] buf = new byte[8192];
            int n;
            while ((n = in.read(buf)) > 0) {
                md5.update(buf, 0, n);
            }
        }
        StringBuilder hex = new StringBuilder();
        for (byte b : md5.digest()) {
            hex.append(String.format("%02x", b));
        }

        FileRecord record = new FileRecord();
        record.setFileName(original);
        record.setFileExt(ext);
        record.setFileSize(dest.length());
        record.setMimeType(file.getContentType());
        record.setStoragePath(dest.getAbsolutePath());
        record.setMd5(hex.toString());
        record.setUploader(uploader == null || uploader.isEmpty() ? "anonymous" : uploader);
        record.setServiceCode(serviceCode);
        record.setStatus("正常");
        record.setCreateTime(LocalDateTime.now());
        record.setUpdateTime(LocalDateTime.now());
        fileRecordService.save(record);

        saveLog(record.getId(), "upload", record.getUploader(), request, original);
        return ResponseResult.ok(record);
    }

    // 统一下载: 流式返回并写操作日志
    @GetMapping("/download/{id}")
    public ResponseEntity<InputStreamResource> download(@PathVariable Long id, HttpServletRequest request) throws Exception {
        FileRecord record = fileRecordService.getById(id);
        if (record == null) {
            throw new BizException("文件不存在");
        }
        File f = new File(record.getStoragePath());
        if (!f.exists()) {
            throw new BizException("文件已被移除");
        }
        saveLog(id, "download", record.getUploader(), request, record.getFileName());
        String encoded = URLEncoder.encode(record.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");
        MediaType mediaType = record.getMimeType() != null
                ? MediaType.parseMediaType(record.getMimeType()) : MediaType.APPLICATION_OCTET_STREAM;
        return ResponseEntity.ok()
                .contentLength(f.length())
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encoded)
                .body(new InputStreamResource(new FileInputStream(f)));
    }

    private void saveLog(Long fileId, String opType, String operator, HttpServletRequest request, String detail) {
        FileLog log = new FileLog();
        log.setFileId(fileId);
        log.setOpType(opType);
        log.setOperator(operator);
        log.setIp(request.getRemoteAddr());
        log.setOpTime(LocalDateTime.now());
        log.setDetail(detail);
        log.setCreateTime(LocalDateTime.now());
        fileLogMapper.insert(log);
    }
}
