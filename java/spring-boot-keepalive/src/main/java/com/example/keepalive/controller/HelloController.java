package com.example.keepalive.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@RestController
public class HelloController {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @GetMapping("/hello")
    public String hello(String name) {
        logger.info("Received /hello request from {}", name);
        return "hello @" + Instant.now();
    }

    /**
     * 服务器发送事件 (SSE) 端点
     * 使用响应式编程实现真正的长连接
     */
    @GetMapping(value = "/sse", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> serverSentEvents() {
        return Flux.interval(Duration.ofSeconds(1))
                .map(sequence -> {
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    return String.format("data: {\"sequence\": %d, \"timestamp\": \"%s\", \"message\": \"服务器推送消息\"}\n\n",
                            sequence, timestamp);
                })
                .take(11);
    }
}
