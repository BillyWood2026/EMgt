package com.example.partyaffairsmgt.membership.controller;

import com.example.partyaffairsmgt.common.ResponseResult;
import com.example.partyaffairsmgt.common.party.PartyRole;
import com.example.partyaffairsmgt.membership.entity.DuesAccount;
import com.example.partyaffairsmgt.membership.entity.DuesBackpay;
import com.example.partyaffairsmgt.membership.entity.DuesBase;
import com.example.partyaffairsmgt.membership.entity.DuesPayment;
import com.example.partyaffairsmgt.membership.entity.DuesReduce;
import com.example.partyaffairsmgt.membership.entity.DuesRemittance;
import com.example.partyaffairsmgt.membership.service.DuesService;
import java.io.IOException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 党费收缴管理（需求 5.3）。 */
@RestController
@RequestMapping("/api/party/membership/dues")
@CrossOrigin(origins = "*")
public class DuesPaymentController {

    @Autowired
    private DuesService duesService;

    @GetMapping("/categories")
    public ResponseResult<List<String>> categories() {
        return ResponseResult.success(duesService.categories());
    }

    @GetMapping("/calc-preview")
    public ResponseResult<Map<String, Object>> calcPreview(
            @RequestParam String category,
            @RequestParam(required = false) BigDecimal base,
            @RequestParam(required = false) BigDecimal fixed) {
        return ResponseResult.success(duesService.calcPreview(category, base, fixed));
    }

    // ---------- 基数 ----------

    @GetMapping("/base/list")
    public ResponseResult<List<DuesBase>> baseList(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String keyword) {
        return ResponseResult.success(duesService.baseList(orgId, keyword));
    }

    @PostMapping("/base")
    public ResponseResult<DuesBase> saveBase(@RequestBody DuesBase body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R08, PartyRole.R12);
        return ResponseResult.success(duesService.saveBase(body));
    }

    @PostMapping("/base/{id}/audit")
    public ResponseResult<DuesBase> auditBase(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R03, PartyRole.R04);
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        String opinion = body.get("opinion") == null ? null : body.get("opinion").toString();
        return ResponseResult.success(duesService.auditBase(id, pass, opinion));
    }

    // ---------- 账单 ----------

    @PostMapping("/bills/generate")
    public ResponseResult<Map<String, Object>> generate(@RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R08);
        Long orgId = body.get("orgId") == null ? null : Long.valueOf(body.get("orgId").toString());
        int year = Integer.parseInt(body.get("year").toString());
        int month = Integer.parseInt(body.get("month").toString());
        return ResponseResult.success(duesService.generateBills(orgId, year, month));
    }

    @GetMapping("/bills")
    public ResponseResult<List<DuesPayment>> bills(
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String status) {
        return ResponseResult.success(duesService.bills(year, month, orgId, status));
    }

    @GetMapping("/export")
    public void export(@RequestParam Integer year, @RequestParam Integer month,
                       @RequestParam(required = false) Long orgId, HttpServletResponse resp)
            throws IOException {
        List<DuesPayment> rows = duesService.bills(year, month, orgId, null);
        resp.setContentType("text/csv; charset=UTF-8");
        String fn = URLEncoder.encode("党费账单_" + year + month + ".csv", StandardCharsets.UTF_8);
        resp.setHeader("Content-Disposition", "attachment; filename=" + fn);
        resp.getWriter().write('\uFEFF'); // CSV UTF-8 BOM，确保 Excel 打开不乱码

        resp.getWriter().println("账单号,组织,党员,人员类别,基数,比例,应缴,实缴,状态,缴纳方式,凭证号,登记人,确认人");
        for (DuesPayment p : rows) {
            resp.getWriter().println(String.join(",",
                    nz(p.getBillNo()), nz(p.getOrgName()), nz(p.getMemberName()), nz(p.getMemberCategory()),
                    str(p.getBaseAmount()), str(p.getRatio()), str(p.getDueAmount()), str(p.getPaidAmount()),
                    nz(p.getStatus()), nz(p.getPayMethod()), nz(p.getVoucherNo()),
                    nz(p.getCollectorName()), nz(p.getConfirmer())));
        }
    }

    // ---------- 收缴 / 确认 ----------

    @PostMapping("/payments/register")
    public ResponseResult<DuesPayment> register(@RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R08, PartyRole.R12);
        return ResponseResult.success(duesService.registerPayment(body));
    }

    @PostMapping("/payments/confirm")
    public ResponseResult<Integer> confirm(@RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R12);
        @SuppressWarnings("unchecked")
        List<Object> raw = (List<Object>) body.getOrDefault("ids", List.of());
        List<Long> ids = raw.stream().map(x -> Long.valueOf(x.toString())).toList();
        return ResponseResult.success(duesService.confirmPayments(ids));
    }

    // ---------- 减免 ----------

    @GetMapping("/reduce/list")
    public ResponseResult<List<DuesReduce>> reduceList(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) String status) {
        return ResponseResult.success(duesService.reduceList(orgId, status));
    }

    @PostMapping("/reduce")
    public ResponseResult<DuesReduce> createReduce(@RequestBody DuesReduce body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07, PartyRole.R08);
        return ResponseResult.success(duesService.createReduce(body));
    }

    @PostMapping("/reduce/{id}/audit")
    public ResponseResult<DuesReduce> auditReduce(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R03);
        boolean pass = Boolean.TRUE.equals(body.get("pass"));
        String opinion = body.get("opinion") == null ? null : body.get("opinion").toString();
        return ResponseResult.success(duesService.auditReduce(id, pass, opinion));
    }

    // ---------- 补缴 ----------

    @GetMapping("/backpay/list")
    public ResponseResult<List<DuesBackpay>> backpayList(@RequestParam(required = false) Long orgId) {
        return ResponseResult.success(duesService.backpayList(orgId));
    }

    @PostMapping("/backpay")
    public ResponseResult<DuesBackpay> createBackpay(@RequestBody DuesBackpay body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R08, PartyRole.R12);
        return ResponseResult.success(duesService.createBackpay(body));
    }

    // ---------- 催缴 / 脱党 ----------

    @GetMapping("/remind")
    public ResponseResult<Map<String, Object>> remind(
            @RequestParam(required = false) Long orgId,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) Integer month) {
        return ResponseResult.success(duesService.remind(orgId, year, month));
    }

    @PostMapping("/deserter-workflow")
    public ResponseResult<Integer> deserterWorkflow(@RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R07);
        @SuppressWarnings("unchecked")
        List<Object> raw = (List<Object>) body.getOrDefault("memberIds", List.of());
        List<Long> ids = raw.stream().map(x -> Long.valueOf(x.toString())).toList();
        return ResponseResult.success(duesService.deserterWorkflow(ids));
    }

    // ---------- 上缴 ----------

    @GetMapping("/remittance/list")
    public ResponseResult<List<DuesRemittance>> remittanceList(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer year) {
        return ResponseResult.success(duesService.remittanceList(type, year));
    }

    @PostMapping("/remittance/large")
    public ResponseResult<DuesRemittance> large(@RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R12);
        Long paymentId = Long.valueOf(body.get("paymentId").toString());
        return ResponseResult.success(duesService.createLargeRemittance(paymentId));
    }

    @PostMapping("/remittance/yearly")
    public ResponseResult<DuesRemittance> yearly(@RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R12);
        Long orgId = body.get("orgId") == null ? null : Long.valueOf(body.get("orgId").toString());
        int year = Integer.parseInt(body.get("year").toString());
        return ResponseResult.success(duesService.createYearlyRemittance(orgId, year));
    }

    @PutMapping("/remittance/{id}/receipt")
    public ResponseResult<DuesRemittance> receipt(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R12);
        String receiptNo = body.get("receiptNo").toString();
        LocalDate receiptDate = body.get("receiptDate") == null ? null : LocalDate.parse(body.get("receiptDate").toString());
        LocalDate remitDate = body.get("remitDate") == null ? null : LocalDate.parse(body.get("remitDate").toString());
        return ResponseResult.success(duesService.receipt(id, receiptNo, receiptDate, remitDate));
    }

    // ---------- 账户 ----------

    @GetMapping("/account/list")
    public ResponseResult<List<DuesAccount>> accountList() {
        return ResponseResult.success(duesService.accountList());
    }

    @PostMapping("/account")
    public ResponseResult<DuesAccount> saveAccount(@RequestBody DuesAccount body) {
        PartyRole.requireAny(PartyRole.R02, PartyRole.R12);
        return ResponseResult.success(duesService.saveAccount(body));
    }

    // ---------- 统计 ----------

    @GetMapping("/stats")
    public ResponseResult<Map<String, Object>> stats(
            @RequestParam Integer year,
            @RequestParam(required = false) Long orgId) {
        return ResponseResult.success(duesService.stats(year, orgId));
    }

    private static String nz(String s) {
        return s == null ? "" : s.replace(",", " ");
    }

    private static String str(BigDecimal v) {
        return v == null ? "" : v.toPlainString();
    }
}
