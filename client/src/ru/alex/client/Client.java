package ru.alex.client;

import ru.alex.common.ConnectionHandler;
import ru.alex.common.FileClass;
import ru.alex.common.Message;

import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private InetSocketAddress address;
    private String username;
    private final Scanner scanner;
    private ConnectionHandler connectionHandler;

    private boolean fileDownloading = false;

    private Object lock = new Object();

    private int maxDesciptionLength = 30;
    private long maxFileSizeInMb = 2;

    private String clientDirectory;

    public Client(InetSocketAddress address) {
        this.address = address;
        clientDirectory = "clientFiles/";
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
            System.out.println("Введите текст сообщения или /uploadfile чтобы послать файл или /getfile чтобы запросить файл: ");
            while (true) {
                    String text = scanner.nextLine();
                    sendMessage(username, text);

                    if (text.equals("/uploadfile")) {
                        String description;
                        String fileToSend;

                        while (true) {
                            System.out.println("Введите описание файла (не более " + maxDesciptionLength + " символов)");
                            description = scanner.nextLine();
                            if (description.length() <= maxDesciptionLength) {
                                break;
                            }
                            System.out.println("Описание слишком длинное");
                        }
                        while (true) {
                            System.out.println("Введите название файла без расширения (размер должен быть не более " + maxFileSizeInMb + " Mb)");
                            fileToSend = scanner.nextLine();
                            if (new File(clientDirectory + fileToSend + ".txt").length() <= maxFileSizeInMb * 1000000) {
                                break;
                            }
                            System.out.println("Файл слишком объёмный");
                        }
                        sendMessage(username, fileToSend + ".txt");
                        sendMessage(username, description);
                        try {

                            connectionHandler.sendFile(new FileClass(clientDirectory + fileToSend + ".txt"));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println("Введите текст сообщения или /uploadfile чтобы послать файл или /getfile чтобы запросить файл: ");
                    } else if (text.equals("/getfile")) {
                        fileDownloading = true;
                        while (fileDownloading) {
                            try {
                                Thread.sleep(500);
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        }
                        System.out.println("Введите текст сообщения или /uploadfile чтобы послать файл или /getfile чтобы запросить файл: ");
                    }
                }
            }
        }

    private class Reader extends Thread {
        Scanner sc = new Scanner(System.in);

        public void run(){
            while (true){
                    Message message = null;
                    try {
                        message = connectionHandler.read();
                        if (message.getText() != null) {
                            System.out.println(message.getText());
                            if (message.getText().startsWith("Список файлов на сервере:")) {
                                String requestedFileName = sc.nextLine();
                                connectionHandler.send(new Message("Client", requestedFileName + ".txt"));

                                String newFileName;
                                while (true) {
                                    System.out.println("Под каким названием сохранить полученный файл? (без расширения):");
                                    newFileName = sc.nextLine();
                                    if (!(new File(clientDirectory + newFileName + ".txt").exists())) {
                                        break;
                                    }
                                    System.out.println("Такой файл у вас уже существует!");
                                }

                                connectionHandler.receiveFile(clientDirectory + newFileName + ".txt");
                            }
                        }
                        fileDownloading = false;
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
