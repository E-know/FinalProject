package data.datasource.remote;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import data.datasource.remote.callback.ServerCallback;
import data.datasource.remote.network.Server;

import java.io.IOException;

public class RemoteDataSourceImpl implements RemoteDataSource {
    private static RemoteDataSource INSTANCE = null;
    private Server server;
    private JsonParser parser = new JsonParser();

    private RemoteDataSourceImpl(Server server) {
        this.server = server;
    }

    public static RemoteDataSource getInstance(final Server server) {
        if (INSTANCE == null) INSTANCE = new RemoteDataSourceImpl(server);
        return INSTANCE;
    }

    @Override
    public void openServer(ServerCallback callback) {
        server.startServer();
        server.ReceiveData(data -> {
            JsonObject object = parser.parse(data).getAsJsonObject();
            System.out.println(object.toString());
            if (object.get("login") != null) {
                callback.login();
            } else if (object.get("select") != null) {
                callback.selectItem(object.get("select").toString());
            } else if (object.get("minus") != null) {
                callback.minusItem(object.get("minus").toString());
            } else if (object.get("exit") != null) {
                callback.exitCallback(object.get("exit").toString(), object.get("count").getAsInt());
            } else {
                callback.error();
            }

        });

    }

    @Override
    public void sendData(final String data) {
        server.send(data);
    }

    @Override
    public void closeServer() throws IOException {
        server.close();
    }

}
