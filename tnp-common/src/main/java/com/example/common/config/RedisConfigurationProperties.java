package com.example.common.config;

import java.util.ArrayList;
import java.util.List;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "spring.redis.cluster")
public class RedisConfigurationProperties {

    /**
     * Redis cluster nodes in the format {@code host:port}. Example: spring.redis.cluster.nodes[0] =
     * 127.0.0.1:7379 spring.redis.cluster.nodes[1] = 127.0.0.1:7380
     */
    private List<String> nodes = new ArrayList<>();

    /** Maximum redirects for cluster connections */
    private int maxRedirects = 3;

    public RedisConfigurationProperties() {
        // Default constructor
    }

    public List<String> getNodes() {
        return nodes;
    }

    public void setNodes(List<String> nodes) {
        this.nodes = nodes;
    }

    public int getMaxRedirects() {
        return maxRedirects;
    }

    public void setMaxRedirects(int maxRedirects) {
        this.maxRedirects = maxRedirects;
    }
}
