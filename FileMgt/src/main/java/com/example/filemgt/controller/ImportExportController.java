package com.example.filemgt.controller;

import com.example.filemgt.common.BizException;
import com.example.filemgt.common.ResponseResult;
import com.example.filemgt.entity.FileLog;
import com.example.filemgt.mapper.FileLogMapper;
import com.lowagie.text.Document;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.poi.xwpf.usermodel.ParagraphAlignment;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * 统一导入导出引擎（中台通用能力，不耦合任何业务表）。
 * 契约：body {fileName, sheetName, title, columns:[{key,title}], rows:[{...}]}
 */
@RestController
@RequestMapping("/api/file/io")
public class ImportExportController {

    private static final long MAX_IMPORT_SIZE = 10L * 1024 * 1024;
    private static final int MAX_IMPORT_ROWS = 5000;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final FileLogMapper fileLogMapper;

    public ImportExportController(FileLogMapper fileLogMapper) {
        this.fileLogMapper = fileLogMapper;
    }

    // ==================== 导出 Excel ====================
    @PostMapping("/export/excel")
    public ResponseEntity<byte[]> exportExcel(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        ExportPayload payload = ExportPayload.parse(body);
        byte[] bytes;
        try (Workbook wb = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = wb.createSheet(payload.sheetName == null || payload.sheetName.isEmpty() ? "Sheet1" : payload.sheetName);
            CellStyle headerStyle = wb.createCellStyle();
            org.apache.poi.ss.usermodel.Font headerFont = wb.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            headerStyle.setAlignment(HorizontalAlignment.CENTER);

            Row header = sheet.createRow(0);
            for (int i = 0; i < payload.columns.size(); i++) {
                Cell cell = header.createCell(i);
                cell.setCellValue(payload.columns.get(i).title);
                cell.setCellStyle(headerStyle);
            }
            for (int r = 0; r < payload.rows.size(); r++) {
                Row row = sheet.createRow(r + 1);
                Map<String, Object> data = payload.rows.get(r);
                for (int c = 0; c < payload.columns.size(); c++) {
                    Cell cell = row.createCell(c);
                    Object v = data.get(payload.columns.get(c).key);
                    if (v instanceof Number) {
                        cell.setCellValue(((Number) v).doubleValue());
                    } else {
                        cell.setCellValue(v == null ? "" : String.valueOf(v));
                    }
                }
            }
            for (int i = 0; i < payload.columns.size(); i++) {
                sheet.autoSizeColumn(i);
                int w = sheet.getColumnWidth(i);
                sheet.setColumnWidth(i, Math.min(w + 512 * 2, 255 * 256));
            }
            wb.write(out);
            bytes = out.toByteArray();
        }
        saveLog(null, "export_excel", operator(request), request, payload.fileName);
        return download(bytes, payload.fileName, ".xlsx",
                "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
    }

    // ==================== 导出 Word ====================
    @PostMapping("/export/word")
    public ResponseEntity<byte[]> exportWord(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        ExportPayload payload = ExportPayload.parse(body);
        byte[] bytes;
        try (XWPFDocument doc = new XWPFDocument(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            XWPFParagraph titleP = doc.createParagraph();
            titleP.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = titleP.createRun();
            titleRun.setBold(true);
            titleRun.setFontSize(16);
            titleRun.setText(payload.title == null || payload.title.isEmpty() ? payload.fileName : payload.title);

            XWPFTable table = doc.createTable(1, payload.columns.size());
            XWPFTableRow headRow = table.getRow(0);
            for (int i = 0; i < payload.columns.size(); i++) {
                XWPFTableCell cell = i == 0 ? headRow.getCell(0) : headRow.getCell(i);
                setCellText(cell, payload.columns.get(i).title, true);
            }
            for (Map<String, Object> data : payload.rows) {
                XWPFTableRow row = table.createRow();
                for (int i = 0; i < payload.columns.size(); i++) {
                    Object v = data.get(payload.columns.get(i).key);
                    setCellText(row.getCell(i), v == null ? "" : String.valueOf(v), false);
                }
            }
            doc.write(out);
            bytes = out.toByteArray();
        }
        saveLog(null, "export_word", operator(request), request, payload.fileName);
        return download(bytes, payload.fileName, ".docx",
                "application/vnd.openxmlformats-officedocument.wordprocessingml.document");
    }

    private void setCellText(XWPFTableCell cell, String text, boolean bold) {
        if (cell == null) {
            return;
        }
        XWPFParagraph p = cell.getParagraphs().isEmpty() ? cell.addParagraph() : cell.getParagraphs().get(0);
        XWPFRun run = p.createRun();
        run.setBold(bold);
        run.setFontSize(10);
        run.setText(text);
    }

    // ==================== 导出 PDF ====================
    @PostMapping("/export/pdf")
    public ResponseEntity<byte[]> exportPdf(@RequestBody Map<String, Object> body, HttpServletRequest request) throws Exception {
        ExportPayload payload = ExportPayload.parse(body);
        byte[] bytes;
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document(PageSize.A4.rotate());
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont;
            Font bodyFont;
            String note = null;
            BaseFont bf = loadChineseFont();
            if (bf != null) {
                titleFont = new Font(bf, 16, Font.BOLD);
                bodyFont = new Font(bf, 10, Font.NORMAL);
            } else {
                titleFont = new Font(Font.HELVETICA, 16, Font.BOLD);
                bodyFont = new Font(Font.HELVETICA, 10, Font.NORMAL);
                note = "[Note] Chinese font not found on server, fallback to Helvetica; CJK characters may not render.";
            }

            Paragraph title = new Paragraph(payload.title == null || payload.title.isEmpty() ? payload.fileName : payload.title, titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(12f);
            document.add(title);
            if (note != null) {
                document.add(new Paragraph(note, bodyFont));
            }

            PdfPTable table = new PdfPTable(payload.columns.size());
            table.setWidthPercentage(100);
            table.setSpacingBefore(6f);
            for (ExportPayload.Column col : payload.columns) {
                PdfPCell cell = new PdfPCell(new Phrase(col.title, bodyFont));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setBackgroundColor(new java.awt.Color(230, 230, 230));
                cell.setPadding(5f);
                table.addCell(cell);
            }
            for (Map<String, Object> data : payload.rows) {
                for (ExportPayload.Column col : payload.columns) {
                    Object v = data.get(col.key);
                    PdfPCell cell = new PdfPCell(new Phrase(v == null ? "" : String.valueOf(v), bodyFont));
                    cell.setPadding(4f);
                    table.addCell(cell);
                }
            }
            document.add(table);
            document.close();
            bytes = out.toByteArray();
        }
        saveLog(null, "export_pdf", operator(request), request, payload.fileName);
        return download(bytes, payload.fileName, ".pdf", "application/pdf");
    }

    private BaseFont loadChineseFont() {
        String[] candidates = {"C:/Windows/Fonts/simsun.ttc", "C:/Windows/Fonts/msyh.ttc"};
        for (String path : candidates) {
            try {
                if (new File(path).exists()) {
                    return BaseFont.createFont(path + ",0", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }

    // ==================== 导入 Excel/CSV ====================
    @PostMapping("/import/excel")
    public ResponseResult<Map<String, Object>> importExcel(@RequestParam("file") MultipartFile file,
                                                           HttpServletRequest request) throws Exception {
        if (file == null || file.isEmpty()) {
            throw new BizException("导入文件为空");
        }
        if (file.getSize() > MAX_IMPORT_SIZE) {
            throw new BizException("文件超过10MB限制");
        }
        String original = file.getOriginalFilename() == null ? "" : file.getOriginalFilename();
        String lower = original.toLowerCase();
        List<List<String>> grid;
        if (lower.endsWith(".csv")) {
            grid = parseCsv(file.getBytes());
        } else if (lower.endsWith(".xlsx") || lower.endsWith(".xls")) {
            grid = parseExcel(file.getInputStream());
        } else {
            throw new BizException("仅支持 .xlsx/.xls/.csv 文件");
        }
        if (grid.isEmpty()) {
            throw new BizException("文件内容为空");
        }
        if (grid.size() - 1 > MAX_IMPORT_ROWS) {
            throw new BizException("数据行超过5000行限制");
        }

        List<String> headers = grid.get(0);
        List<Map<String, String>> columns = new ArrayList<>();
        for (int i = 0; i < headers.size(); i++) {
            Map<String, String> col = new HashMap<>();
            col.put("key", "col" + i);
            col.put("title", headers.get(i));
            columns.add(col);
        }
        List<Map<String, String>> rows = new ArrayList<>();
        for (int r = 1; r < grid.size(); r++) {
            List<String> line = grid.get(r);
            Map<String, String> row = new LinkedHashMap<>();
            for (int c = 0; c < headers.size(); c++) {
                row.put("col" + c, c < line.size() ? line.get(c) : "");
            }
            rows.add(row);
        }
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("columns", columns);
        data.put("rows", rows);
        data.put("total", rows.size());

        saveLog(null, "import_excel", operator(request), request, original);
        return ResponseResult.ok(data);
    }

    private List<List<String>> parseExcel(InputStream in) throws Exception {
        List<List<String>> grid = new ArrayList<>();
        try (Workbook wb = WorkbookFactory.create(in)) {
            Sheet sheet = wb.getSheetAt(0);
            if (sheet == null) {
                return grid;
            }
            int lastRow = sheet.getLastRowNum();
            for (int r = 0; r <= lastRow && r <= MAX_IMPORT_ROWS; r++) {
                Row row = sheet.getRow(r);
                List<String> line = new ArrayList<>();
                if (row != null) {
                    short lastCell = row.getLastCellNum();
                    for (int c = 0; c < lastCell; c++) {
                        line.add(cellToString(row.getCell(c)));
                    }
                }
                grid.add(line);
            }
        }
        return grid;
    }

    private String cellToString(Cell cell) {
        if (cell == null) {
            return "";
        }
        CellType type = cell.getCellType() == CellType.FORMULA ? cell.getCachedFormulaResultType() : cell.getCellType();
        switch (type) {
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getLocalDateTimeCellValue().format(DATE_FMT);
                }
                return BigDecimal.valueOf(cell.getNumericCellValue()).stripTrailingZeros().toPlainString();
            case BOOLEAN:
                return String.valueOf(cell.getBooleanCellValue());
            case STRING:
                return cell.getStringCellValue() == null ? "" : cell.getStringCellValue().trim();
            default:
                return "";
        }
    }

    private List<List<String>> parseCsv(byte[] bytes) {
        // 依次尝试 UTF-8（含BOM）、GBK
        String text = tryDecode(bytes, StandardCharsets.UTF_8);
        if (text == null || text.contains("")) {
            text = tryDecode(bytes, Charset.forName("GBK"));
        }
        if (text == null) {
            text = new String(bytes, StandardCharsets.UTF_8);
        }
        if (text.startsWith("﻿")) {
            text = text.substring(1);
        }
        List<List<String>> grid = new ArrayList<>();
        String[] lines = text.split("\\r\\n|\\n|\\r");
        for (String line : lines) {
            if (grid.size() > MAX_IMPORT_ROWS) {
                break;
            }
            if (line.isEmpty()) {
                continue;
            }
            grid.add(parseCsvLine(line));
        }
        return grid;
    }

    private List<String> parseCsvLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder cur = new StringBuilder();
        boolean inQuotes = false;
        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (inQuotes) {
                if (ch == '"') {
                    if (i + 1 < line.length() && line.charAt(i + 1) == '"') {
                        cur.append('"');
                        i++;
                    } else {
                        inQuotes = false;
                    }
                } else {
                    cur.append(ch);
                }
            } else if (ch == '"') {
                inQuotes = true;
            } else if (ch == ',') {
                fields.add(cur.toString().trim());
                cur.setLength(0);
            } else {
                cur.append(ch);
            }
        }
        fields.add(cur.toString().trim());
        return fields;
    }

    private String tryDecode(byte[] bytes, Charset charset) {
        try {
            return charset.newDecoder().decode(java.nio.ByteBuffer.wrap(bytes)).toString();
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== 公共 ====================
    private ResponseEntity<byte[]> download(byte[] bytes, String fileName, String ext, String contentType) throws Exception {
        String base = fileName == null || fileName.isEmpty() ? "export" : fileName;
        String encoded = URLEncoder.encode(base + ext, StandardCharsets.UTF_8).replace("+", "%20");
        return ResponseEntity.ok()
                .contentLength(bytes.length)
                .contentType(MediaType.parseMediaType(contentType))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename*=UTF-8''" + encoded)
                .body(bytes);
    }

    private String operator(HttpServletRequest request) {
        String op = request.getHeader("X-Operator");
        return op == null || op.isEmpty() ? "anonymous" : op;
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

    /** 统一导出契约体 */
    private static class ExportPayload {
        String fileName;
        String sheetName;
        String title;
        List<Column> columns;
        List<Map<String, Object>> rows;

        static class Column {
            String key;
            String title;
        }

        @SuppressWarnings("unchecked")
        static ExportPayload parse(Map<String, Object> body) {
            if (body == null) {
                throw new BizException("请求体为空");
            }
            ExportPayload p = new ExportPayload();
            p.fileName = str(body.get("fileName"));
            p.sheetName = str(body.get("sheetName"));
            p.title = str(body.get("title"));
            Object cols = body.get("columns");
            if (!(cols instanceof List) || ((List<?>) cols).isEmpty()) {
                throw new BizException("columns 不能为空");
            }
            p.columns = new ArrayList<>();
            for (Object o : (List<?>) cols) {
                if (!(o instanceof Map)) {
                    throw new BizException("columns 元素格式错误");
                }
                Map<String, Object> m = (Map<String, Object>) o;
                Column c = new Column();
                c.key = str(m.get("key"));
                c.title = str(m.get("title"));
                if (c.key == null || c.key.isEmpty()) {
                    throw new BizException("columns.key 不能为空");
                }
                if (c.title == null) {
                    c.title = c.key;
                }
                p.columns.add(c);
            }
            Object rs = body.get("rows");
            p.rows = new ArrayList<>();
            if (rs instanceof List) {
                for (Object o : (List<?>) rs) {
                    if (o instanceof Map) {
                        p.rows.add(new LinkedHashMap<>((Map<String, Object>) o));
                    }
                }
            }
            return p;
        }

        private static String str(Object o) {
            return o == null ? null : String.valueOf(o);
        }
    }
}
