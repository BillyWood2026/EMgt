package com.example.sysmgt.config;

import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 雪花ID精度修复：JS Number 安全整数上限为 2^53-1（16位），
 * 19位雪花ID以数字返回会被前端 JSON.parse 静默截断（尾部变000），
 * 导致按 ID 编辑/删除时报"用户不存在"。统一将 Long 序列化为字符串。
 */
@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer longToStringCustomizer() {
        return builder -> builder
                .serializerByType(Long.class, ToStringSerializer.instance)
                .serializerByType(Long.TYPE, ToStringSerializer.instance);
    }
}
