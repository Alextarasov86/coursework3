package ru.alex.client;

import java.io.IOException;
import java.net.InetSocketAddress;

public class ClientApp {
    public static void main(String[] args) throws IOException {
        new Client(new InetSocketAddress("127.0.0.1", 2222)).startClient();
    }
}
