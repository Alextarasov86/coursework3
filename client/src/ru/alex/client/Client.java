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
            while (true){
                System.out.println("Введите текст сообщения или /uploadfile чтобы послать файл или /getfile чтобы запросить файл: ");
                String text = scanner.nextLine();
                sendMessage(username, text);

                if (text.equals("/uploadfile")) {
                    String description;
                    String fileToSend;

                    while (true) {
                        System.out.println("Введите описание файла (не более " + maxDesciptionLength + " символов)");
                        description = scanner.nextLine();    //  todo AP or next()?
                        if (description.length() <= maxDesciptionLength) {
                            break;
                        }
                        System.out.println("Описание слишком длинное");
                    }
                    while (true) {
                        System.out.println("Введите название файла (размер должен быть не более " + maxFileSizeInMb + " Mb)");
                        fileToSend = scanner.nextLine();
                        if (new File(clientDirectory + fileToSend).length() <= maxFileSizeInMb *1000000) {
                            break;
                        }
                        System.out.println("Файл слишком объёмный");
                    }
                    sendMessage(username, description);
                    try {
//                        connectionHandler.send()      // todo ap what if server expects file but filenotfound?
                        connectionHandler.sendFile(new FileClass(clientDirectory + fileToSend));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                } else if (text.equals("/getfile")) {
                    String fileNumber = scanner.nextLine();
                    sendMessage(username, fileNumber);
                    System.out.println("Введите желаемое название файла:");
                    String newFileName = scanner.nextLine();
//                    connectionHandler.receiveFile(clientDirectory + newFileName + ".txt");
                }
            }
        }
    }

    private class Reader extends Thread {
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
