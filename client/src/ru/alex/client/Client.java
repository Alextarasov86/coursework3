package ru.alex.client;

import ru.alex.common.ConnectionHandler;
import ru.alex.common.FileClass;
import ru.alex.common.Message;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private InetSocketAddress address;
    private String username;
    private final Scanner scanner;
    private ConnectionHandler connectionHandler;

    public Client(InetSocketAddress address) {
        this.address = address;
        scanner = new Scanner(System.in);
    }

    private void createConnection() throws IOException {
        connectionHandler =
                    new ConnectionHandler(
                            new Socket(address.getHostName(), address.getPort()));
    }

    private class Writer extends Thread{

        public void sendMessage(String username, String text) {
            Message message = new Message(username);
            message.setText(text);
            try {
                connectionHandler.send(message);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }


        public void run(){
            while (true){
                System.out.println("Введите текст сообщения или /file чтобы послать файл: ");
                String text = scanner.nextLine();
                sendMessage(username, text);

                if (text.equals("/file")) {
                    System.out.println("Введите описание файла");
                    String description = scanner.nextLine();    //  todo AP or next()?
                    sendMessage(username, description);
                    try {
//                        connectionHandler.send()
                        connectionHandler.sendFile(new FileClass("test1.txt"));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                }
            }
        }
    }

    private class Reader extends Thread{
        public void run(){
            while (true){
                Message message = null;
                try {
                    message = connectionHandler.read();
                    System.out.println(message.getText());
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    public void startClient() throws IOException {
        System.out.println("Введите имя: ");
        username = scanner.nextLine();
        createConnection();
        new Writer().start();
        new Reader().start();
    }
}
