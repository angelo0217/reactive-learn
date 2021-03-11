package com.reactive.learn.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.module.paramnames.ParameterNamesModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.integration.redis.util.RedisLockRegistry;

import javax.annotation.PreDestroy;

@Configuration
public class RedisConfig {
    @Autowired
    private RedisConnectionFactory factory;

//    @Bean
//    public RedisSerializationContext redisSerializationContext() {
//        RedisSerializationContext.RedisSerializationContextBuilder builder = RedisSerializationContext.newSerializationContext();
//        builder.key(StringRedisSerializer.UTF_8);
//        builder.value(RedisSerializer.json());
//        builder.hashKey(StringRedisSerializer.UTF_8);
//        builder.hashValue(StringRedisSerializer.UTF_8);
//
//        return builder.build();
//    }
//
//    @Bean
//    public ReactiveRedisTemplate reactiveRedisTemplate(ReactiveRedisConnectionFactory connectionFactory) {
//        RedisSerializationContext serializationContext = redisSerializationContext();
//        ReactiveRedisTemplate reactiveRedisTemplate = new ReactiveRedisTemplate(connectionFactory,serializationContext);
//        return reactiveRedisTemplate;
//    }

    @Bean("jacksonSerializer")
    public Jackson2JsonRedisSerializer<Object> jacksonSerializer() {
        Jackson2JsonRedisSerializer<Object> serializer  = new Jackson2JsonRedisSerializer<>(
                Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new ParameterNamesModule());
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL,
                JsonTypeInfo.As.WRAPPER_ARRAY);

        serializer.setObjectMapper(objectMapper);

        return serializer;
    }

    @Bean("objectTemplate")
    public <T> ReactiveRedisTemplate<String, T> reactiveRedisTemplate(ReactiveRedisConnectionFactory factory,
                                                                      Jackson2JsonRedisSerializer<T> jacksonSerializer) {
        RedisSerializationContext.RedisSerializationContextBuilder<String, T> builder = RedisSerializationContext
                .newSerializationContext(new StringRedisSerializer());
        RedisSerializationContext<String, T> context = builder.value(jacksonSerializer).build();
        return new ReactiveRedisTemplate<>(factory, context);
    }

    @Bean
    public RedisLockRegistry redisLockRegistry(RedisConnectionFactory redisConnectionFactory) {
        return new RedisLockRegistry(redisConnectionFactory, "commerce-lock", 10 * 1000);
    }

    /**
     * Clear database before shut down.
     *
     * */
    @PreDestroy
    public void cleanRedis() {
        factory.getConnection().flushDb();
    }
}