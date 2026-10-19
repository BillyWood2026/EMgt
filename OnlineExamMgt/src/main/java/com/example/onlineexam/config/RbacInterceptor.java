package com.example.onlineexam.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * 轻量 RBAC 拦截器：从请求头 X-Role 读取角色（缺失时回退 X-Operator，
 * e2e/admin 视为超管 C99 放行，其它视为普通角色 C01）。
 * GET/HEAD/OPTIONS 全部放行；POST/PUT/DELETE/PATCH 需角色命中 writeRoles 集合，
 * 否则返回 HTTP 403 + 信封 {code:403,message,data,timestamp,msg}。
 * 超管角色 C99/ADMIN 一律放行（已自动并入 writeRoles）。
 */
public class RbacInterceptor implements HandlerInterceptor {

    public static final String ROLE_HEADER = "X-Role";
    public static final String OPERATOR_HEADER = "X-Operator";
    public static final String SUPER_ADMIN = "C99";
    public static final String ADMIN = "ADMIN";
    public static final String DEFAULT_ROLE = "C01";

    private final String moduleName;
    private final Set<String> writeRoles;

    public RbacInterceptor(String moduleName, String... writeRoles) {
        this.moduleName = moduleName;
        Set<String> s = new HashSet<>(Arrays.asList(writeRoles));
        s.add(SUPER_ADMIN);
        s.add(ADMIN);
        this.writeRoles = Collections.unmodifiableSet(s);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String method = request.getMethod();
        if (method == null) {
            return true;
        }
        String m = method.toUpperCase();
        if ("GET".equals(m) || "HEAD".equals(m) || "OPTIONS".equals(m)) {
            return true;
        }
        String role = resolveRole(request);
        if (writeRoles.contains(role)) {
            return true;
        }
        writeForbidden(response, role);
        return false;
    }

    private String resolveRole(HttpServletRequest request) {
        String role = request.getHeader(ROLE_HEADER);
        if (role != null && !role.isBlank()) {
            return role.trim().toUpperCase();
        }
        String op = request.getHeader(OPERATOR_HEADER);
        if (op != null && !op.isBlank()) {
            String o = op.trim().toLowerCase();
            if ("e2e".equals(o) || "admin".equals(o)) {
                return SUPER_ADMIN;
            }
            return DEFAULT_ROLE;
        }
        return DEFAULT_ROLE;
    }

    private void writeForbidden(HttpServletResponse response, String currentRole) throws IOException {
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        String msg = "无操作权限，当前角色：[" + currentRole + "]，需要：" + writeRoles;
        String body = "{\"code\":403,\"message\":\"" + escape(msg) + "\",\"data\":null,\"timestamp\":"
                + System.currentTimeMillis() + ",\"msg\":\"" + escape(msg) + "\"}";
        PrintWriter out = response.getWriter();
        out.write(body);
        out.flush();
    }

    private static String escape(String s) {
        StringBuilder sb = new StringBuilder(s.length() + 16);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '"': sb.append("\\\""); break;
                case '\\': sb.append("\\\\"); break;
                case '\n': sb.append("\\n"); break;
                case '\r': sb.append("\\r"); break;
                case '\t': sb.append("\\t"); break;
                default:
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
            }
        }
        return sb.toString();
    }
}