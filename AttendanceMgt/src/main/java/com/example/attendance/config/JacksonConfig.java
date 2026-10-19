package com.example.attendance.config;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Jackson 配置：兼容前端提交的 "yyyy-MM-dd HH:mm" / ISO 时间，
 * 统一以 "yyyy-MM-dd HH:mm:ss" 输出，日期以 "yyyy-MM-dd" 输出。
 */
@Configuration
public class JacksonConfig {

    private static final DateTimeFormatter DT_FULL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private static final DateTimeFormatter DT_MINUTES = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer dateTimeCustomizer() {
        return builder -> {
            JavaTimeModule module = new JavaTimeModule();

            module.addSerializer(LocalDateTime.class, new JsonSerializer<>() {
                @Override
                public void serialize(LocalDateTime value, JsonGenerator gen, SerializerProvider serializers)
                        throws IOException {
                    gen.writeString(value.format(DT_FULL));
                }
            });
            module.addDeserializer(LocalDateTime.class, new JsonDeserializer<>() {
                @Override
                public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String text = p.getText();
                    if (text == null || text.isBlank()) {
                        return null;
                    }
                    String s = text.trim().replace('T', ' ');
                    if (s.length() <= 10) {
                        return LocalDate.parse(s.substring(0, 10), DATE).atStartOfDay();
                    }
                    if (s.length() == 16) {
                        return LocalDateTime.parse(s, DT_MINUTES);
                    }
                    return LocalDateTime.parse(s, DT_FULL);
                }
            });

            module.addSerializer(LocalDate.class, new JsonSerializer<>() {
                @Override
                public void serialize(LocalDate value, JsonGenerator gen, SerializerProvider serializers)
                        throws IOException {
                    gen.writeString(value.format(DATE));
                }
            });
            module.addDeserializer(LocalDate.class, new JsonDeserializer<>() {
                @Override
                public LocalDate deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                    String text = p.getText();
                    if (text == null || text.isBlank()) {
                        return null;
                    }
                    String s = text.trim().replace('T', ' ');
                    return LocalDate.parse(s.substring(0, 10), DATE);
                }
            });

            builder.modulesToInstall(module);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        };
    }
}
