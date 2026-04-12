package com.leander.lottery.lottery.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;

@Configuration
public class RedisConfig {

    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        // 設定多型支援 (Polymorphic Typing)
        // 這確保抽獎系統存入的 Item/Campaign 物件在讀取時不會變成 LinkedHashMap
        PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        ObjectMapper om = JsonMapper.builder()
                .changeDefaultVisibility(vc ->
                        vc.withVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY)
                )
                .activateDefaultTyping(
                        ptv,
                        DefaultTyping.NON_FINAL,
                        JsonTypeInfo.As.PROPERTY
                )
                .build();

        // 建立序列化器
        // 在 Spring Boot 4 中，GenericJacksonJsonRedisSerializer 會完美匹配 tools.jackson 的 ObjectMapper
        GenericJacksonJsonRedisSerializer jsonSerializer = new GenericJacksonJsonRedisSerializer(om);

        // 配置 Key 與 Value 的序列化方式
        StringRedisSerializer stringSerializer = new StringRedisSerializer();

        // Key 與 HashKey 使用 String
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);

        // Value 與 HashValue 使用 JSON
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);

        template.afterPropertiesSet();
        return template;
    }
}