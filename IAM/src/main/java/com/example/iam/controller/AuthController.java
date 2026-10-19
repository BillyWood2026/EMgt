package com.example.iam.controller;

import com.example.iam.entity.user.IamUser;
import com.example.iam.mapper.IamUserMapper;
import com.example.iam.mapper.IamUserRoleMapper;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IamUserMapper userMapper;
    private final IamUserRoleMapper userRoleMapper;
    private final BCryptPasswordEncoder passwordEncoder;

    @Value("${jwt.secret:eOpt-IAM-JWT-Secret-Key-2024}")
    private String jwtSecret;

    @Value("${jwt.expiration:86400000}")
    private Long jwtExpiration;

    private SecretKey getSigningKey() {
        byte[] keyBytes = jwtSecret.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");

        log.debug("Login attempt for username: {}", username);

        try {
            IamUser user = userMapper.selectByUsername(username);

            if (user == null) {
                log.warn("User not found: {}", username);
                return buildErrorResponse("用户不存在");
            }

            log.debug("User found: {}, password hash: {}", user.getId(), user.getPasswordHash());

            if (!passwordEncoder.matches(password, user.getPasswordHash())) {
                log.warn("Invalid password for user: {}", username);
                return buildErrorResponse("密码错误");
            }

            String role = getUserRole(user.getId());

            String token = generateToken(user.getId(), username, role);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", Map.of(
                    "token", token,
                    "username", username,
                    "role", role,
                    "userId", user.getId()
            ));

            log.info("User logged in successfully: {}", username);
            return response;
        } catch (Exception e) {
            log.error("Login failed for user: {}", username, e);
            return buildErrorResponse("登录失败: " + e.getMessage());
        }
    }

    @PostMapping("/reset-password")
    public Map<String, Object> resetPassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String newPassword = request.get("newPassword");

        log.debug("Password reset attempt for username: {}", username);

        try {
            IamUser user = userMapper.selectByUsername(username);
            if (user == null) {
                log.warn("User not found for password reset: {}", username);
                return buildErrorResponse("用户不存在");
            }

            String encodedPassword = passwordEncoder.encode(newPassword);
            userMapper.updatePasswordByUsername(username, encodedPassword);

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "密码重置成功");
            return response;
        } catch (Exception e) {
            log.error("Password reset failed for user: {}", username, e);
            return buildErrorResponse("密码重置失败: " + e.getMessage());
        }
    }

    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String password = request.get("password");
        String email = request.get("email");
        String phone = request.get("phone");

        log.debug("Register attempt for username: {}", username);

        IamUser existingUser = userMapper.selectByUsername(username);
        if (existingUser != null) {
            log.warn("Username already exists: {}", username);
            return buildErrorResponse("用户名已存在");
        }

        IamUser user = new IamUser();
        user.setEmployeeId("EMP" + System.currentTimeMillis());
        user.setUsername(username);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setEmail(email);
        user.setMobile(phone);
        user.setStatus("ACTIVE");
        user.setCreatedBy("SYSTEM");

        userMapper.insert(user);

        Map<String, Object> response = new HashMap<>();
        response.put("code", 200);
        response.put("message", "注册成功");
        response.put("data", Map.of(
                "username", username,
                "userId", user.getId()
        ));

        log.info("User registered successfully: {}", username);
        return response;
    }

    @GetMapping("/me")
    public Map<String, Object> getCurrentUser(@RequestHeader("Authorization") String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            return buildErrorResponse(401, "Invalid token");
        }

        String token = authorization.substring(7);
        try {
            Long userId = Long.parseLong(Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload()
                    .getSubject());

            IamUser user = userMapper.selectById(userId);
            if (user == null) {
                return buildErrorResponse(404, "User not found");
            }

            String role = getUserRole(userId);

            // Apply data masking for sensitive fields
            String maskedEmail = maskEmail(user.getEmail());
            String maskedPhone = maskPhone(user.getMobile());

            Map<String, Object> response = new HashMap<>();
            response.put("code", 200);
            response.put("message", "success");
            response.put("data", Map.of(
                    "userId", user.getId(),
                    "username", user.getUsername(),
                    "role", role,
                    "email", maskedEmail,
                    "phone", maskedPhone
            ));
            return response;
        } catch (Exception e) {
            log.error("Token validation failed", e);
            return buildErrorResponse(401, "Invalid token");
        }
    }

    private String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return email;
        }
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domain = parts[1];
        if (localPart.length() <= 2) {
            return localPart.charAt(0) + "***@" + domain;
        }
        return localPart.substring(0, 2) + "***" + localPart.substring(localPart.length() - 1) + "@" + domain;
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 7) {
            return phone;
        }
        return phone.substring(0, 3) + "****" + phone.substring(phone.length() - 4);
    }

    private String getUserRole(Long userId) {
        return "ADMIN";
    }

    private String generateToken(Long userId, String username, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("username", username)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    private Map<String, Object> buildErrorResponse(String message) {
        return buildErrorResponse(500, message);
    }

    private Map<String, Object> buildErrorResponse(int code, String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", code);
        response.put("message", message);
        return response;
    }
}