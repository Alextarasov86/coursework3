package alex.ru.server;

import ru.alex.common.ConnectionHandler;
import ru.alex.common.FileClass;
import ru.alex.common.Message;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Server {
    private int port;
    private List<ConnectionHandler> connectionHandlers = new ArrayList<>();

    public Server(int port) {
        this.port = port;
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
                            if (fromClient.getText().equals("/file")) {
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
                                connectionHandler.receiveFile();   // todo multiple files
                                message.setText("New file uploaded to server: " + "file.txt");      // todo ap
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
