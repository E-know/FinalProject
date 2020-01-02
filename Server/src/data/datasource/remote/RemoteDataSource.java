package data.datasource.remote;

import data.datasource.remote.callback.ServerCallback;

import java.io.IOException;

public interface RemoteDataSource {
    void openServer(ServerCallback callback);

    void sendData(String data);

    void closeServer() throws IOException;
}
