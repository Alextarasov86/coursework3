package alex.ru.server;

public class ServerApp {
    public static void main(String[] args) {
        new Server(2222,"serverFiles/").startServer();
    }
}
