package data.datasource.remote;

import data.datasource.remote.callback.ServerCallback;

import java.io.IOException;

/**
 * Client로 데이터 송, 수신 기본이 되는 정의서
 */
public interface RemoteDataSource {
    void openServer(ServerCallback callback);

    void sendData(String data);

    void closeServer() throws IOException;
}
