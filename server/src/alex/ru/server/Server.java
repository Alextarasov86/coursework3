package alex.ru.server;

import ru.alex.common.ConnectionHandler;
import ru.alex.common.FileClass;
import ru.alex.common.Message;

import java.io.File;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
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
                        Message fromClient = null;
                        try {
                            fromClient = connectionHandler.read();
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        System.out.println(fromClient.getText());
                        Message message = new Message("server");
                        message.setText("text");
                        for (ConnectionHandler handler : connectionHandlers) {
                            if(connectionHandler == handler){
                                continue;
                            }
                            try {
                                handler.send(message);
                            } catch (IOException e) {
                                connectionHandlers.remove(handler);
                            }
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
}
