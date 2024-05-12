package alex.ru.server;

import ru.alex.common.ConnectionHandler;
import ru.alex.common.FileClass;
import ru.alex.common.Message;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Array;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Server {
    private static int fileCounter = 1;

    private int port;
    private String filesDirectory;
    private List<ConnectionHandler> connectionHandlers = new ArrayList<>();

    public Server(int port, String filesDirectory) {
        this.port = port;
        this.filesDirectory = filesDirectory;
    }

    public void startServer(){
        try(ServerSocket serverSocket = new ServerSocket(port)){
            while (true){
//                Сервер получает сообщение от клиента и рассылает по всем активным соединениям
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
                                Message descriptionMessage;      // todo AP readMessage method
                                try {
                                    descriptionMessage = connectionHandler.read();
                                    Message uploadMessage = new Message("server");
                                    message.setText("На сервер выгружен новый файл, описание следует");
                                    broadcast(connectionHandler, uploadMessage);
                                    broadcast(connectionHandler, descriptionMessage);
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                connectionHandler.receiveFile("file" + fileCounter++ + ".txt");   // todo multiple files
                                message.setText("New file uploaded to server: " + "file.txt");      // todo ap
                            } else if (fromClient.getText().equals("/getfile")) {
                                sendListOfFiles(connectionHandler);
                            } else {
                                message.setText(fromClient.getText());
                            }

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

    private void sendListOfFiles(ConnectionHandler handler) {       // todo ap 6/10 move to connectionHandler
        // todo ap nullpointerexception?
        Set<String> fileSet =  Stream.of(new File(filesDirectory).listFiles())
                                            .filter(file -> !file.isDirectory())
                                            .map(File::getName)
                                            .collect(Collectors.toSet());

        String filesString = "Список файлов на сервере: \n";
        int counter = 1;
        for (String fileString : fileSet) {
            filesString += "(" + counter++ + ") " + fileString + "\n";      // todo ap StringBuilder?
        }
        filesString += "Выберите желаемый файл";

        try {
            handler.send(new Message("server",filesString));
        } catch (IOException e) {
            System.out.println("Список файлов не был отправлен");
            throw new RuntimeException(e);      // todo ap and other RuntimeExceptions
        }

        //----------

        try {
            String chosenFileNumber = handler.read().getText();
            handler.sendFile(new FileClass(filesDirectory + "file" + chosenFileNumber + ".txt"));
        } catch (IOException e) {
            System.out.println("Номер желаемого файла не получен");
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
