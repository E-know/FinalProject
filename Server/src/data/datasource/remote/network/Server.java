package data.datasource.remote.network;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class Server extends Thread {
    private static final int port = 5050;
    private ServerSocket socket;
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;

    public Server() {

    }

    public void startServer() {
        try {
            socket = new ServerSocket(port);

            System.out.println("IP : " + InetAddress.getLocalHost().getHostAddress());
            System.out.println("Server Open...");


            clientSocket = socket.accept();
            new Thread(this).start();

            System.out.println("클라이언트 접속 : " + clientSocket.getInetAddress());


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        super.run();
        try {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream(), StandardCharsets.UTF_8));
            writer = new PrintWriter(new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream(), StandardCharsets.UTF_8)),
                    true);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void ReceiveData(ReceiveCallback callback) {
        new Thread(() -> {
            try {
                while (!this.isInterrupted()) {
                    callback.accept(reader.readLine());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public synchronized void send(String data) {
        writer.println(data);
    }

    public void close() throws IOException {
        reader.close();
        writer.close();
        clientSocket.close();
        socket.close();
    }

    public interface ReceiveCallback {
        void accept(String data);
    }
}
