package com.example.onlinevideo.Config;

import com.example.onlinevideo.Enum.VideoType;
import com.example.onlinevideo.Enum.VideoApprovalStatus;
import com.example.onlinevideo.Enum.ReportStatus;
import com.example.onlinevideo.Enum.ReportType;
import com.example.onlinevideo.TypeHandler.VideoTypeTypeHandler;
import com.example.onlinevideo.TypeHandler.VideoApprovalStatusTypeHandler;
import com.example.onlinevideo.TypeHandler.ReportStatusTypeHandler;
import com.example.onlinevideo.TypeHandler.ReportTypeTypeHandler;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.type.TypeHandlerRegistry;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import java.time.Duration;

@Configuration
@EnableCaching
public class CacheConfig {
    
    @Resource
    private SqlSessionFactory sqlSessionFactory;
    
    @PostConstruct
    public void registerTypeHandlers() {
        TypeHandlerRegistry registry = sqlSessionFactory.getConfiguration().getTypeHandlerRegistry();
        
        // Explicitly register custom TypeHandlers to ensure they're used instead of default EnumTypeHandler
        registry.register(VideoType.class, VideoTypeTypeHandler.class);
        registry.register(VideoApprovalStatus.class, VideoApprovalStatusTypeHandler.class);
        registry.register(ReportStatus.class, ReportStatusTypeHandler.class);
        registry.register(ReportType.class, ReportTypeTypeHandler.class);
    }
    
    @Bean
    public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
        RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofHours(1))
                .serializeKeysWith(RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(new GenericJackson2JsonRedisSerializer()));
        return RedisCacheManager.builder(connectionFactory).cacheDefaults(config).build();
    }
}
