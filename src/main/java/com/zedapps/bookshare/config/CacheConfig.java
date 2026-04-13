package com.zedapps.bookshare.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.List;

/**
 * @author smzoha
 * @since 12/4/26
 **/
@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager cacheManager() {
        SimpleCacheManager cacheManager = new SimpleCacheManager();

        cacheManager.setCaches(List.of(
                // Individual items
                build("books", 5000, Duration.ofHours(12)),
                build("authors", 1000, Duration.ofHours(12)),
                build("genres", 200, Duration.ofHours(24)),
                build("tags", 500, Duration.ofHours(24)),
                build("shelves", 1000, Duration.ofMinutes(10)),
                build("logins", 500, Duration.ofMinutes(30)),
                build("feed", 1000, Duration.ofSeconds(60)),

                // Lists
                build("book-lists", 500, Duration.ofMinutes(30)),
                build("author-lists", 200, Duration.ofMinutes(30)),
                build("genre-lists", 50, Duration.ofHours(24)),
                build("tag-lists", 100, Duration.ofHours(24)),
                build("shelf-lists", 200, Duration.ofMinutes(5))
        ));

        return cacheManager;
    }

    private CaffeineCache build(String name, int maxSize, Duration ttl) {
        return new CaffeineCache(name, Caffeine.newBuilder()
                .maximumSize(maxSize)
                .expireAfterWrite(ttl)
                .recordStats()
                .build());
    }
}
