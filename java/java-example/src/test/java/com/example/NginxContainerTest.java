package com.example;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

@Testcontainers
public class NginxContainerTest {

  @Container
  private static final GenericContainer<?> nginxContainer =
      new GenericContainer<>(DockerImageName.parse("nginx"))
          .withExposedPorts(80)
          .waitingFor(Wait.forHttp("/").forStatusCode(200))
          .withStartupTimeout(java.time.Duration.ofSeconds(30));

  @AfterAll
  public static void shutdown() {
    nginxContainer.close();
  }

  @Test
  void shouldServeDefaultNginxPage() throws IOException, InterruptedException {
    int mappedPort = nginxContainer.getMappedPort(80);
    String host = nginxContainer.getHost();

    // 2. 构建请求URL
    String url = String.format("http://%s:%d", host, mappedPort);
    System.out.println("Accessing Nginx at: " + url); // 调试日志

    // 3. 发送HTTP请求（Java 11+ HttpClient）
    HttpClient client = HttpClient.newHttpClient();
    HttpRequest request = HttpRequest.newBuilder().uri(URI.create(url)).GET().build();

    HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

    // 4. 验证响应
    Assertions.assertEquals(200, response.statusCode(), "HTTP status should be 200");
    Assertions.assertTrue(
        response.body().contains("Welcome to nginx!"),
        "Response should contain nginx welcome text");
    Assertions.assertTrue(
        response.headers().firstValue("Server").orElse("").startsWith("nginx"),
        "Server header should indicate nginx");
  }

  @Test
  public void shouldContainerBeRunning() {
    Assertions.assertTrue(nginxContainer.isRunning(), "Nginx container should be running");
    Assertions.assertTrue(nginxContainer.getMappedPort(80) > 0, "Mapped port should be valid");
  }
}
