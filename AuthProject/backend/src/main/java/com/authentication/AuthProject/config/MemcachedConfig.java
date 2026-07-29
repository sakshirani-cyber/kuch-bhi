package com.authentication.AuthProject.config;

import net.spy.memcached.MemcachedClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.InetSocketAddress;

// Configures the Spy Memcached client from application properties.
 
@Configuration
public class MemcachedConfig {

    @Value("${app.memcached.host}")
    private String host;

    @Value("${app.memcached.port}")
    private int port;

    @Bean(destroyMethod = "shutdown")
    public MemcachedClient memcachedClient() throws IOException {
        return new MemcachedClient(new InetSocketAddress(host, port));
    }
}
