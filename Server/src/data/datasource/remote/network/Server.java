package data.datasource.remote.network;

import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

/**
 * 서버와 연결후 테이터 송, 수신만을 책임지는 클래스
 * 모든 데이터 변환, 및 관리는 하위 레이어에게 위임한다.
 *
 * @author 조재영
 */
public class Server extends Thread {
    private static final int port = 5050;
    private ServerSocket socket;
    private Socket clientSocket;
    private BufferedReader reader;
    private PrintWriter writer;
    private Thread receiveThread;

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

    /**
     * 서버가 닫히기 전까지의 모든 데이터를 DataSource Layer로 전송한다
     *
     * @param callback DataSourceLayer로 전송하기 위한 콜백
     */
    public void ReceiveData(ReceiveCallback callback) {
        receiveThread = new Thread(() -> {
            try {
                while (!this.isInterrupted()) {
                    callback.accept(reader.readLine());
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
        });
        receiveThread.start();

    }

    public synchronized void send(String data) {
        writer.println(data);
    }

    public void close() throws IOException {
        if (!receiveThread.isInterrupted()) receiveThread.interrupt();
        reader.close();
        writer.close();
        clientSocket.close();
        socket.close();
    }

    public interface ReceiveCallback {
        void accept(String data);
    }
}
