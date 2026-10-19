package com.example.partyaffairsmgt.config;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * 宽松的时间反序列化配置：
 * 兼容 "yyyy-MM-dd HH:mm:ss"、"yyyy-MM-dd HH:mm"、ISO-8601（含 Z/时区偏移，按系统时区转换）、
 * "yyyy-MM-dd" 与毫秒时间戳，避免前后端时间格式不一致导致 500/400。
 */
@Configuration
public class LenientJacksonConfig {

    private static final DateTimeFormatter DT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DT_MIN = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter D = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    static class LenientLocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String s = p.getValueAsString();
            if (s == null || s.trim().isEmpty() || "null".equals(s)) return null;
            s = s.trim();
            try { return DT.parse(s, LocalDateTime::from); } catch (Exception ignored) { }
            try { return DT_MIN.parse(s, LocalDateTime::from); } catch (Exception ignored) { }
            try { return OffsetDateTime.parse(s).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime(); } catch (Exception ignored) { }
            try { return LocalDateTime.parse(s); } catch (Exception ignored) { }
            try { return LocalDate.parse(s, D).atStartOfDay(); } catch (Exception ignored) { }
            try { return Instant.ofEpochMilli(Long.parseLong(s)).atZone(ZoneId.systemDefault()).toLocalDateTime(); } catch (Exception ignored) { }
            throw new IllegalArgumentException("无法解析日期时间: " + s);
        }
    }

    static class LenientLocalDateDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String s = p.getValueAsString();
            if (s == null || s.trim().isEmpty() || "null".equals(s)) return null;
            s = s.trim();
            try { return LocalDate.parse(s, D); } catch (Exception ignored) { }
            try { return LocalDate.parse(s); } catch (Exception ignored) { }
            try { return OffsetDateTime.parse(s).atZoneSameInstant(ZoneId.systemDefault()).toLocalDate(); } catch (Exception ignored) { }
            try { return DT.parse(s, LocalDateTime::from).toLocalDate(); } catch (Exception ignored) { }
            try { return Instant.ofEpochMilli(Long.parseLong(s)).atZone(ZoneId.systemDefault()).toLocalDate(); } catch (Exception ignored) { }
            throw new IllegalArgumentException("无法解析日期: " + s);
        }
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer lenientTimeCustomizer() {
        return builder -> builder
                .deserializerByType(LocalDateTime.class, new LenientLocalDateTimeDeserializer())
                .deserializerByType(LocalDate.class, new LenientLocalDateDeserializer());
    }
}
