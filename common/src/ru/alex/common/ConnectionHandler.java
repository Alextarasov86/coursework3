package ru.alex.common;

import java.io.*;
import java.net.Socket;
import java.util.Objects;

public class ConnectionHandler implements AutoCloseable{

    private ObjectInputStream inputStream;
    private ObjectOutputStream outputStream;
    private Socket socket;


    public ConnectionHandler(Socket socket) throws IOException {
        this.socket = Objects.requireNonNull(socket);
        this.outputStream = new ObjectOutputStream(socket.getOutputStream());
        this.inputStream = new ObjectInputStream(socket.getInputStream());
    }

    public void send(Message message) throws IOException {
        outputStream.writeObject(message);
        outputStream.flush();
    }

    public Message read() throws IOException{
        try{
            return (Message) inputStream.readObject();
        } catch (ClassNotFoundException e){
            throw new RuntimeException(e);
        }
    }

    // sendFile function define here
    public void sendFile(FileClass file) throws IOException {
        int bytes = 0;
        // Open the File where he located in your pc
        File myFile = new File(file.getPath());

        try {
            FileInputStream fileInputStream = new FileInputStream(file.getPath());
            // Here we send the File to Server
            outputStream.writeLong(myFile.length());
            // Here we  break file into chunks
            byte[] buffer = new byte[4 * 1024];
            while ((bytes = fileInputStream.read(buffer))
                    != -1) {
                // Send the file to Server Socket
                outputStream.write(buffer, 0, bytes);
                outputStream.flush();
            }
            // close the file here
            fileInputStream.close();
        } catch (FileNotFoundException e) {
            System.out.println(file.getPath() + ": Такого файла не существует");
        }
    }

    // receive file function is start here
    public void receiveFile(String destinationFile)  {
        int bytes = 0;
        File myFile = new File(destinationFile);

        FileOutputStream fileOutputStream
                = null;
        try {
            fileOutputStream = new FileOutputStream(destinationFile);
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }

        long size = 0; // read file size
        try {
            size = inputStream.readLong();
        } catch (IOException e) {
            System.out.println("Не прочитать лонг");
        }
        byte[] buffer = new byte[4 * 1024];
        while (true) {
            try {
                if (!(size > 0
                        && (bytes = inputStream.read(buffer, 0, (int)Math.min(buffer.length, size))) != -1))
                    break;
            } catch (IOException e) {
                System.out.println("Не прочитать");
            }
            // Here we write the file using write method
            try {
                fileOutputStream.write(buffer, 0, bytes);
            } catch (IOException e) {
                System.out.println("Не записать");
            }
            size -= bytes; // read upto file size
        }

        // Here we received file
        System.out.println("Файл получен!");

        try {
            fileOutputStream.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void close() throws Exception {
        outputStream.close();
        inputStream.close();
        socket.close();
    }
}
