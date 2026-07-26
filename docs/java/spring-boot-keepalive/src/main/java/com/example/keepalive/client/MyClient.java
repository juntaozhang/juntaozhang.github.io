package com.example.keepalive.client;

import org.apache.hc.client5.http.classic.methods.HttpGet;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class MyClient {
    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private CloseableHttpClient httpClient;

    public static void main(String[] args) throws Exception {
        MyClient client = new MyClient();
//        client.testKeepAlive();
        client.testServerSentEvents();
    }

    public void testKeepAlive() throws IOException {
        httpClient = HttpClients.createDefault();
        try {
            logger.info("------------------- zhangsan -----------------------");
            hello("zhangsan");

            logger.info("\n\n---------------------- lisi --------------------");
            Thread.sleep(500);
            hello("lisi", true); // reuse connection before, and close it after response

            logger.info("\n\n---------------------- wangwu --------------------");
            Thread.sleep(500);
            hello("wangwu");// create new connection due to previous conn closed

            logger.info("\n\n---------------------- zhaoliu --------------------");
            Thread.sleep(10_000);
            hello("zhaoliu");// create new connection due to keep-alive is timeout
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        httpClient.close();
    }

    public void hello(String name) throws Exception {
        hello(name, false);
    }

    public void hello(String name, boolean close) throws Exception {
        HttpGet httpGet = new HttpGet("http://localhost:8080/hello?name=" + name);
        HttpClientContext ctx = HttpClientContext.create();
        if (close) {
            httpGet.setHeader("Connection", "close");
        }
        httpClient.execute(httpGet, ctx, response -> {
            int statusCode = response.getCode();
            logger.info("Status Code: {}", statusCode);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                String responseBody = EntityUtils.toString(entity, StandardCharsets.UTF_8);
                logger.info("Response Body: {}", responseBody);
            }
            return null;
        });
    }

    public void testServerSentEvents() throws Exception {
        if (httpClient == null) {
            httpClient = HttpClients.createDefault();
        }
        HttpGet get = new HttpGet("http://localhost:8080/sse");
        get.addHeader("Accept", "text/event-stream");
        long maxMillis = 8_000;
        long start = System.currentTimeMillis();
        httpClient.execute(get, HttpClientContext.create(), resp -> {
            logger.info("SSE status={}", resp.getCode());
            HttpEntity entity = resp.getEntity();
            if (entity == null) {
                logger.info("No SSE entity");
                return null;
            }
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(entity.getContent(), StandardCharsets.UTF_8))) {
                String line;
                StringBuilder eventData = new StringBuilder();
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        eventData.append(line.substring(5).trim()).append("\n");
                    } else if (line.isEmpty()) {
                        if (!eventData.isEmpty()) {
                            logger.info("SSE event data=\n{}", eventData.toString().trim());
                            eventData.setLength(0);
                        }
                    }
                    // 虽然client 不做处理，但是如果server端还在发送直到结束
                    if (System.currentTimeMillis() - start > maxMillis) {
                        logger.info("Stop SSE after {} ms", maxMillis);
                        break;
                    }
                }
            }
            return null;
        });

        hello("test");
    }
}
