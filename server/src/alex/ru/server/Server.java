package alex.ru.server;

import ru.alex.common.ConnectionHandler;
import ru.alex.common.FileClass;
import ru.alex.common.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private int port;
    private String serverFilesDirectory;
    private String fileWithDescriptions;
    private List<ConnectionHandler> connectionHandlers = new ArrayList<>();

    public Server(int port, String serverFilesDirectory) {
        this.port = port;
        this.serverFilesDirectory = serverFilesDirectory;
        fileWithDescriptions = "filesInformation.txt";
    }

    public void startServer(){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            while (true){
                try {
                    Socket socket = serverSocket.accept();
                    ConnectionHandler connectionHandler = new ConnectionHandler(socket);
                    connectionHandlers.add(connectionHandler);
                    new Thread(() -> {
                        while (true) {
                            Message fromClient = null;
                            try {
                                fromClient = connectionHandler.read();
                            } catch (IOException e) {
                                throw new RuntimeException(e);
                            }

                            Message message = new Message("server");
                            if (fromClient.getText().equals("/uploadfile")) {
                                Message descriptionMessage;
                                String newFilename;
                                try {
                                    newFilename = connectionHandler.read().getText();
                                    descriptionMessage = connectionHandler.read();
                                    Message uploadMessage = new Message("server","На сервер выгружен новый файл: " + newFilename + ", описание следует");
                                    broadcast(connectionHandler, uploadMessage);
                                    broadcast(connectionHandler, descriptionMessage);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                if (! new File(serverFilesDirectory + newFilename).exists()) {
                                    connectionHandler.receiveFile(serverFilesDirectory + newFilename);
                                } else {
                                    // если файл с таким названием уже существует
                                    for (int i = 2;;i++) {
                                        if (! new File( serverFilesDirectory + i + "-" + newFilename).exists()) {
                                            connectionHandler.receiveFile(serverFilesDirectory + i + "-" + newFilename);
                                            break;
                                        }
                                    }
                                }
                                // Записываем информацию о полученном фйле в общий файл
                                try (FileWriter writer = new FileWriter(serverFilesDirectory + fileWithDescriptions, true)){
                                    writer.write(serverFilesDirectory + newFilename + "\n");
                                    writer.write(descriptionMessage.getText() + "\n");
                                    writer.flush();
                                } catch (IOException e) {
                                    System.out.println("Ошибка записи в файл");
                                }

                                continue;
                            } else if (fromClient.getText().equals("/getfile")) {
                                File file = new File(serverFilesDirectory + fileWithDescriptions);
                                System.out.println(file.length());
                                if(file.length() == 0){
                                    System.out.println("На сервере не файлов");
                                    continue;
                                }
                                try {
                                    sendFile(connectionHandler);
                                } catch (FileNotFoundException e) {
                                    System.out.println("Файл со списком файлов не найден!");
                                }
                            } else {
                                message.setText(fromClient.getText());
                            }

                            // рассылка обычного сообщения
                            broadcast(connectionHandler, message);
                        }

                    }).start();
                } catch (Exception e){
                    System.out.println("Проблема подключения");
                }
            }

        } catch (IOException e) {
            System.out.println("Ошибка сервера");
            throw new RuntimeException(e);
        }
    }


    private void sendFile(ConnectionHandler handler) throws FileNotFoundException {

        Scanner fileScanner = new Scanner(new File(serverFilesDirectory + fileWithDescriptions));

        String filesString = "Список файлов на сервере: \n";
        int counter = 1;
        while (fileScanner.hasNextLine()) {
            filesString += fileScanner.nextLine() + "  Описание: " + fileScanner.nextLine() + "\n";
        }

        filesString += "введите название желаемого файла без расширения";

        try {
            handler.send(new Message("server",filesString + ".txt"));
        } catch (IOException e) {
            System.out.println("Список файлов не был отправлен");
            throw new RuntimeException(e);
        }

        try {
            String chosenFileName = handler.read().getText();
            handler.sendFile(new FileClass(serverFilesDirectory + chosenFileName));
        } catch (IOException e) {
            System.out.println("Название желаемого файла не получено");
            throw new RuntimeException(e);
        }
    }

    public void broadcast(ConnectionHandler broadcastingHandler, Message message) {
        for (ConnectionHandler handler : connectionHandlers) {
            if (broadcastingHandler == handler) {
                continue;
            }
            try {
                handler.send(message);
            } catch (IOException e) {
                connectionHandlers.remove(handler);
            }
        }
    }
}
