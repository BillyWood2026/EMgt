package com.example.iam.engine;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * Permission Expression Engine
 * Evaluates permission expressions using SpEL (Spring Expression Language)
 * 
 * Supported expression types:
 * - DATA_SCOPE: Data row filtering expressions
 * - CONDITIONAL: Conditional permission expressions
 * - TIME_RANGE: Time-based access control
 * - ORG_HIERARCHY: Organization hierarchy-based access
 */
@Slf4j
@Component
public class PermissionExpressionEngine {
    
    private final ExpressionParser expressionParser = new SpelExpressionParser();
    
    // Built-in functions available in expressions
    private static final Pattern SECURITY_PATTERN = Pattern.compile(
        "^(?!.*(Runtime\\.|System\\.|Process\\.|Class\\.|java\\.lang\\.)).*$"
    );
    
    /**
     * Context for permission evaluation
     */
    @Data
    public static class PermissionContext {
        private Long userId;
        private String employeeId;
        private String username;
        private Long orgId;
        private String orgCode;
        private String orgPath;
        private Integer orgLevel;
        private String[] roleCodes;
        private Long[] roleIds;
        private Integer dataPermissionLevel;
        private String costCenter;
        private String businessLine;
        private String regionCode;
        private String managerEmployeeId;
        private LocalDateTime currentTime;
        private String requestIp;
        private String moduleCode;
        private String action;
        private String resourceType;
        private String resourceId;
        
        // Additional context from request
        private Map<String, Object> attributes = new HashMap<>();
        
        public PermissionContext() {
            this.currentTime = LocalDateTime.now();
        }
    }
    
    /**
     * Evaluation result
     */
    @Data
    public static class EvaluationResult {
        private boolean allowed;
        private String expression;
        private String reason;
        private Map<String, Object> contextSnapshot;
        private long evaluationTimeMs;
        
        public static EvaluationResult allow(String expression) {
            EvaluationResult result = new EvaluationResult();
            result.setAllowed(true);
            result.setExpression(expression);
            result.setReason("Permission granted");
            return result;
        }
        
        public static EvaluationResult deny(String expression, String reason) {
            EvaluationResult result = new EvaluationResult();
            result.setAllowed(false);
            result.setExpression(expression);
            result.setReason(reason);
            return result;
        }
    }
    
    /**
     * Evaluate a permission expression
     * 
     * @param expression SpEL expression string
     * @param context Permission evaluation context
     * @return Evaluation result
     */
    public EvaluationResult evaluate(String expression, PermissionContext context) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Security check - prevent dangerous expressions
            if (!isSecureExpression(expression)) {
                log.warn("Blocked potentially dangerous expression: {}", expression);
                return EvaluationResult.deny(expression, "Expression contains forbidden patterns");
            }
            
            // Create SpEL evaluation context
            EvaluationContext evalContext = createEvaluationContext(context);
            
            // Parse and evaluate expression
            Expression exp = expressionParser.parseExpression(expression);
            Boolean result = exp.getValue(evalContext, Boolean.class);
            
            long evaluationTime = System.currentTimeMillis() - startTime;
            
            EvaluationResult evalResult = result != null && result 
                ? EvaluationResult.allow(expression) 
                : EvaluationResult.deny(expression, "Expression evaluated to false");
            
            evalResult.setEvaluationTimeMs(evaluationTime);
            evalResult.setContextSnapshot(buildContextSnapshot(context));
            
            log.debug("Expression '{}' evaluated to {} in {}ms", 
                expression, evalResult.isAllowed(), evaluationTime);
            
            return evalResult;
            
        } catch (Exception e) {
            log.error("Failed to evaluate expression '{}': {}", expression, e.getMessage());
            EvaluationResult result = EvaluationResult.deny(expression, "Evaluation error: " + e.getMessage());
            result.setEvaluationTimeMs(System.currentTimeMillis() - startTime);
            return result;
        }
    }
    
    /**
     * Evaluate a data scope expression and return filter conditions
     */
    public String evaluateDataScope(String expression, PermissionContext context) {
        try {
            if (expression == null || expression.isBlank()) {
                return null;
            }
            
            EvaluationContext evalContext = createEvaluationContext(context);
            Expression exp = expressionParser.parseExpression(expression);
            Object result = exp.getValue(evalContext);
            
            return result != null ? result.toString() : null;
            
        } catch (Exception e) {
            log.error("Failed to evaluate data scope expression '{}': {}", expression, e.getMessage());
            return null;
        }
    }
    
    /**
     * Create SpEL evaluation context from permission context
     */
    private EvaluationContext createEvaluationContext(PermissionContext context) {
        StandardEvaluationContext evalContext = new StandardEvaluationContext();
        
        // Set root object
        evalContext.setVariable("userId", context.getUserId());
        evalContext.setVariable("employeeId", context.getEmployeeId());
        evalContext.setVariable("username", context.getUsername());
        evalContext.setVariable("orgId", context.getOrgId());
        evalContext.setVariable("orgCode", context.getOrgCode());
        evalContext.setVariable("orgPath", context.getOrgPath());
        evalContext.setVariable("orgLevel", context.getOrgLevel());
        evalContext.setVariable("roleCodes", context.getRoleCodes());
        evalContext.setVariable("roleIds", context.getRoleIds());
        evalContext.setVariable("dataPermissionLevel", context.getDataPermissionLevel());
        evalContext.setVariable("costCenter", context.getCostCenter());
        evalContext.setVariable("businessLine", context.getBusinessLine());
        evalContext.setVariable("regionCode", context.getRegionCode());
        evalContext.setVariable("managerEmployeeId", context.getManagerEmployeeId());
        evalContext.setVariable("currentTime", context.getCurrentTime());
        evalContext.setVariable("requestIp", context.getRequestIp());
        evalContext.setVariable("moduleCode", context.getModuleCode());
        evalContext.setVariable("action", context.getAction());
        evalContext.setVariable("resourceType", context.getResourceType());
        evalContext.setVariable("resourceId", context.getResourceId());
        
        // Register custom functions
        try {
            evalContext.registerFunction("isInOrgPath", 
                org.apache.commons.lang3.StringUtils.class.getMethod("contains", CharSequence.class, CharSequence.class));
            evalContext.registerFunction("hasRole", 
                PermissionExpressionEngine.class.getDeclaredMethod("hasRole", String[].class, String.class));
            evalContext.registerFunction("isWithinTimeRange", 
                PermissionExpressionEngine.class.getDeclaredMethod("isWithinTimeRange", LocalDateTime.class, LocalDateTime.class));
            evalContext.registerFunction("isManagerOf", 
                PermissionExpressionEngine.class.getDeclaredMethod("isManagerOf", String.class, String.class));
            evalContext.registerFunction("matchesOrgLevel", 
                PermissionExpressionEngine.class.getDeclaredMethod("matchesOrgLevel", Integer.class, Integer.class, String.class));
        } catch (NoSuchMethodException e) {
            log.error("Failed to register custom functions: {}", e.getMessage());
        }
        
        // Set attributes as root
        evalContext.setRootObject(context.getAttributes());
        
        return evalContext;
    }
    
    /**
     * Security check for expressions
     */
    private boolean isSecureExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            return true;
        }
        return SECURITY_PATTERN.matcher(expression).matches();
    }
    
    /**
     * Check if user has a specific role
     */
    private boolean hasRole(String[] roleCodes, String roleCode) {
        if (roleCodes == null || roleCode == null) {
            return false;
        }
        for (String rc : roleCodes) {
            if (roleCode.equals(rc)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if current time is within range
     */
    private boolean isWithinTimeRange(LocalDateTime start, LocalDateTime end) {
        LocalDateTime now = LocalDateTime.now();
        boolean afterStart = start == null || !now.isBefore(start);
        boolean beforeEnd = end == null || !now.isAfter(end);
        return afterStart && beforeEnd;
    }
    
    /**
     * Check if user is manager of target employee
     */
    private boolean isManagerOf(String managerId, String employeeId) {
        if (managerId == null || employeeId == null) {
            return false;
        }
        return managerId.equals(employeeId);
    }
    
    /**
     * Check if org level matches
     */
    private boolean matchesOrgLevel(Integer userLevel, Integer requiredLevel, String operator) {
        if (userLevel == null || requiredLevel == null) {
            return false;
        }
        return switch (operator) {
            case "=" -> userLevel.equals(requiredLevel);
            case "<" -> userLevel < requiredLevel;
            case ">" -> userLevel > requiredLevel;
            case "<=" -> userLevel <= requiredLevel;
            case ">=" -> userLevel >= requiredLevel;
            default -> false;
        };
    }
    
    /**
     * Build context snapshot for audit
     */
    private Map<String, Object> buildContextSnapshot(PermissionContext context) {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("userId", context.getUserId());
        snapshot.put("employeeId", context.getEmployeeId());
        snapshot.put("orgId", context.getOrgId());
        snapshot.put("orgLevel", context.getOrgLevel());
        snapshot.put("dataPermissionLevel", context.getDataPermissionLevel());
        snapshot.put("moduleCode", context.getModuleCode());
        snapshot.put("action", context.getAction());
        snapshot.put("currentTime", context.getCurrentTime());
        return snapshot;
    }
    
    /**
     * Parse and validate an expression
     * 
     * @param expression Expression to validate
     * @return true if valid, false otherwise
     */
    public boolean validateExpression(String expression) {
        if (expression == null || expression.isBlank()) {
            return true;
        }
        if (!isSecureExpression(expression)) {
            return false;
        }
        try {
            expressionParser.parseExpression(expression);
            return true;
        } catch (Exception e) {
            log.warn("Invalid expression '{}': {}", expression, e.getMessage());
            return false;
        }
    }
}
