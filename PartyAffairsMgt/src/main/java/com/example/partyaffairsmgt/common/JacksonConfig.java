package com.example.partyaffairsmgt.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 全局时间格式：统一 yyyy-MM-dd HH:mm:ss / yyyy-MM-dd，
 * 反序列化兼容 ISO-8601（带 T）入参，覆盖全部实体入参。
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter D_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static class LocalDateTimeSerializer extends JsonSerializer<LocalDateTime> {
        @Override
        public void serialize(LocalDateTime v, JsonGenerator g, SerializerProvider sp) throws IOException {
            g.writeString(v == null ? null : v.format(DT_FMT));
        }
        @Override
        public Class<LocalDateTime> handledType() {
            return LocalDateTime.class;
        }
    }

    public static class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
        @Override
        public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String s = p.getText();
            if (s == null || s.isBlank()) return null;
            s = s.trim();
            try {
                return LocalDateTime.parse(s, DT_FMT);
            } catch (DateTimeParseException ignore) {
                return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
            }
        }
        @Override
        public Class<LocalDateTime> handledType() {
            return LocalDateTime.class;
        }
    }

    public static class LocalDateSerializer extends JsonSerializer<LocalDate> {
        @Override
        public void serialize(LocalDate v, JsonGenerator g, SerializerProvider sp) throws IOException {
            g.writeString(v == null ? null : v.format(D_FMT));
        }
        @Override
        public Class<LocalDate> handledType() {
            return LocalDate.class;
        }
    }

    public static class LocalDateDeserializer extends JsonDeserializer<LocalDate> {
        @Override
        public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
            String s = p.getText();
            if (s == null || s.isBlank()) return null;
            s = s.trim();
            try {
                return LocalDate.parse(s, D_FMT);
            } catch (DateTimeParseException ignore) {
                try {
                    return LocalDate.parse(s, DateTimeFormatter.ISO_LOCAL_DATE);
                } catch (DateTimeParseException e) {
                    return LocalDateTime.parse(s, DateTimeFormatter.ISO_LOCAL_DATE_TIME).toLocalDate();
                }
            }
        }
        @Override
        public Class<LocalDate> handledType() {
            return LocalDate.class;
        }
    }

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer partyJavaTimeCustomizer() {
        return builder -> builder
                .serializers(new LocalDateTimeSerializer(), new LocalDateSerializer())
                .deserializers(new LocalDateTimeDeserializer(), new LocalDateDeserializer());
    }
}
