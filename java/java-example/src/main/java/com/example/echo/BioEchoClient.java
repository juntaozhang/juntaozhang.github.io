package com.example.echo;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

/**
 * BIO (Blocking IO) Echo Client
 * 传统的阻塞IO客户端示例
 */
public class BioEchoClient {

    private static final String HOST = "localhost";
    private static final int PORT = 8080;

    public static void main(String[] args) {
        try (
            Socket socket = new Socket(HOST, PORT);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(socket.getInputStream()));
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("Connected to BIO Echo Server at " + HOST + ":" + PORT);
            System.out.println("Type messages to echo (type 'bye' to exit):");

            // 启动一个线程来读取服务器响应
            new Thread(() -> {
                try {
                    String response;
                    while ((response = in.readLine()) != null) {
                        System.out.println(response);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();

            // 主线程读取用户输入并发送到服务器
            String userInput;
            while (scanner.hasNextLine()) {
                userInput = scanner.nextLine();
                out.println(userInput);

                if ("bye".equalsIgnoreCase(userInput)) {
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}